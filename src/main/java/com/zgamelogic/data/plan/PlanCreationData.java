package com.zgamelogic.data.plan;

import java.util.Date;
import java.util.List;

public record PlanCreationData(
        String title,
        String notes,
        Date date,
        Long author,
        List<Long> players,
        List<Long> roles,
        int count,
        Long pollId,
        Long pollChannelId
) {}
