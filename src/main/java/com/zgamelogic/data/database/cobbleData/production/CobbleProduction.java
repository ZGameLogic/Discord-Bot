package com.zgamelogic.data.database.cobbleData.production;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
public class CobbleProduction {
    @EmbeddedId
    private CobbleProductionId id;
    private String cost;
    private String production;

    @Embeddable
    @EqualsAndHashCode
    public static class CobbleProductionId {
        @Enumerated(EnumType.STRING)
        private CobbleBuildingType building;
        private int level;
    }
}
