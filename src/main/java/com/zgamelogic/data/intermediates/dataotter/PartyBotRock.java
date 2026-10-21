package com.zgamelogic.data.intermediates.dataotter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.UUID;

@Getter
@AllArgsConstructor
@JsonSerialize(using = PartyBotRock.PartyRockSerializer.class)
public class PartyBotRock {
    private final String ROCK_TYPE = "party bot";
    private final long userId;
    private final boolean joined;
    private final UUID session;

    public static class PartyRockSerializer extends JsonSerializer<PartyBotRock> {
        @Override
        public void serialize(PartyBotRock value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("rock type", "party bot");
            gen.writeNumberField("userId", value.userId);
            gen.writeBooleanField("joined", value.joined);
            gen.writeStringField("session", value.session.toString());
            gen.writeEndObject();
        }
    }
}
