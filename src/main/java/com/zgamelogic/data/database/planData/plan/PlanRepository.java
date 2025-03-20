package com.zgamelogic.data.database.planData.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query("SELECT p FROM Plan p WHERE p.authorId = :authorId AND (p.date IS NULL OR p.date > :date) AND p.deleted = false")
    List<Plan> findAllPlansByAuthorId(@Param("authorId") long authorId, @Param("date") Date date);

    @Query("SELECT p FROM Plan p JOIN p.invitees pu WHERE pu.id.userId = :userId AND (p.date IS NULL OR p.date > :date) AND pu.userStatus <> 'DECLINED' AND p.deleted = false")
    List<Plan> findAllPlansByUserId(@Param("userId") long userId, @Param("date") Date date);

    @Query("SELECT p FROM Plan p WHERE (p.date IS NULL OR CAST(p.date AS date) = CAST(:date AS date)) AND p.count > (SELECT COUNT(pu) FROM PlanUser pu WHERE pu.plan = p AND pu.userStatus = 'ACCEPTED') AND p.deleted = false")
    List<Plan> findAllPlansByDateWithAvailableSpots(Date date);

    @Query("SELECT p FROM Plan p WHERE " +
            "p.date IS NULL OR (" +
            "YEAR(p.date) = YEAR(:date) " +
            "AND MONTH(p.date) = MONTH(:date) " +
            "AND DAY(p.date) = DAY(:date) " +
            "AND HOUR(p.date) = HOUR(:date) " +
            "AND MINUTE(p.date) = MINUTE(:date))" +
            "AND p.deleted = false"
    )
    List<Plan> getPlansByTime(Date date);

    @Query("SELECT p FROM Plan p WHERE (p.date IS NULL OR p.date > :date) AND p.deleted = false")
    List<Plan> findAllPlansByDateAfterAndNotDeleted(Date date);

    @Query("SELECT p FROM Plan p WHERE p.authorId = :authorId AND p.date IS NOT NULL AND p.date > :startDate AND p.date < :endDate AND p.deleted = false")
    List<Plan> findAllPlansByAuthorIdBetweenDates(@Param("authorId") long authorId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
