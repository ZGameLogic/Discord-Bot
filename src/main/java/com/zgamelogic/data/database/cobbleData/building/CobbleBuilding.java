package com.zgamelogic.data.database.cobbleData.building;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.production.CobbleProduction;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@ToString
@NoArgsConstructor
public class CobbleBuilding {
    @EmbeddedId
    private CobbleBuildingId id;
    private int level;
    private LocalDateTime buildTime;
    @Enumerated(EnumType.STRING)
    private CobbleBuildingType type;
    private String buildingName;

    @OneToOne
    @JoinColumns({
        @JoinColumn(name = "type", referencedColumnName = "building", insertable = false, updatable = false),
        @JoinColumn(name = "level", referencedColumnName = "level", insertable = false, updatable = false)
    })
    private CobbleProduction production;

    public CobbleBuilding(long userId, CobbleBuildingType type) {
        level = 0;
        this.type = type;
        id = new CobbleBuildingId(userId);
    }

    @Getter
    @ToString
    @Embeddable
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class CobbleBuildingId {
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID cobbleBuildingId;
        private long userId;

        public CobbleBuildingId(long userId) { this.userId = userId; }
    }
}
