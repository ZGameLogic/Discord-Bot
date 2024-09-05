package com.zgamelogic.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.data.plan.ApplePlanLiveNotification;
import com.zgamelogic.data.plan.ApplePlanNotification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import okhttp3.OkHttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ApplePushNotificationService {
    @Value("${kid}")    private String kid;
    @Value("${org_id}") private String orgId;
    @Value("${APN}")    private String apnEndpoint;

    @PostConstruct
    public void init() {
        log.info("AuthKey_{} : {}", kid, new File("apns/AuthKey_" + kid + ".p8").exists());
        sendLiveNotificationStart("eb50fd9eeec631eefe9424eb60c1838a33babc865a43b82cc71f1c92821980f1", new ApplePlanLiveNotification());
    }

    public void sendLiveNotificationStart(String device, ApplePlanLiveNotification notification){
        String url = apnEndpoint + "/3/device/" + device;
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", "bearer " + authJWT());
        headers.add("apns-push-type", "liveactivity");
        headers.add("apns-priority", "10");
        headers.add("apns-expiration", "0");
        headers.add("apns-topic", "zgamelogic.Planner-Bot.push-type.liveactivity");
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(objectMapper.writeValueAsString(notification), headers), String.class);
        } catch (HttpClientErrorException e) {
            log.error("APNs error response: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }

    public void sendNotification(String device, ApplePlanNotification notification){
        String url = apnEndpoint + "/3/device/" + device;
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", "bearer " + authJWT());
        headers.add("apns-push-type", "alert");
        headers.add("apns-priority", "10");
        headers.add("apns-expiration", "0");
        headers.add("apns-topic", "zgamelogic.Planner-Bot");
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(objectMapper.writeValueAsString(notification), headers), String.class);
        } catch (HttpClientErrorException e) {
            log.error("APNs error response: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }

    private String authJWT() {
        try {
            String privateKeyPEM = new String(Files.readAllBytes(new File("apns/AuthKey_" + kid + ".p8").toPath()));
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", ""); // Remove any whitespaces or newlines

            byte[] decodedKey = org.bouncycastle.util.encoders.Base64.decode(privateKeyPEM);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            return Jwts.builder()
                    .setIssuer(orgId)
                    .setIssuedAt(new Date())
                    .signWith(privateKey, SignatureAlgorithm.ES256)
                    .setHeaderParam("kid", kid)
                    .compact();
        } catch (Exception e){
            log.error("Unable to create JWT", e);
            return "";
        }
    }
}
