package com.zgamelogic.data.database.planData.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PlanReminderRepository extends JpaRepository<PlanReminder, PlanReminder.PlanReminderId> {
    @Query("SELECT p FROM PlanReminder p WHERE " +
            "YEAR(CAST(p.id.time AS timestamp)) = YEAR(CAST(:time AS timestamp)) " +
            "AND MONTH(CAST(p.id.time AS timestamp)) = MONTH(CAST(:time AS timestamp)) " +
            "AND DAY(CAST(p.id.time AS timestamp)) = DAY(CAST(:time AS timestamp)) " +
            "AND HOUR(CAST(p.id.time AS timestamp)) = HOUR(CAST(:time AS timestamp)) " +
            "AND MINUTE(CAST(p.id.time AS timestamp)) = MINUTE(CAST(:time AS timestamp))"
    )
    List<PlanReminder> getRemindersByTime(Date time);
}
