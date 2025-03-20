package com.zgamelogic.controllers;

import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.intermediates.planData.CreatePlanData;
import com.zgamelogic.data.plan.PlanCreationData;
import com.zgamelogic.data.plan.PlanEventResultMessage;
import com.zgamelogic.discord.auth.data.authData.DiscordUser;
import com.zgamelogic.services.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class PlannerController {
    private final PlanService planService;
    private final PlanRepository planRepository;

    public PlannerController(PlanService planService, PlanRepository planRepository) {
        this.planService = planService;
        this.planRepository = planRepository;
    }

    @GetMapping("plans")
    private ResponseEntity<List<Plan>> getPlans(DiscordUser discordUser) {
        LocalDateTime dateTimeAnHourAgo = LocalDateTime.now().minusHours(1);
        Date dateAnHourAgo = Date.from(dateTimeAnHourAgo.atZone(ZoneId.systemDefault()).toInstant());
        List<Plan> plans = planRepository.findAllPlansByUserId(discordUser.id(), dateAnHourAgo);
        plans.addAll(planRepository.findAllPlansByAuthorId(discordUser.id(), dateAnHourAgo));
        return ResponseEntity.ok(plans);
    }

    @PostMapping("plans")
    private ResponseEntity<Plan> createPlan(@RequestBody CreatePlanData planData, DiscordUser discordUser){
        PlanCreationData planCreationData = new PlanCreationData(
                planData.title(),
                planData.notes(),
                planData.startTime(),
                discordUser.id(),
                planData.userInvitees(),
                planData.roleInvitees(),
                planData.count(),
                null,
                null
        );
        Plan plan = planService.createPlan(planCreationData);
        if(plan == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(plan);
    }

    @PostMapping("plans/{planId}/accept")
    private ResponseEntity<PlanEventResultMessage> acceptPlan(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.accept(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/requestFillIn")
    private ResponseEntity<PlanEventResultMessage> requestPlanFillIn(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.requestFillIn(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/fillIn")
    private ResponseEntity<PlanEventResultMessage> fillInPlan(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.fillIn(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/dropOut")
    private ResponseEntity<PlanEventResultMessage> dropOutPlan(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.dropOut(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/waitlist")
    private ResponseEntity<PlanEventResultMessage> waitlistPlan(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.waitList(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/maybe")
    private ResponseEntity<PlanEventResultMessage> maybePlan(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.maybe(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/deny")
    private ResponseEntity<PlanEventResultMessage> denyPlan(@PathVariable long planId, DiscordUser discordUser){
        PlanEventResultMessage result = planService.deny(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/plans/{planId}/message")
    private ResponseEntity<PlanEventResultMessage> sendMessage(
            @PathVariable long planId,
            DiscordUser discordUser,
            @RequestBody String message
    ){
        PlanEventResultMessage result = planService.sendMessage(planId, discordUser.id(), message);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @DeleteMapping("plans/{planId}")
    private ResponseEntity<?> deletePlan(@PathVariable long planId, DiscordUser discordUser){
        planService.deletePlan(planId);
        PlanEventResultMessage message = PlanEventResultMessage.success("Event canceled");
        return ResponseEntity.status(message.getStatus()).body(message);
    }
}
