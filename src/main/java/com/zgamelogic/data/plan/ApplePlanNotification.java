package com.zgamelogic.data.plan;

import lombok.Data;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.StdSerializer;

@Data
@JsonSerialize(using = ApplePlanNotification.ApplePlanNotificationSerializer.class)
public class ApplePlanNotification {

    private final String title;
    private final String subtitle;
    private final String body;

    public static ApplePlanNotification PlanInvite(String author, String planTitle){
        return new ApplePlanNotification(author + " has invited you to a plan.", planTitle, "Respond in the app.");
    }

    public static ApplePlanNotification PlanAccepted(String title, String username) {
        return new ApplePlanNotification(username + " has accepted your plan.", title, null);
    }

    public static ApplePlanNotification PlanRemind(String title){
        return new ApplePlanNotification(title, "You are receiving this notification because this plan is still not filled and you \"maybed\" the plan.", null);
    }

    protected static class ApplePlanNotificationSerializer extends StdSerializer<ApplePlanNotification> {
        protected ApplePlanNotificationSerializer() { super(ApplePlanNotification.class); }

        @Override
        public void serialize(ApplePlanNotification notification, tools.jackson.core.JsonGenerator gen, SerializationContext provider) throws JacksonException {
            gen.writeStartObject();
            gen.writeObjectPropertyStart("aps");
            gen.writeStringProperty("sound", "bingbong.aiff");
            gen.writeObjectPropertyStart("alert");
            gen.writeStringProperty("title", notification.getTitle());

            if (notification.getSubtitle() != null) {
                gen.writeStringProperty("subtitle", notification.getSubtitle());
            }

            if (notification.getBody() != null) {
                gen.writeStringProperty("body", notification.getBody());
            }

            gen.writeEndObject(); // alert
            gen.writeEndObject(); // aps
            gen.writeEndObject();
        }
    }
}
