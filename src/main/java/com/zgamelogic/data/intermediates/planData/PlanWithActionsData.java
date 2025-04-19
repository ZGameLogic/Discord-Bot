package com.zgamelogic.data.intermediates.planData;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zgamelogic.data.database.planData.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Getter
@AllArgsConstructor
@JsonSerialize(using = PlanWithActionsData.PlanWithActionDataSerialization.class)
public class PlanWithActionsData {
    private Plan plan;
    private List<PlanAction> actions;

    @Getter
    @AllArgsConstructor
    public enum PlanAction {
        ACCEPT("accept_event"),
        MAYBE("maybe_event"),
        DENY("deny_event"),
        DROPOUT("drop_out_event"),
        WAITLIST("waitlist_event"),
        FILLIN("fill_in"),
        REQUEST_FILLIN("request_fill_in"),
        EDIT_EVENT("edit_event"),
        DELETE("delete_event"),
        SEND_MESSAGE("send_message"),
        SCHEDULE_REMINDER("schedule_reminder");
        private final String id;

        public static PlanAction fromButton(Button button){
            return Arrays.stream(PlanAction.values()).filter(action -> action.getId().equals(button.getId())).findFirst().orElse(null);
        }
    }

    public static class PlanWithActionDataSerialization extends JsonSerializer<PlanWithActionsData> {

        @Override
        public void serialize(PlanWithActionsData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("id", value.getPlan().getId());
            gen.writeStringField("title", value.getPlan().getTitle());
            gen.writeStringField("notes", value.getPlan().getNotes());
            if(value.getPlan().getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String formattedDate = dateFormat.format(value.getPlan().getDate());
                gen.writeStringField("start time", formattedDate);
            }
            gen.writeNumberField("count", value.getPlan().getCount());
            gen.writeNumberField("author id", value.getPlan().getAuthorId());
            gen.writeArrayFieldStart("invitees");
            value.getPlan().getInvitees().values().forEach(planUser -> {
                try {
                    gen.writeStartObject();
                    gen.writeNumberField("user id", planUser.getId().getUserId());
                    gen.writeStringField("status", planUser.getUserStatus().name());
                    gen.writeBooleanField("needs fill in", planUser.isNeedFillIn());
                    gen.writeEndObject();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            gen.writeEndArray();
            gen.writeArrayFieldStart("actions");
            if(value.getActions() != null){
                for(PlanAction action : value.getActions()){
                    gen.writeString(action.name());
                }
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }
}
