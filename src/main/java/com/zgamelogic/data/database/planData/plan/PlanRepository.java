package com.zgamelogic.data.database.planData.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface PlanRepository extends JpaRepository<Plan, Long> {}
