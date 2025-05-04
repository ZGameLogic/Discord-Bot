package com.zgamelogic.data.database.cobbleData.player;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleResourceConverter;
import com.zgamelogic.data.database.cobbleData.CobbleResourceType;
import com.zgamelogic.data.database.cobbleData.CobbleServiceException;
import com.zgamelogic.data.database.cobbleData.action.CobbleAction;
import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpc;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor
public class CobblePlayer {
    @Id
    private long playerId;
    private LocalDateTime started;
    @Setter
    private String townName;
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> resources;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CobbleBuilding> buildings;
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CobbleNpc> npcs;
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CobbleAction> actions;

    public CobblePlayer(long playerId, String name) {
        this.playerId = playerId;
        started = LocalDateTime.now();
        buildings = new ArrayList<>();
        npcs = new ArrayList<>();
        actions = new ArrayList<>();
        townName = name + "'s town";
        resources = new HashMap<>();
    }

    public void addNpc(CobbleNpc npc) { npcs.add(npc); }

    public void addBuilding(CobbleBuildingType type, int level, String name, UUID buildingUUID) {
        CobbleBuilding building = new CobbleBuilding(this, type, level, name, buildingUUID);
        buildings.add(building);
    }

    public CobbleNpc getMayor() throws CobbleServiceException {
        return npcs.stream()
            .filter(npc -> npc.getCobbleBuilding().getType() == CobbleBuildingType.TOWN_HALL)
            .findFirst()
            .orElseThrow(() -> new CobbleServiceException("NPC not found"));
    }
}
