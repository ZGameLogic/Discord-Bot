package com.zgamelogic.data.database.cobbleData.history;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cobble_history_birth")
public class CobbleBirthHistory extends CobbleHistory {
    private String name;
}
