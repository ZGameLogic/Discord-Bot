package com.zgamelogic.data.plan;

import net.dv8tion.jda.api.entities.Mentions;

public record PlanModalData(String title, String notes, String count, String date, Mentions people) {}