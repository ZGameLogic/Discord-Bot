package com.zgamelogic.data.intermediates.planData;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.zgamelogic.data.database.planData.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
        ACCEPT(0b1000_0000_0000),
        MAYBE(0b0100_0000_0000),
        DENY(0b0010_0000_0000),
        DROPOUT(0b0001_0000_0000),
        WAITLIST(0b0000_1000_0000),
        FILLIN(0b0000_0100_0000),
        REQUEST_FILLIN(0b0000_0010_0000),
        EDIT_EVENT(0b0000_0000_1000),
        DELETE(0b0000_0000_0100),
        SEND_MESSAGE(0b0000_0000_0010),
        SCHEDULE_REMINDER(0b0000_0000_0001);
        private final int mask;
    }
}
