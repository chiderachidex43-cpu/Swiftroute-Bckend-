
package com.swiftroute.backend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String senderEmail;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EmailService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void sendOtpEmail(
            String receiverEmail,
            String receiverName,
            String otp) {

        try {

            // 1. Build the email content
            String emailContent =
                    "Hello " + receiverName + ",\n\n" +
                    "Your SwiftRoute delivery is ready for confirmation.\n\n" +
                    "Your delivery verification code is: " + otp + "\n\n" +
                    "This code expires in 5 minutes.\n\n" +
                    "Please share this code with your delivery rider " +
                    "to confirm receipt.\n\n" +
                    "Thank you for choosing SwiftRoute!";

            // 2. Build the Brevo API JSON request body
            Map<String, Object> emailBody = Map.of(
                    "sender", Map.of(
                            "name", "SwiftRoute Logistics",
                            "email", senderEmail
                    ),
                    "to", List.of(
                            Map.of(
                                    "email", receiverEmail,
                                    "name", receiverName
                            )
                    ),
                    "subject",
                    "Your SwiftRoute Delivery Verification Code",
                    "textContent",
                    emailContent
            );

            String jsonBody = objectMapper.writeValueAsString(emailBody);

            // 3. Build the HTTPS request to Brevo
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.brevo.com/v3/smtp/email"
                    ))
                    .timeout(Duration.ofSeconds(30))
                    .header("api-key", brevoApiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(
                            HttpRequest.BodyPublishers.ofString(jsonBody)
                    )
                    .build();

            // 4. Send the email
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println(
                    "SwiftRoute Brevo API HTTP status: " +
                    response.statusCode()
            );

            // 5. Check whether Brevo accepted the request
            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Brevo API failed. HTTP " +
                        response.statusCode() + ": " +
                        response.body()
                );
            }

            System.out.println(
                    "SwiftRoute OTP email accepted by Brevo."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "SwiftRoute email operation was interrupted."
            );

            throw new IllegalStateException(
                    "Email sending was interrupted.", e
            );

        } catch (Exception e) {

            System.err.println(
                    "========== SWIFTROUTE EMAIL ERROR =========="
            );

            e.printStackTrace();

            System.err.println(
                    "============================================"
            );

            throw new IllegalStateException(
                    "Failed to send SwiftRoute OTP email.", e
            );
        }
    }
}