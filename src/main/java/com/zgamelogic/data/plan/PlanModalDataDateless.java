package com.zgamelogic.data.plan;

public record PlanModalDataDateless(String title, String notes, String count, String poll) {
    public Long getPollId(){
        return Long.parseLong(poll.split("-")[1]);
    }

    public Long getPollChannelId(){
        return Long.parseLong(poll.split("-")[0]);
    }
}
