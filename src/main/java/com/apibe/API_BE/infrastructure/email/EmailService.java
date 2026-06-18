package com.apibe.API_BE.infrastructure.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        String body = """
                Your verification code is:

                %s

                This code will expire in 5 minutes.
                """.formatted(otp);
        sendPlainText(to, "Verify your account", body);
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        String body = """
                Click link below:

                %s
                """.formatted(resetLink);
        sendPlainText(to, "Reset your password", body);
    }

    private void sendPlainText(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

