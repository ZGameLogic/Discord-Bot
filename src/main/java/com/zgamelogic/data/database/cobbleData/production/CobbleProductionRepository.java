package com.zgamelogic.data.database.cobbleData.production;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CobbleProductionRepository extends JpaRepository<CobbleProduction, CobbleProduction.CobbleProductionId> {
    List<CobbleProduction> findAllById_Building(CobbleBuildingType id_building);
}
