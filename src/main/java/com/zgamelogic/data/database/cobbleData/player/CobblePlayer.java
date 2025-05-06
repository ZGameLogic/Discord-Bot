package com.zgamelogic.data.database.cobbleData.player;

import com.zgamelogic.data.database.cobbleData.enums.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleResourceConverter;
import com.zgamelogic.data.database.cobbleData.enums.CobbleResourceType;
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
        townName = name;
        resources = CobbleResourceType.mapResources("", true);
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

    public void addResource(CobbleResourceType type, int amount){
        resources.merge(type, amount, Integer::sum);
    }

    public void addResources(Map<CobbleResourceType, Integer> resources){
        resources.forEach((k, v) -> resources.merge(k, v, Integer::sum));
    }

    public void removeResources(Map<CobbleResourceType, Integer> resources){
        resources.forEach((k, v) -> resources.merge(k, -v, Integer::sum));
    }

    public boolean canAfford(Map<CobbleResourceType, Integer> resources) {
        for(Map.Entry<CobbleResourceType, Integer> entry : resources.entrySet()) {
            if(entry.getValue() > this.resources.get(entry.getKey())) return false;
        }
        return true;
    }

    public int populationCount(){ return npcs.size(); }
    public int populationCapacity(){ return buildings.stream().mapToInt(b -> b.getResource(CobbleResourceType.POPULATION)).sum(); }
}
