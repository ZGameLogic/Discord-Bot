package com.zgamelogic.data.database.cobbleData.action;

import com.zgamelogic.data.database.cobbleData.CobbleActionType;
import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Entity
@ToString
@Getter
public class CobbleAction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    private CobbleActionType type;
    // Production remaining
    private int remaining;

    @OneToOne
    @JoinColumn(name = "cobbleBuildingId", referencedColumnName = "cobbleBuildingId", insertable = false, updatable = false)
    private CobbleBuilding building;

    @ManyToOne
    @JoinColumn(name = "playerId", referencedColumnName = "playerId", nullable = false)
    private CobblePlayer player;
}
