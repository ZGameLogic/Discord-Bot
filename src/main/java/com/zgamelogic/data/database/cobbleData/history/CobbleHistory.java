package com.zgamelogic.data.database.cobbleData.history;

import com.zgamelogic.data.database.cobbleData.CobbleActionType;
import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.action.CobbleAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class CobbleHistory {
    @EmbeddedId
    private CobbleHistoryId id;
    private LocalDateTime completed;
    private int buildingLevel;

    @Enumerated(EnumType.STRING)
    private CobbleActionType actionType;
    @Enumerated(EnumType.STRING)
    private CobbleBuildingType buildingType;
    private UUID cobbleBuildingId;

    public CobbleHistory(long userId, int buildingLevel, CobbleActionType actionType, CobbleBuildingType buildingType, UUID cobbleBuildingId) {
        id = new CobbleHistoryId(userId);
        this.completed = LocalDateTime.now();
        this.buildingLevel = buildingLevel;
        this.actionType = actionType;
        this.buildingType = buildingType;
        this.cobbleBuildingId = cobbleBuildingId;
    }

    public static CobbleHistory from(CobbleAction action){
        return new CobbleHistory(
            action.getId().getUserId(),
            action.getBuilding().getLevel(),
            action.getType(),
            action.getBuilding().getType(),
            action.getBuilding().getId().getCobbleBuildingId()
        );
    }

    @Getter
    @Embeddable
    @NoArgsConstructor
    public static class CobbleHistoryId {
        @GeneratedValue
        private UUID id;
        private long userId;

        public CobbleHistoryId(long userId) {
            this.userId = userId;
        }
    }
}
