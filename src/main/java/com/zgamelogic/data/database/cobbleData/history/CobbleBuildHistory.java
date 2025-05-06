package com.zgamelogic.data.database.cobbleData.history;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cobble_history_build")
public class CobbleBuildHistory extends CobbleHistory {
    private String buildingName;
    // TODO finish this
}
