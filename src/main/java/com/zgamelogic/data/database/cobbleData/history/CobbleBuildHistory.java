package com.zgamelogic.data.database.cobbleData.history;

import com.zgamelogic.data.database.cobbleData.enums.CobbleActionType;
import com.zgamelogic.data.database.cobbleData.enums.CobbleBuildingType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "cobble_history_build")
public class CobbleBuildHistory extends CobbleHistory {
    private int level;
    @Enumerated(EnumType.STRING)
    private CobbleActionType action;
    @Enumerated(EnumType.STRING)
    private CobbleBuildingType buildingType;
    private UUID buildingId;
}
