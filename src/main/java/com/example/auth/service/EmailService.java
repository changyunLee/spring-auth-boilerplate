package com.example.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        Context ctx = new Context();
        ctx.setVariable("link", frontendUrl + "/verify-email.html?token=" + token);
        ctx.setVariable("to", to);
        sendHtml(to, "[Spring Auth] 이메일 인증", "email/verification", ctx);
    }

    @Async
    public void send2FACodeEmail(String to, String code) {
        Context ctx = new Context();
        ctx.setVariable("code", code);
        ctx.setVariable("to", to);
        sendHtml(to, "[Spring Auth] 2차 인증 코드", "email/two-factor-code", ctx);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        Context ctx = new Context();
        ctx.setVariable("link", frontendUrl + "/reset-password.html?token=" + token);
        ctx.setVariable("to", to);
        sendHtml(to, "[Spring Auth] 비밀번호 재설정", "email/password-reset", ctx);
    }

    private void sendHtml(String to, String subject, String template, Context ctx) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, ctx), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        }
    }
}
