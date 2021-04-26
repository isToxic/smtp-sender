package com.zgr.mail.sender;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class MailSenderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MailSenderApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
