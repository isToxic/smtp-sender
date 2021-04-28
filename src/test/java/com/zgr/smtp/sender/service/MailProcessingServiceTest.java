package com.zgr.smtp.sender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sun.mail.imap.IMAPStore;
import com.zgr.smtp.sender.config.HttpConfig;
import com.zgr.smtp.sender.enums.MessageType;
import com.zgr.smtp.sender.model.NotificationResponse;
import com.zgr.smtp.sender.service.impl.MailProcessingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(value = "file:src/test/resources/application.yml")
@SpringBootTest(classes = {MailProcessingService.class, MailProcessingServiceImpl.class, HttpConfig.class})
public class MailProcessingServiceTest {
    private final MailProcessingService mailProcessingService;
    private final RestTemplate restTemplate;
    private static final String userName = GreenMailUtil.random();
    private static final String pass = GreenMailUtil.random();
    private static GreenMail greenMail;
    private static GreenMailUser user;
    private static final String phone = "7".concat(RandomStringUtils.randomNumeric(10));
    private static final String email = phone.concat("@localhost");
    private static final String successFolder = GreenMailUtil.random();
    private static final String errorFolder = GreenMailUtil.random();
    private MockRestServiceServer server;

    @Value("${mail-sender.send.url}")
    private String url;
    @Value("${mail-sender.send.login}")
    private String login;
    @Value("${mail-sender.send.password}")
    private String password;
    @Value("${mail-sender.send.ttl}")
    private int messageTtl;
    @Value("${mail-sender.send.ttlUnit}")
    private String ttlUnit;
    @Value("${mail-sender.send.service-numbers.push}")
    private String serviceNumberPush;
    @Value("${mail-sender.send.service-numbers.sms}")
    private String serviceNumberSms;
    @Value("${mail-sender.send.send-state}")
    private String sendState;


    @BeforeAll
    static void setUpAll() {
        greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP);
        greenMail.start();
        user = greenMail.setUser("all_mail@localhost", userName, pass);
        System.setProperty("mail-sender.mail.host", "localhost");
        System.setProperty("mail-sender.mail.port", String.valueOf(greenMail.getImap().getPort()));
        System.setProperty("mail-sender.mail.folders.success", successFolder);
        System.setProperty("mail-sender.mail.folders.error", errorFolder);
        System.setProperty("mail-sender.mail.user-name", userName);
        System.setProperty("mail-sender.mail.password", pass);

    }

    @Test
    public void successfullySendTest() throws MessagingException, JsonProcessingException {
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(restTemplate);
        server = MockRestServiceServer.createServer(gateway);
        String response = new ObjectMapper().writeValueAsString(
                NotificationResponse.builder()
                        .id(GreenMailUtil.random())
                        .mtNum(GreenMailUtil.random())
                        .build());
        server.expect(ExpectedCount.once(), requestTo(url))
                .andExpect(MockRestRequestMatchers.jsonPath("$.login", Matchers.equalToIgnoringCase(login)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.password", Matchers.equalToIgnoringCase(password)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.useTimeDiff", Matchers.equalTo(true)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.destAddr", Matchers.equalToIgnoringCase(phone)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.message.type", Matchers.equalToIgnoringCase(MessageType.PUSH.name())))
                .andExpect(MockRestRequestMatchers.jsonPath("$.message.data.title", Matchers.equalToIgnoringCase(subject)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.message.data.text", Matchers.equalToIgnoringCase(body)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.message.data.serviceNumber", Matchers.equalToIgnoringCase(serviceNumberPush)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.message.data.ttl", Matchers.equalTo(messageTtl)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.message.data.ttlUnit", Matchers.equalTo(ttlUnit)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.cascadeChainLink.state", Matchers.equalToIgnoringCase(sendState)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.cascadeChainLink.message.type", Matchers.equalToIgnoringCase(MessageType.SMS.name())))
                .andExpect(MockRestRequestMatchers.jsonPath("$.cascadeChainLink.message.data.text", Matchers.equalToIgnoringCase(body)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.cascadeChainLink.message.data.serviceNumber", Matchers.equalToIgnoringCase(serviceNumberSms)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.cascadeChainLink.message.data.ttl", Matchers.equalTo(messageTtl)))
                .andExpect(MockRestRequestMatchers.jsonPath("$.cascadeChainLink.message.data.ttlUnit", Matchers.equalTo(ttlUnit)))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        mailProcessingService.process(getMessage(subject, body, email));
        server.verify();
    }

    @Test
    public void errorDestAddrTest() {
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        String user = GreenMailUtil.random();
        String mail = user.concat("@localhost");
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> mailProcessingService.process(getMessage(subject, body, mail)),
                String.format("Wrong destAddr value fom message, username: %s", user)
        );
    }

    @Test
    public void errorSendTest() {
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        Assertions.assertThrows(AssertionError.class, () -> mailProcessingService.process(getMessage(subject, body, email)));
    }

    private MimeMessage getMessage(String sub, String body, String mail) throws MessagingException {
        IMAPStore store = greenMail.getImap().createStore();
        store.connect(userName, pass);
        prepareFolders(store);
        MimeMessage message = GreenMailUtil.createTextEmail(mail, "aaa@google.com", sub, body, greenMail.getImap().getServerSetup()); // Construct message
        user.deliver(message);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        return (MimeMessage) folder.getMessages()[0];
    }

    private static void prepareFolders(IMAPStore store) {
        List<String> needFoldersNames = Arrays.asList(successFolder, errorFolder);
        try {
            needFoldersNames.forEach(folderName -> {
                try {
                    Folder folder = store.getFolder(folderName);
                    if (!folder.exists()) {
                        if (folder.create(Folder.HOLDS_FOLDERS)) {
                            folder.setSubscribed(true);
                        }
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
