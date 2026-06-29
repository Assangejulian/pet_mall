package com.pat.user.utils;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
public class MailUtils {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            mailSender.send(message);
            log.info("邮件发送成功，收件人: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败，收件人: {}", to, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    public static String generateCode(int length) {
        SecureRandom random = new SecureRandom();
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        return String.valueOf(random.nextInt(max - min + 1) + min);
    }
}