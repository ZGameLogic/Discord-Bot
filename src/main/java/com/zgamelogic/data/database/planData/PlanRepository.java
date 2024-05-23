package com.zgamelogic.data.database.planData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PlanRepository extends JpaRepository<Plan, Long> {
    @Query("SELECT p FROM Plan p WHERE p.date > CURRENT_TIMESTAMP AND p.authorId = :userId")
    List<Plan> findPlansByAuthorAndFutureDate(@Param("userId") Long userId);

    @Query(value = """
            SELECT p.* FROM Plans p
            JOIN Plan_Player_Status ps ON p.id = ps.example_id
            WHERE p.date > CURRENT_TIMESTAMP AND ps.Plan_Player_Status_Id = :userId""", nativeQuery = true)
    List<Plan> findPlansByInviteeAndFutureDate(@Param("userId") Long userId);
}
