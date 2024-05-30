package com.zgamelogic.controllers;

import com.zgamelogic.data.authData.DiscordUser;
import com.zgamelogic.data.database.authData.AuthData;
import com.zgamelogic.data.database.authData.AuthDataRepository;
import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.intermediates.planData.CreatePlanData;
import com.zgamelogic.data.plan.PlanCreationData;
import com.zgamelogic.services.DiscordService;
import com.zgamelogic.services.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class PlannerController {

    private final AuthDataRepository authDataRepository;
    private final DiscordService discordService;
    private final PlanService planService;
    private final PlanRepository planRepository;

    public PlannerController(AuthDataRepository authDataRepository, DiscordService discordService, PlanService planService, PlanRepository planRepository) {
        this.authDataRepository = authDataRepository;
        this.discordService = discordService;
        this.planService = planService;
        this.planRepository = planRepository;
    }

    @GetMapping("plans")
    private ResponseEntity<List<Plan>> getPlans(@RequestHeader String token) {
        Optional<DiscordUser> discordUser = discordService.getUserFromToken(token);
        if(discordUser.isEmpty()) return ResponseEntity.status(401).build();
        List<Plan> plans = planRepository.findAllPlansByUserId(discordUser.get().id(), new Date());
        plans.addAll(planRepository.findAllByAuthorIdAndDateGreaterThan(discordUser.get().id(), new Date()));
        return ResponseEntity.ok(plans);
    }

    @PostMapping("plans")
    private ResponseEntity<Plan> createPlan(
            @RequestHeader String token,
            @RequestHeader String device,
            @RequestBody CreatePlanData planData
    ){
        Optional<DiscordUser> discordUser = discordService.getUserFromToken(token);
        if(discordUser.isEmpty()) return ResponseEntity.status(401).build();
        Optional<AuthData> authData = authDataRepository.findById_DiscordIdAndId_DeviceIdAndToken(discordUser.get().id(), device, token);
        if(authData.isEmpty()) return ResponseEntity.status(401).build();
        PlanCreationData planCreationData = new PlanCreationData(
                planData.title(),
                planData.notes(),
                planData.startTime(),
                discordUser.get().id(),
                planData.userInvitees(),
                planData.roleInvitees(),
                planData.count()
        );
        Plan plan = planService.createPlan(planCreationData);
        if(plan == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(plan);
    }
}
