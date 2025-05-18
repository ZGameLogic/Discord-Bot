package com.zgamelogic.data.database.cobbleData.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CobbleHistoryRepository extends JpaRepository<CobbleHistory, UUID> {
    default List<CobbleHistory> findAllByPlayer_PlayerIdAndCompletedBetween(long playerId, LocalDate day){
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay().minusNanos(1);
        return findAllByPlayer_PlayerIdAndCompletedBetween(playerId, start, end);
    }

    List<CobbleHistory> findAllByPlayer_PlayerIdAndCompletedBetween(long playerPlayerId, LocalDateTime startDate, LocalDateTime endDate);
}
