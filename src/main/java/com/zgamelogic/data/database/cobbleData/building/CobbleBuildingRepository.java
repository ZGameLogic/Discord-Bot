package com.zgamelogic.data.database.cobbleData.building;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CobbleBuildingRepository extends JpaRepository<CobbleBuilding, UUID> {
}
