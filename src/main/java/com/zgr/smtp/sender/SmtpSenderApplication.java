package com.zgr.smtp.sender;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SmtpSenderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SmtpSenderApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
