package com.zgamelogic.data.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public record PlanEventResultMessage(boolean success, String message) {

    public static PlanEventResultMessage success(String message) {
        return new PlanEventResultMessage(true, message);
    }

    public static PlanEventResultMessage failure(String message) {
        return new PlanEventResultMessage(false, message);
    }

    @JsonIgnore
    public HttpStatus getStatus() {
        return success ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
    }
}