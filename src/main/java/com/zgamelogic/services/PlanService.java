package com.zgamelogic.services;

import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.data.intermediates.planData.PlanEvent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.stereotype.Service;

@Service
@DiscordController
@Slf4j
public class PlanService {
    @Bot
    private JDA bot;

    @DiscordMapping
    private void onReady(ReadyEvent event){

    }

    public void createPlan(){}
    public void editPlan(){}
    public void deletePlan(){}
    public void handelEvent(PlanEvent event){}
}
