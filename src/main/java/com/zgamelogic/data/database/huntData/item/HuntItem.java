package com.zgamelogic.data.database.huntData.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "hunt_items")
@EqualsAndHashCode
public class HuntItem {

    public enum Type {
        TOOL,
        CONSUMABLE,
        MELEE,
        MEDKIT,
        HEALING,
    }

    @Id
    private String name;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String asset;

    public String toString(){ return name; }
}
