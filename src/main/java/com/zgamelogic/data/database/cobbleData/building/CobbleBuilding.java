package com.zgamelogic.data.database.cobbleData.building;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
public class CobbleBuilding {
    @EmbeddedId
    private CobbleBuildingId id;
    private int level;
    private LocalDateTime buildTime;
    @Enumerated(EnumType.STRING)
    private CobbleBuildingType type;

    public CobbleBuilding(long userId, CobbleBuildingType type) {
        level = 0;
        this.type = type;
        id = new CobbleBuildingId(userId);
    }

    @Embeddable
    @NoArgsConstructor
    public static class CobbleBuildingId {
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID cobbleBuildingId;
        private long userId;

        public CobbleBuildingId(long userId) { this.userId = userId; }
    }
}
