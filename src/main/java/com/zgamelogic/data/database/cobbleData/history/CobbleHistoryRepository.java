package com.zgamelogic.data.database.cobbleData.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CobbleHistoryRepository extends JpaRepository<CobbleHistory, UUID> {
}
