package com.zgamelogic.data.authData;

import com.zgamelogic.data.database.authData.AuthData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record DiscordToken(
        String token_type,
        String access_token,
        Long expires_in,
        String refresh_token,
        String scope
) {
    public static DiscordToken fromAuthData(AuthData authData) {
        return new DiscordToken(
                authData.getTokenType(),
                authData.getToken(),
                Instant.now().until(authData.getExpires(), ChronoUnit.SECONDS),
                authData.getRefreshToken(),
                authData.getScope()
        );
    }
}