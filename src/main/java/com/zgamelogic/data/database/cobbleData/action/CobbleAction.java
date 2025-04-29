package com.zgamelogic.data.database.cobbleData.action;

import com.zgamelogic.data.database.cobbleData.CobbleActionType;
import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Entity
@ToString
@Getter
public class CobbleAction {
    @EmbeddedId
    private CobbleActionId id;
    @Enumerated(EnumType.STRING)
    private CobbleActionType type;
    private int remaining;
    @OneToOne
    @JoinColumns({
        @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false),
        @JoinColumn(name = "buildingId", referencedColumnName = "cobbleBuildingId", insertable = false, updatable = false)
    })
    private CobbleBuilding building;

    @Getter
    @ToString
    @Embeddable
    public static class CobbleActionId {
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        private long userId;
    }
}
