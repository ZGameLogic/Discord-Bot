package com.zgamelogic.controllers;

import com.zgamelogic.data.authData.DiscordLoginPayload;
import com.zgamelogic.data.authData.DiscordToken;
import com.zgamelogic.data.authData.DiscordUser;
import com.zgamelogic.data.database.authData.AuthData;
import com.zgamelogic.data.database.authData.AuthDataRepository;
import com.zgamelogic.services.DiscordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@Slf4j
public class DiscordAuthController {
    private final AuthDataRepository authDataRepository;
    private final DiscordService discordService;

    public DiscordAuthController(AuthDataRepository authDataRepository, DiscordService discordService) {
        this.authDataRepository = authDataRepository;
        this.discordService = discordService;
    }

    @PostMapping("/devices/register/{deviceId}/{token}")
    public ResponseEntity<?> registerDevice(@PathVariable String deviceId, @PathVariable String token) {
        Optional<AuthData> data = authDataRepository.findById_DeviceId(deviceId);
        if (data.isPresent()) {
            data.get().setAppleNotificationId(token);
            authDataRepository.save(data.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("auth/login")
    private ResponseEntity<DiscordLoginPayload> login(
            @RequestParam String code,
            @RequestParam String device
    ){
        Optional<DiscordToken> token = discordService.postForToken(code);
        if(token.isEmpty()) return ResponseEntity.badRequest().build();
        Optional<DiscordUser> user = discordService.getUserFromToken(token.get().access_token());
        if(user.isEmpty()) return ResponseEntity.badRequest().build();
        AuthData data = new AuthData(token.get(), user.get(), device);
        authDataRepository.save(data);
        return ResponseEntity.ok(new DiscordLoginPayload(token.get(), user.get()));
    }

    @PostMapping("auth/relogin")
    private ResponseEntity<DiscordLoginPayload> login(
            @RequestParam long userId,
            @RequestParam String token,
            @RequestParam String device
    ){
        Optional<DiscordUser> userOptional = discordService.getUserFromToken(token);
        Optional<AuthData> authData = authDataRepository.findById_DiscordIdAndId_DeviceIdAndToken(userId, device, token);
        if(userOptional.isPresent()){ // if we get user data, its valid
            if(authData.isPresent()){ // if we have auth data, its valid
                Duration durationBetween = Duration.between(Instant.now(), authData.get().getExpires());
                boolean isLessThanADay = durationBetween.compareTo(Duration.ofDays(1)) < 0;
                if(isLessThanADay){ // if the token expires in less than a day
                    DiscordToken newToken = discordService.refreshToken(authData.get().getRefreshToken()).get();
                    AuthData newAuth = new AuthData(newToken, discordService.getUserFromToken(newToken.access_token()).get(), device);
                    authDataRepository.save(newAuth);
                    return ResponseEntity.ok(new DiscordLoginPayload(newToken, userOptional.get()));
                }
                return ResponseEntity.ok(new DiscordLoginPayload(DiscordToken.fromAuthData(authData.get()), userOptional.get()));
            }
        }
        return ResponseEntity.badRequest().build();
    }
}