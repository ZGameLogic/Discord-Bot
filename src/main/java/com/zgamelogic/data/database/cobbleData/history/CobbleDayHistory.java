package com.zgamelogic.data.database.cobbleData.history;

import com.zgamelogic.data.database.cobbleData.CobbleResourceConverter;
import com.zgamelogic.data.database.cobbleData.enums.CobbleResourceType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Map;

@Entity
@Table(name = "cobble_history_day")
public class CobbleDayHistory extends CobbleHistory {
    @Convert(converter = CobbleResourceConverter.class)
    private Map<CobbleResourceType, Integer> resourcesGained;
}
