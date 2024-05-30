package com.zgamelogic.data.intermediates.planData;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public record CreatePlanData(
        @JsonProperty("start time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Date startTime,
        String title,
        String notes,
        @JsonProperty("user invites")
        List<Long> userInvitees,
        @JsonProperty("role invites")
        List<Long> roleInvitees,
        int count
) {}
