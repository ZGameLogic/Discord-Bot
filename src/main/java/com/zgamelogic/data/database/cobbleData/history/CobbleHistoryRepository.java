package com.zgamelogic.data.database.cobbleData.history;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CobbleHistoryRepository extends JpaRepository<CobbleHistory, CobbleHistory.CobbleHistoryId> {
}
