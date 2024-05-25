package com.zgamelogic.services;

import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.database.planData.user.PlanUserRepository;
import com.zgamelogic.data.intermediates.planData.PlanEvent;
import com.zgamelogic.data.plan.PlanCreationData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@DiscordController
@Slf4j
public class PlanService {

    @Value("${discord.guild}")
    private String discordGuildId;
    @Bot
    private JDA bot;

    private final PlanRepository planRepository;
    private final PlanUserRepository planUserRepository;

    public PlanService(PlanRepository planRepository, PlanUserRepository planUserRepository) {
        this.planRepository = planRepository;
        this.planUserRepository = planUserRepository;
    }

    public void createPlan(PlanCreationData planData){

    }

    public void invitePlayersToPlan(long planId, long...players){}
    public void editPlan(long planId){}
    public void deletePlan(){}
    public void processEvent(PlanEvent event){}

    private boolean isDiscordUser(long userId){
        return bot.getGuildById(discordGuildId).getMemberById(userId) != null;
    }
}
