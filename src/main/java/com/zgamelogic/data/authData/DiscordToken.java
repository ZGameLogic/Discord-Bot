package com.zgamelogic.data.authData;

import com.zgamelogic.data.database.authData.AuthData;

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
                authData.getExpiresIn(),
                authData.getRefreshToken(),
                authData.getScope()
        );
    }
}
