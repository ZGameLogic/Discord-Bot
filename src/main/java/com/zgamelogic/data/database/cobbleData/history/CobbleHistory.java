package com.zgamelogic.data.database.cobbleData.history;

import com.zgamelogic.data.database.cobbleData.CobbleActionType;
import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.action.CobbleAction;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class CobbleHistory {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDateTime completed;
    private int buildingLevel;

    @Enumerated(EnumType.STRING)
    private CobbleActionType actionType;
    @Enumerated(EnumType.STRING)
    private CobbleBuildingType buildingType;
    private UUID cobbleBuildingId;

    @ManyToOne
    @JoinColumn(name = "playerId", referencedColumnName = "playerId", nullable = false)
    private CobblePlayer player;

    public CobbleHistory(CobblePlayer player, int buildingLevel, CobbleActionType actionType, CobbleBuildingType buildingType, UUID cobbleBuildingId) {
        this.player = player;
        this.completed = LocalDateTime.now();
        this.buildingLevel = buildingLevel;
        this.actionType = actionType;
        this.buildingType = buildingType;
        this.cobbleBuildingId = cobbleBuildingId;
    }

    public static CobbleHistory from(CobbleAction action){
        return new CobbleHistory(
            action.getPlayer(),
            action.getBuilding().getLevel(),
            action.getType(),
            action.getBuilding().getType(),
            action.getBuilding().getCobbleBuildingId()
        );
    }
}
