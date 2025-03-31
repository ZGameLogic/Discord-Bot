package com.zgamelogic.data.database.planData.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PlanReminderRepository extends JpaRepository<PlanReminder, PlanReminder.PlanReminderId> {
    @Query("SELECT p FROM PlanReminder p WHERE " +
            "    YEAR(p.id.time) = YEAR(:time) " +
            "AND MONTH(p.id.time) = MONTH(:time) " +
            "AND DAY(p.id.time) = DAY(:time) " +
            "AND HOUR(p.id.time) = HOUR(:time) " +
            "AND MINUTE(p.id.time) = MINUTE(:time)"
    )
    List<PlanReminder> getRemindersByTime(Date time);
}
