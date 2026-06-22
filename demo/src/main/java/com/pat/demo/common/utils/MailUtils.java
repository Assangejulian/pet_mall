package com.pat.demo.common.utils;//package com.artsail.common.utils;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Component;
//
//
//import java.security.SecureRandom;
//import java.util.Random;
//
//
//@Slf4j
//@Component
//public class MailUtils {
//
//    // 使用更安全的随机数生成器
//    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String from;
//
//    /**
//     * 发送邮件 - 基础方法
//     * @param to 收件人邮箱
//     * @param subject 邮件主题
//     * @param content 邮件内容（支持HTML）
//     * @param isHtml 是否为HTML内容
//     */
//    public void sendMail(String to, String subject, String content, boolean isHtml) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(content, isHtml);
//
//            mailSender.send(message);
//
//            log("邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
//
//        } catch (MessagingException e) {
//            log.error("邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
//            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("邮件发送异常 - 收件人: {}, 错误: {}", to, e.getMessage());
//            throw new RuntimeException("邮件发送异常", e);
//        }
//    }
//
//
//    public static String generateCode() {
//        // 生成6位随机数字
//        Random random = new Random();
//        return String.format("%06d", random.nextInt(1000000));
//    }
//
//    /**
//     * 生成指定长度的数字验证码
//     */
//    public String generateCode(int length) {
//        if (length <= 0) {
//            throw new IllegalArgumentException("验证码长度必须大于0");
//        }
//
//        int maxValue = (int) Math.pow(10, length) - 1;
//        int minValue = (int) Math.pow(10, length - 1);
//
//        int code = SECURE_RANDOM.nextInt(maxValue - minValue + 1) + minValue;
//        return String.valueOf(code);
//    }
//
//    /**
//     * 生成字母数字混合验证码（可选）
//     */
//    public String generateAlphanumericCode(int length) {
//        String characters = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
//        StringBuilder code = new StringBuilder(length);
//
//        for (int i = 0; i < length; i++) {
//            int index = SECURE_RANDOM.nextInt(characters.length());
//            code.append(characters.charAt(index));
//        }
//
//        return code.toString();
//    }
//
//}