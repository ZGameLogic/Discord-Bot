package com.zgamelogic.data.database.authData;

import com.zgamelogic.data.authData.DiscordToken;
import com.zgamelogic.data.authData.DiscordUser;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;
import java.util.Date;

@Entity
@NoArgsConstructor
@ToString
@Getter
public class AuthData {
    @Id
    private AuthDataId id;
    private String token;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private Instant expires;
    @Setter
    private String appleNotificationId;

    public AuthData(DiscordToken token, DiscordUser user, String deviceId) {
        this.token = token.access_token();
        refreshToken = token.refresh_token();
        id = new AuthDataId(deviceId, user.id());
        tokenType = token.token_type();
        scope = token.scope();
        expires = new Date().toInstant().plusSeconds(token.expires_in());
    }

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class AuthDataId {
        private String deviceId;
        private Long discordId;
    }
}