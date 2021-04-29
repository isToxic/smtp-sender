package com.zgr.smtp.sender;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties
@PropertySource("file:config/application.yml")
public class SmtpSenderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SmtpSenderApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
