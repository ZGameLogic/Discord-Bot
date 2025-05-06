package com.zgamelogic.data.database.huntData.gun;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AmmoType {

    private String name;
    private String asset;
    private boolean special;
    private boolean secondarySlotOnly;

    public String toString(){ return name; }

}
