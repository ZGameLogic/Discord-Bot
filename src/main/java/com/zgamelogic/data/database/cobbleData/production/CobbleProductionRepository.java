package com.zgamelogic.data.database.cobbleData.production;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CobbleProductionRepository extends JpaRepository<CobbleProduction, CobbleProduction.CobbleProductionId> {
}
