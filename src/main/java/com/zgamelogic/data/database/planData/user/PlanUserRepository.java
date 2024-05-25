package com.zgamelogic.data.database.planData.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanUserRepository extends JpaRepository<PlanUser, PlanUser.PlanUserId> {
}
