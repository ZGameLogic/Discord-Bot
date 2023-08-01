package data.database.huntData.gun;

import lombok.*;

import javax.persistence.*;
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

    public String getAsset(){
        if(dualWieldable){
            int index = slot == Slot.MEDIUM ? 1 : 0;
            return asset.split(",")[index].replace(" ", "");
        }
        return asset;
    }

    public AmmoType getDefaultAmmo(){
        for(AmmoType type: ammoTypes){
            if(!type.isSpecial()){
                return type;
            }
        }
        return ammoTypes.get(0);
    }
}
