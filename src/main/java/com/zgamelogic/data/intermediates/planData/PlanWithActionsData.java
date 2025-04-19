package com.zgamelogic.data.intermediates.planData;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.zgamelogic.data.database.planData.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public class PlanWithActionsData {
    @JsonUnwrapped
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
}
