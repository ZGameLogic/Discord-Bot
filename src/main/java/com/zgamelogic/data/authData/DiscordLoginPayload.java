package com.zgamelogic.data.authData;

public record DiscordLoginPayload(
        DiscordToken token,
        DiscordUser user
) {}
