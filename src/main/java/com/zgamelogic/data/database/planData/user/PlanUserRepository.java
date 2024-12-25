package com.zgamelogic.data.database.planData.user;

import com.zgamelogic.data.intermediates.planData.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanUserRepository extends JpaRepository<PlanUser, PlanUser.PlanUserId> {
    @Query("SELECT new com.zgamelogic.data.intermediates.planData.UserStats(" +
            "SUM(CASE WHEN p.userStatus = 'ACCEPTED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.userStatus = 'DECLINED' THEN 1 ELSE 0 END)) " +
            "FROM PlanUser p WHERE p.id.userId = :userId GROUP BY p.id.userId")
    UserStats findUserStatusCounts(@Param("userId") long userId);
}