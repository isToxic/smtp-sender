package com.zgr.smtp.sender.config;


import com.zgr.smtp.sender.service.MailProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IMAPIdleConfiguration {

    @Value("${mail-sender.mail.user-name}")
    private String userName;
    @Value("${mail-sender.mail.password}")
    private String password;
    @Value("${mail-sender.mail.host}")
    private String host;
    @Value("${mail-sender.mail.port}")
    private String port;
    @Value("${mail-sender.mail.folders.receive}")
    private String mailbox;
    @Value("${mail-sender.mail.folders.error}")
    private String errorMailbox;
    @Value("${mail-sender.mail.folders.success}")
    private String successMailbox;
    @Value("${mail-sender.mail.polling-ms}")
    private long pollingMs;
    @Value("${mail-sender.mail.max-messages-per-poll}")
    private int maxMessagesPerPoll;
    @Value("${mail-sender.mail.max-poll-size}")
    private int maxPollSize;
    @Value("${mail-sender.mail.connection-poll-size}")
    private int connectionPollSize;
    @Value("${mail-sender.mail.debug}")
    private Boolean mailDebug;
    @Value("${mail-sender.mail.partialfetch}")
    private Boolean partialFetch;
    @Value("${mail-sender.mail.timeout}")
    private int mailTimeout;

    private final MailProcessingService mailProcessingService;

    @Bean
    public IntegrationFlow mailListener() {
        String imapMailReceiverURL = String.format("imap://%s:%s/%s", host, port, mailbox);
        return IntegrationFlows.from(Mail.imapInboundAdapter(imapMailReceiverURL)
                        .maxFetchSize(maxMessagesPerPoll)
                        .autoCloseFolder(false)
                        .simpleContent(false)
                        .javaMailProperties(props())
                        .javaMailAuthenticator(authenticator()).get(),
                e -> e.autoStartup(true)
                        .poller(Pollers.fixedDelay(pollingMs)
                                .taskExecutor(executor())
                                .maxMessagesPerPoll(maxMessagesPerPoll)).get())
                .split(MimeMessage.class, this::prepareFolders)
                .handle(mailProcessingService, "process")
                .get();
    }

    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPollSize);
        return executor;
    }

    @Bean
    public Authenticator authenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
    }

    @Bean
    public Properties props() {
        Properties p = new Properties();
        p.put("mail.imap.connectionpoolsize", connectionPollSize);
        p.put("mail.imap.partialfetch", partialFetch.toString());
        p.put("mail.store.protocol", "imap");
        p.put("mail.imap.starttls.enable", "true");
        p.put("mail.imap.ssl.enable", "false");
        p.put("mail.imap.ssl.trust", "*");
        p.put("mail.imap.socketFactory.fallback", "true");
        p.put("mail.debug", mailDebug.toString());
        p.put("mail.imap.timeout", mailTimeout);
        return p;
    }

    private Message prepareFolders(Message message) {
        List<String> needFoldersNames = Arrays.asList(errorMailbox, successMailbox);
        try {
            Store store = message.getFolder().getParent().getStore();
            needFoldersNames.forEach(folderName -> {
                try {
                    Folder folder = store.getFolder(folderName);
                    if (!folder.exists()) {
                        if (folder.create(Folder.HOLDS_FOLDERS)) {
                            folder.setSubscribed(true);
                            log.info("Folder: {} was created successfully", folderName);
                        }
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                    log.error("Error work with folder.", e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error getting folder", e);
        }
        log.info("Service has all folders for work: {},{}", successMailbox, errorMailbox);
        return message;
    }
}
