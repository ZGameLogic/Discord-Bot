package com.zgamelogic.data.database.cobbleData.production;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleResourceType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

import static com.zgamelogic.data.database.cobbleData.CobbleResourceType.mapResources;

@Entity
@ToString
public class CobbleProduction {
    @Getter
    @EmbeddedId
    private CobbleProductionId id;
    private String cost;
    private String production;
    private String consumption;

    @Getter
    @Transient
    private Map<CobbleResourceType, Integer> resourceCost;
    @Getter
    @Transient
    private Map<CobbleResourceType, Integer> resourceProduction;
    @Getter
    @Transient
    private Map<CobbleResourceType, Integer> resourceConsumption;

    @PostLoad
    private void postInit(){
        resourceCost = mapResources(cost, false);
        resourceProduction = mapResources(production, false);
        resourceConsumption = mapResources(consumption, false);
    }

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
