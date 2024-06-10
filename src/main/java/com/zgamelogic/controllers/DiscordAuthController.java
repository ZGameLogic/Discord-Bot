package com.zgamelogic.controllers;

import com.zgamelogic.data.authData.DiscordLoginPayload;
import com.zgamelogic.data.authData.DiscordToken;
import com.zgamelogic.data.authData.DiscordUser;
import com.zgamelogic.data.database.authData.AuthData;
import com.zgamelogic.data.database.authData.AuthDataRepository;
import com.zgamelogic.services.DiscordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class DiscordAuthController {
    private final AuthDataRepository authDataRepository;
    private final DiscordService discordService;

    public DiscordAuthController(AuthDataRepository authDataRepository, DiscordService discordService) {
        this.authDataRepository = authDataRepository;
        this.discordService = discordService;
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
            if(authData.isPresent()){
                return ResponseEntity.ok(new DiscordLoginPayload(DiscordToken.fromAuthData(authData.get()), userOptional.get()));
            }
        } else { // no user data means it's not valid, refresh the token
            if(authData.isPresent()){ // we have auth data
                Optional<DiscordToken> newDiscordToken = discordService.refreshToken(authData.get().getRefreshToken());
                if(newDiscordToken.isPresent()){ // we have a new token
                    userOptional = discordService.getUserFromToken(newDiscordToken.get().access_token());
                    if(userOptional.isPresent()){ // we are able to get user information
                        AuthData data = new AuthData(newDiscordToken.get(), userOptional.get(), device);
                        authDataRepository.save(data);
                        return ResponseEntity.ok(new DiscordLoginPayload(newDiscordToken.get(), userOptional.get()));
                    }
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
