package com.zgamelogic.data.database.planData.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findAllByAuthorIdAndDateGreaterThan(long authorId, Date date);

    @Query("SELECT p FROM Plan p JOIN p.invitees pu WHERE pu.id.userId = :userId AND p.date > :date AND pu.userStatus <> 'DECLINED'")
    List<Plan> findAllPlansByUserId(@Param("userId") long userId, @Param("date") Date date);

    @Query("SELECT p FROM Plan p WHERE CAST(p.date AS date) = CAST(:date AS date) AND p.count > (SELECT COUNT(pu) FROM PlanUser pu WHERE pu.plan = p AND pu.userStatus = 'ACCEPTED')")
    List<Plan> findAllPlansByDateWithAvailableSpots(Date date);
}