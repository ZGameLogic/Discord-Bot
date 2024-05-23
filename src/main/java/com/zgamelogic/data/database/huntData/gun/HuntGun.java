package com.zgamelogic.data.database.huntData.gun;

import lombok.*;

import jakarta.persistence.*;
import java.util.LinkedList;
import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "HuntGuns")
public class HuntGun {

    public enum Slot {
        LARGE,
        MEDIUM,
        SMALL
    }

    @Id
    private String name;
    @Setter
    @Enumerated(EnumType.STRING)
    private Slot slot;

    @Column
    @CollectionTable(name = "hunt_ammo_types", joinColumns = @JoinColumn(name = "ammo_types"))
    @ElementCollection
    private List<AmmoType> ammoTypes;

    @Column(columnDefinition = "integer default 1")
    private int specialAmmoCount;
    private boolean dualWieldable;

    private String asset;

    public AmmoType getDefaultAmmo(){
        for(AmmoType type: ammoTypes){
            if(!type.isSpecial()){
                return type;
            }
        }
        try {
            return ammoTypes.get(0);
        } catch (Exception e){
            System.out.println("error getting default ammo for " + name);
            throw e;
        }
    }

    public AmmoType getDefaultAmmo(boolean secondary){
        for(AmmoType type: ammoTypes){
            if(!type.isSpecial() && type.isSecondarySlotOnly() == secondary){
                return type;
            }
        }
        return ammoTypes.get(0);
    }

    public boolean hasSecondaryAmmo(){
        for(AmmoType type: ammoTypes){
            if(type.isSecondarySlotOnly()) return true;
        }
        return false;
    }

    public LinkedList<AmmoType> primaryAmmo(){
        LinkedList<AmmoType> primaryAmmo = new LinkedList<>(ammoTypes);
        primaryAmmo.removeIf(AmmoType::isSecondarySlotOnly);
        return primaryAmmo;
    }

    public LinkedList<AmmoType> secondaryAmmo(){
        LinkedList<AmmoType> secondaryAmmo = new LinkedList<>(ammoTypes);
        secondaryAmmo.removeIf(ammoType -> !ammoType.isSecondarySlotOnly());
        return secondaryAmmo;
    }

    public AmmoType getAmmoTypeFromString(String ammoType){
        for(AmmoType ammo: ammoTypes){
            if(ammo.getName().equals(ammoType)){
                return ammo;
            }
        }
        return null;
    }

    public LinkedList<AmmoType> getAmmoTypes(){
        return new LinkedList<>(ammoTypes);
    }

    public String propperName(){
        return dualWieldable && slot == Slot.MEDIUM ? "Dual " + name : name;
    }
}
