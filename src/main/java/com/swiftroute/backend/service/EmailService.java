
package com.swiftroute.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Value("${GMAIL_CLIENT_ID}")
    private String clientId;

    @Value("${GMAIL_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GMAIL_REFRESH_TOKEN}")
    private String refreshToken;

    @Value("${GMAIL_SENDER_EMAIL}")
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
            // 1. Obtain a fresh Gmail access token
            String accessToken = getAccessToken();

            System.out.println(
                    "SwiftRoute: Gmail access token obtained successfully."
            );

            // 2. Build the OTP email
            Properties properties = new Properties();
            Session session = Session.getInstance(properties);

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(senderEmail));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(receiverEmail, true)
            );

            message.setSubject(
                    "Your SwiftRoute Delivery Verification Code",
                    StandardCharsets.UTF_8.name()
            );

            message.setText(
                    "Hello " + receiverName + ",\n\n" +
                    "Your SwiftRoute delivery is ready for confirmation.\n\n" +
                    "Your delivery verification code is: " + otp + "\n\n" +
                    "This code expires in 5 minutes.\n\n" +
                    "Please share this code with your delivery rider " +
                    "to confirm receipt.\n\n" +
                    "Thank you for choosing SwiftRoute!",
                    StandardCharsets.UTF_8.name()
            );

            message.saveChanges();

            // 3. Encode the email for the Gmail API
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            message.writeTo(output);

            String rawEmail = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(output.toByteArray());

            // 4. Build the Gmail API JSON request
            String jsonBody = objectMapper.writeValueAsString(
                    Map.of("raw", rawEmail)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"
                    ))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // 5. Send the email
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println(
                    "SwiftRoute Gmail API HTTP status: " +
                    response.statusCode()
            );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Gmail API failed. HTTP " +
                        response.statusCode() + ": " +
                        response.body()
                );
            }

            System.out.println(
                    "SwiftRoute OTP email sent successfully."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "SwiftRoute email operation was interrupted."
            );

            e.printStackTrace();

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

    private String getAccessToken()
            throws IOException, InterruptedException {

        // 1. Prepare Google's OAuth refresh-token request
        String form = "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&refresh_token=" + encode(refreshToken)
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://oauth2.googleapis.com/token"
                ))
                .timeout(Duration.ofSeconds(30))
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                )
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        // 2. Ask Google for an access token
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IllegalStateException(
                    "Google token request failed. HTTP " +
                    response.statusCode() + ": " +
                    response.body()
            );
        }

        // 3. Read Google's JSON response using Jackson
        JsonNode json = objectMapper.readTree(response.body());

        String accessToken = json.path("access_token").asText("");

        if (accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Google did not return an access token."
            );
        }

        return accessToken;
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
