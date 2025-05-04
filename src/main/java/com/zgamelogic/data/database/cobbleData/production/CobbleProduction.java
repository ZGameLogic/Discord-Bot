package com.zgamelogic.data.database.cobbleData.production;

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
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> cost;
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> production;
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> consumption;

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
