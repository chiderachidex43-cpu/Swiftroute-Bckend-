
package com.swiftroute.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${swiftroute.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String receiverEmail, String receiverName, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(receiverEmail);
        message.setSubject("Your SwiftRoute Delivery Verification Code");

        message.setText(
                "Hello " + receiverName + ",\n\n" +
                "Your SwiftRoute delivery is ready for confirmation.\n\n" +
                "Your delivery verification code is: " + otp + "\n\n" +
                "This code expires in 5 minutes.\n\n" +
                "Please share this code with your delivery rider to confirm receipt.\n\n" +
                "Thank you for choosing SwiftRoute!"
        );

        mailSender.send(message);
    }
}
