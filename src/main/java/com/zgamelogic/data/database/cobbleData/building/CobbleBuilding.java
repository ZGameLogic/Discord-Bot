package com.zgamelogic.data.database.cobbleData.building;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import com.zgamelogic.data.database.cobbleData.production.CobbleProduction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@ToString
@NoArgsConstructor
public class CobbleBuilding {
    @Id
    private UUID cobbleBuildingId;
    private int level;
    private LocalDateTime buildTime;
    @Enumerated(EnumType.STRING)
    private CobbleBuildingType type;
    @Setter
    private String buildingName;

    @OneToOne
    @JoinColumns({
        @JoinColumn(name = "type", referencedColumnName = "building", insertable = false, updatable = false),
        @JoinColumn(name = "level", referencedColumnName = "level", insertable = false, updatable = false)
    })
    private CobbleProduction production;

    @ManyToOne
    @JoinColumn(name = "playerId", referencedColumnName = "playerId", nullable = false)
    private CobblePlayer player;

    public CobbleBuilding(CobblePlayer player, CobbleBuildingType type, int level, String name, UUID buildingId) {
        this.level = level;
        buildingName = name;
        this.type = type;
        this.player = player;
        this.cobbleBuildingId = buildingId;
        buildTime= LocalDateTime.now();
    }
}
