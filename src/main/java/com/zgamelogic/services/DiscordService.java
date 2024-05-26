package com.zgamelogic.services;

import com.zgamelogic.data.authData.DiscordToken;
import com.zgamelogic.data.authData.DiscordUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Slf4j
public class DiscordService {
    @Value("${client.id}") private String discordClientId;
    @Value("${client.secret}") private String discordClientSecret;
    @Value("${redirect.url}") private String discordRedirectUrl;

    public Optional<DiscordToken> postForToken(String code) {
        String url = "https://discord.com/api/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Accept-Encoding", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", discordClientId);
        requestBody.add("client_secret", discordClientSecret);
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", discordRedirectUrl);
        try {
            ResponseEntity<DiscordToken> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), DiscordToken.class);
            return Optional.of(response.getBody());
        } catch (Exception e){
            log.error("Unable to post for token", e);
            return Optional.empty();
        }
    }

    public Optional<DiscordToken> refreshToken(String refreshToken){
        String url = "https://discord.com/api/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Accept-Encoding", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", discordClientId);
        requestBody.add("client_secret", discordClientSecret);
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("refresh_token", refreshToken);
        try {
            ResponseEntity<DiscordToken> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), DiscordToken.class);
            return Optional.of(response.getBody());
        } catch (Exception e) {
            log.error("Unable to refresh token", e);
            return Optional.empty();
        }
    }

    public Optional<DiscordUser> getUserFromToken(String token){
        String url = "https://discord.com/api/users/@me";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        try {
            ResponseEntity<DiscordUser> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), DiscordUser.class);
            return Optional.of(response.getBody());
        } catch(HttpClientErrorException.Unauthorized ignored) {
            return Optional.empty();
        } catch(Exception e) {
            log.error("Unable to get user from token", e);
            return Optional.empty();
        }
    }
}
