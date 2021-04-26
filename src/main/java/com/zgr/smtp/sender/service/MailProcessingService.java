package com.zgr.smtp.sender.service;

import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public interface MailProcessingService {
    void process(MimeMessage message);
}
