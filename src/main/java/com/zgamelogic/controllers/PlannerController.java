package com.zgamelogic.controllers;

import com.zgamelogic.data.authData.DiscordUser;
import com.zgamelogic.data.database.authData.AuthData;
import com.zgamelogic.data.database.authData.AuthDataRepository;
import com.zgamelogic.data.database.planData.plan.Plan;
import com.zgamelogic.data.database.planData.plan.PlanRepository;
import com.zgamelogic.data.intermediates.planData.CreatePlanData;
import com.zgamelogic.data.plan.PlanCreationData;
import com.zgamelogic.data.plan.PlanEventResultMessage;
import com.zgamelogic.services.DiscordService;
import com.zgamelogic.services.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private ResponseEntity<List<Plan>> getPlans(@ModelAttribute DiscordUser discordUser) {
        LocalDateTime dateTimeAnHourAgo = LocalDateTime.now().minusHours(1);
        Date dateAnHourAgo = Date.from(dateTimeAnHourAgo.atZone(ZoneId.systemDefault()).toInstant());
        List<Plan> plans = planRepository.findAllPlansByUserId(discordUser.id(), dateAnHourAgo);
        plans.addAll(planRepository.findAllByAuthorIdAndDateGreaterThan(discordUser.id(), dateAnHourAgo));
        return ResponseEntity.ok(plans);
    }

    @PostMapping("plans")
    private ResponseEntity<Plan> createPlan(@RequestBody CreatePlanData planData, @ModelAttribute DiscordUser discordUser){
        PlanCreationData planCreationData = new PlanCreationData(
                planData.title(),
                planData.notes(),
                planData.startTime(),
                discordUser.id(),
                planData.userInvitees(),
                planData.roleInvitees(),
                planData.count()
        );
        Plan plan = planService.createPlan(planCreationData);
        if(plan == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(plan);
    }

    @PostMapping("plans/{planId}/accept")
    private ResponseEntity<PlanEventResultMessage> acceptPlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.accept(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/requestFillIn")
    private ResponseEntity<PlanEventResultMessage> requestPlanFillIn(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.requestFillIn(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/fillIn")
    private ResponseEntity<PlanEventResultMessage> fillInPlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.fillIn(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/dropOut")
    private ResponseEntity<PlanEventResultMessage> dropOutPlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.dropOut(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/waitlist")
    private ResponseEntity<PlanEventResultMessage> waitlistPlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.waitList(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/maybe")
    private ResponseEntity<PlanEventResultMessage> maybePlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.maybe(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("plans/{planId}/deny")
    private ResponseEntity<PlanEventResultMessage> denyPlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        PlanEventResultMessage result = planService.deny(planId, discordUser.id());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @DeleteMapping("plans/{planId}")
    private ResponseEntity<?> deletePlan(@PathVariable long planId, @ModelAttribute DiscordUser discordUser){
        planService.deletePlan(planId);
        PlanEventResultMessage message = PlanEventResultMessage.success("Event canceled");
        return ResponseEntity.status(message.getStatus()).body(message);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ModelAttribute
    public void authenticate(WebRequest request, Model model) {
        String token = request.getHeader("token");
        String device = request.getHeader("device");

        if (token == null || device == null) throw new UnauthorizedException();
        Optional<DiscordUser> discordUser = discordService.getUserFromToken(token);
        if (discordUser.isEmpty()) throw new UnauthorizedException();
        Optional<AuthData> authData = authDataRepository.findById_DiscordIdAndId_DeviceIdAndToken(discordUser.get().id(), device, token);
        if (authData.isEmpty()) throw new UnauthorizedException();

        model.addAttribute("discordUser", discordUser.get());
        model.addAttribute("authData", authData.get());
    }

    private static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException() {
            super("Unauthorized");
        }
    }
}