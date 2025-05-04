package com.zgamelogic.data.database.cobbleData.production;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingConverter;
import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleResourceConverter;
import com.zgamelogic.data.database.cobbleData.CobbleResourceType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Entity
@Getter
@ToString
public class CobbleProduction {
    @EmbeddedId
    private CobbleProductionId id;
    // The cost it takes to upgrade to this level from the previous
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> cost;
    // The amount of production per day this building produces
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> production;
    // The amount of resources consumed per day for this building to produce
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> consumption;
    // The buildings this level unlocks (not cumulative)
    @Convert(converter = CobbleBuildingConverter.class)
    private Map<CobbleBuildingType, Integer> unlocks;
    private int workers;

    @Getter
    @ToString
    @Embeddable
    @EqualsAndHashCode
    public static class CobbleProductionId {
        @Enumerated(EnumType.STRING)
        private CobbleBuildingType building;
        private int level;
    }
}
