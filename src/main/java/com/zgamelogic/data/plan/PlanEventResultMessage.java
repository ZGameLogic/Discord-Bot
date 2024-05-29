package com.zgamelogic.data.plan;

public record PlanEventResultMessage(boolean success, String message) {

    public static PlanEventResultMessage success(String message) {
        return new PlanEventResultMessage(true, message);
    }

    public static PlanEventResultMessage failure(String message) {
        return new PlanEventResultMessage(false, message);
    }
}
