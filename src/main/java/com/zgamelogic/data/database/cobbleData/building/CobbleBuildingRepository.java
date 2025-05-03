package com.zgamelogic.data.database.cobbleData.building;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CobbleBuildingRepository extends JpaRepository<CobbleBuilding, UUID> {
    Optional<CobbleBuilding> findByPlayer_PlayerIdAndCobbleBuildingId(long playerId, UUID cobbleBuildingId);
    List<CobbleBuilding> findAllByPlayer_PlayerId(long playerPlayerId);
}
