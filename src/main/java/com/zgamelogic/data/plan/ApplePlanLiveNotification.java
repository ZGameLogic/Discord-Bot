package com.zgamelogic.data.plan;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize
public class ApplePlanLiveNotification {
    @JsonValue
    public void serialize(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("aps");
        gen.writeStringField("event", "start");
        gen.writeNumberField("timestamp", System.currentTimeMillis());
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
