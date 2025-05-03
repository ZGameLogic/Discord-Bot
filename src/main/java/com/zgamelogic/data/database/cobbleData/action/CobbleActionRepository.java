package com.zgamelogic.data.database.cobbleData.action;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CobbleActionRepository extends JpaRepository<CobbleAction, UUID> {
}
