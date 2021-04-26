package com.zgr.smtp.sender.service.impl;

import com.zgr.smtp.sender.enums.MessageType;
import com.zgr.smtp.sender.enums.RepeatSendState;
import com.zgr.smtp.sender.model.CascadeChainLink;
import com.zgr.smtp.sender.model.Data;
import com.zgr.smtp.sender.model.Message;
import com.zgr.smtp.sender.model.NotificationRequest;
import com.zgr.smtp.sender.model.NotificationResponse;
import com.zgr.smtp.sender.model.PushContent;
import com.zgr.smtp.sender.model.PushData;
import com.zgr.smtp.sender.service.MailProcessingService;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MailProcessingServiceImpl implements MailProcessingService {
    private final RestTemplate restTemplate;

    @Value("${mail-sender.send.login}")
    private String login;
    @Value("${mail-sender.send.password}")
    private String password;
    @Value("${mail-sender.send.ttl}")
    private int messageTtl;
    @Value("${mail-sender.send.ttlUnit}")
    private String ttlUnit;
    @Value("${mail-sender.send.url}")
    private String url;
    @Value("${mail-sender.send.service-numbers.push}")
    private String serviceNumberPush;
    @Value("${mail-sender.send.service-numbers.sms}")
    private String serviceNumberSms;
    @Value("${mail-sender.mail.folders.error}")
    private String errorFolder;
    @Value("${mail-sender.mail.folders.success}")
    private String successFolder;
    @Value("${mail-sender.send.send-state}")
    private String sendState;

    @Override
    public void process(MimeMessage message) {
        String messageId = UUID.randomUUID().toString();
        MimeMessageParser parser = new MimeMessageParser(message);
        Try.run(parser::parse).get();
        loggIt(parser, messageId);
        NotificationRequest requestBody = Try.of(() -> buildRequest(parser, messageId))
                .onFailure(throwable -> {
                    log.error("Failed build request for message with id: {}", messageId, throwable);
                    relocateMessageTo(message, errorFolder, messageId);
                })
                .get();
        if (requestBody != null) {
            RequestEntity<NotificationRequest> request = RequestEntity
                    .post(URI.create(url))
                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .body(requestBody);

            log.info("Send notification request to:{} with body:{}", url, requestBody);
            Try.of(() -> restTemplate.exchange(request, NotificationResponse.class))
                    .onSuccess(response -> {
                        if (response.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(response.getBody()).hasBody()) {
                            log.info("Message with id: {} successfully sent", messageId);
                            relocateMessageTo(message, successFolder, messageId);
                        } else {
                            log.error("Fail sending message! set status ERROR to message with id:{}, status code:{}, message body:{}",
                                    messageId, response.getStatusCode(), Objects.requireNonNull(response.getBody())
                            );
                            relocateMessageTo(message, errorFolder, messageId);
                        }
                    }).onFailure(throwable -> {
                log.error("Fail sending message! Set status ERROR to message with id:{}", messageId, throwable);
                        relocateMessageTo(message, errorFolder, messageId);
                    }
            ).get();
        }
    }

    private void loggIt(@NonNull MimeMessageParser parser, String messageId) {
        String subject = Try.of(parser::getSubject).getOrElse("null");
        String sender = Try.of(parser::getFrom).get();
        String to = Try.of(parser::getTo).get().get(0).toString();
        to = !to.contains("\"") ? to : to.substring(to.indexOf("\"") + 1, to.lastIndexOf("\""));
        log.info("Received mail. subject: {}, from: {}, to: {} with id: {}", subject, sender, to, messageId);
    }

    private void relocateMessageTo(MimeMessage message, String folder, String messageId) {
        javax.mail.Message[] messageArray = new javax.mail.Message[]{message};
        Folder oldFolder = message.getFolder();
        try {
            Folder newFolder = oldFolder.getStore().getFolder(folder);
            oldFolder.close();
            oldFolder.open(Folder.READ_WRITE);
            newFolder.appendMessages(messageArray);
            messageArray[0].setFlag(Flags.Flag.DELETED, true);
            oldFolder.setFlags(messageArray, new Flags(Flags.Flag.DELETED), true);
            oldFolder.close(true);
            log.info("Message with id:{} successfully moved to folder: {}", messageId, folder);
        } catch (MessagingException e) {
            log.error("Error relocate message with id: {} to folder: {}", messageId, folder, e);
        }
    }


    private NotificationRequest buildRequest(MimeMessageParser parser, String uuid) {
        String subject = Try.of(parser::getSubject).getOrElse("null");
        String content = parser.getPlainContent();
        PushContent pushContent = PushContent.builder()
                .actions(new ArrayList<>())
                .build();
        Message push = !subject.equals("null") ? Message.builder()
                .type(MessageType.PUSH)
                .data(PushData.builder()
                        .content(pushContent)
                        .text(content)
                        .serviceNumber(serviceNumberPush)
                        .ttl(messageTtl)
                        .ttlUnit(ttlUnit)
                        .build())
                .build()
                : Message.builder()
                .type(MessageType.PUSH)
                .data(PushData.builder()
                        .title(subject)
                        .content(pushContent)
                        .text(content)
                        .serviceNumber(serviceNumberPush)
                        .ttl(messageTtl)
                        .ttlUnit(ttlUnit)
                        .build())
                .build();
        Message sms = Message.builder()
                .type(MessageType.SMS)
                .data(Data.builder()
                        .text(content)
                        .serviceNumber(serviceNumberSms == null ? serviceNumberPush : serviceNumberSms)
                        .ttl(messageTtl)
                        .ttlUnit(ttlUnit)
                        .build())
                .build();
        return NotificationRequest.builder()
                .login(login)
                .password(password)
                .useTimeDiff(true)
                .id(uuid)
                .destAddr(getPhoneFromMail(parser))
                .message(push)
                .cascadeChainLink(CascadeChainLink.builder()
                        .state(RepeatSendState.valueOf(sendState))
                        .message(sms)
                        .build())
                .build();
    }

    private String getPhoneFromMail(@NonNull MimeMessageParser parser) {
        String to = Try.of(parser::getTo).get().get(0).toString();
        to = !to.contains("\"") ? to : to.substring(to.indexOf("\"") + 1, to.lastIndexOf("\""));
        to = to.substring(0, to.lastIndexOf('@'));
        if (to.matches("^(?:\\+?7|8)\\d{10}$")) {
            return to;
        } else {
            throw new UnsupportedOperationException(String.format("Wrong destAddr value fom message, username: %s", to));
        }
    }
}
