package com.zgamelogic.data.database.cobbleData.player;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.action.CobbleAction;
import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpc;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class CobblePlayer {
    @Id
    private long id;
    private LocalDateTime started;

    @Setter
    @OneToMany(mappedBy = "id.userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CobbleBuilding> buildings;
    @OneToMany(mappedBy = "id.userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CobbleNpc> npcs;
    @OneToMany(mappedBy = "id.userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CobbleAction> actions;

    public CobblePlayer(long id) {
        this.id = id;
        started = LocalDateTime.now();
        buildings = new ArrayList<>();
        npcs = new ArrayList<>();
        actions = new ArrayList<>();
    }

    public void addNpc(String firstname, String lastname, long appearance){
        CobbleNpc npc = new CobbleNpc(id, firstname, lastname, appearance);
        npcs.add(npc);
    }

    public void addBuilding(CobbleBuildingType type, int level, String name, UUID buildingUUID) {
        CobbleBuilding building = new CobbleBuilding(id, type, level, name, buildingUUID);
        buildings.add(building);
    }
}
