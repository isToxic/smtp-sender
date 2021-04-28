package com.zgr.smtp.sender.config;

import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.zgr.smtp.sender.SmtpSenderApplication;
import com.zgr.smtp.sender.service.MailProcessingService;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = SmtpSenderApplication.class)
@Import(IMAPIdleConfigurationTest.TestConfig.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IMAPIdleConfigurationTest {
    private static GreenMail greenMail;
    private static GreenMailUser user;
    private static boolean inMock = false;

    private static final String userName = GreenMailUtil.random();
    private static final String password = GreenMailUtil.random();
    private static final String email = "7".concat(RandomStringUtils.randomNumeric(10)).concat("@localhost");
    private static final String successFolder = GreenMailUtil.random();
    private static final String errorFolder = GreenMailUtil.random();

    @BeforeAll
    static void setUpAll() {
        greenMail = new GreenMail(ServerSetupTest.IMAP.dynamicPort());
        greenMail.start();
        user = greenMail.setUser("all_mail@localhost", userName, password);
        System.setProperty("mail-sender.mail.host", "localhost");
        System.setProperty("mail-sender.mail.port", String.valueOf(greenMail.getImap().getPort()));
        System.setProperty("mail-sender.mail.user-name", userName);
        System.setProperty("mail-sender.mail.password", password);
        System.setProperty("mail-sender.mail.folders.success", successFolder);
        System.setProperty("mail-sender.mail.folders.error", errorFolder);
    }

    @Test
    public void receiveEmailsTest() {
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        MimeMessage message = GreenMailUtil.createTextEmail(email, "aaa@google.com", subject, body, greenMail.getImap().getServerSetup()); // Construct message
        user.deliver(message);
        await().atMost(10, TimeUnit.SECONDS).until(() -> inMock);
        assertTrue(inMock);
    }

    @Test
    public void prepareFoldersTest() {
        List<String> expectedMails = Lists.list(errorFolder, successFolder);
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        MimeMessage message = GreenMailUtil.createTextEmail(email, "aaa@google.com", subject, body, greenMail.getImap().getServerSetup()); // Construct message
        user.deliver(message);
        await().atMost(10, TimeUnit.SECONDS).until(() -> inMock);
        assertTrue(inMock);
        assertTrue(Try.of(() -> greenMail.getManagers()
                .getImapHostManager()
                .getStore()
                .listMailboxes("*")
        ).get().stream()
                .map(MailFolder::getName)
                .collect(Collectors.toList())
                .containsAll(expectedMails));
    }

    @AfterAll
    static void after() {
        greenMail.stop();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public MailProcessingService mailProcessingServiceTest() {
            MailProcessingService std = mock(MailProcessingService.class);
            doAnswer(invocation -> {
                inMock = true;
                return invocation.getArgument(0);
            })
                    .when(std).process(any(MimeMessage.class));
            return std;
        }
    }
}
