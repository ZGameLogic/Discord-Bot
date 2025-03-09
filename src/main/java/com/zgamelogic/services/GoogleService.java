package com.zgamelogic.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GoogleService {
    private final GoogleCredentials credentials;

    public GoogleService(
        @Value("${google.client.id}") String clientId,
        @Value("${google.project.id}") String projectId,
        @Value("${google.private.key.id}") String privateKeyId,
        @Value("${google.private.key}") String privateKey,
        @Value("${google.client.cert.url}") String clientCertURL,
        @Value("${google.client.email}") String clientEmail

    ) throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("type", "service_account");
        jsonMap.put("project_id", projectId);
        jsonMap.put("private_key_id", privateKeyId);
        jsonMap.put("private_key", privateKey.replace("\\n", "\n"));
        jsonMap.put("client_email", clientEmail);
        jsonMap.put("client_id", clientId);
        jsonMap.put("auth_uri", "https://accounts.google.com/o/oauth2/auth");
        jsonMap.put("token_uri", "https://accounts.google.com/o/oauth2");
        jsonMap.put("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
        jsonMap.put("client_x509_cert_url", clientCertURL);
        jsonMap.put("universe_domain", "googleapis.com");
        String jsonString = new ObjectMapper().writeValueAsString(jsonMap);
        System.out.println(jsonString);
        credentials = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(jsonString.getBytes()));
        //credentials = ServiceAccountCredentials.fromStream(new FileInputStream("C:\\Users\\bensh\\Downloads\\discord-bot-446000-63e566273945.json"));
        log.info("Google service credentials stored");
        try {
            createCalendarEvent();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public void createCalendarEvent() throws GeneralSecurityException, IOException {
        Calendar calendar = new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
                .setApplicationName("Something")
        .build();

        Event event = new Event()
                .setSummary("My Friend's Meetup")
                .setDescription("We are meeting for dinner!");

        ZonedDateTime startDateTime = ZonedDateTime.now().plusDays(1);
        ZonedDateTime endDateTime = startDateTime.plusHours(2);

        event.setStart(new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .setTimeZone("America/Chicago"));

        event.setEnd(new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .setTimeZone("America/Chicago"));

        event = calendar.events().insert("primary", event).execute();
        System.out.println("Event created: " + event.getHtmlLink());
    }
}
