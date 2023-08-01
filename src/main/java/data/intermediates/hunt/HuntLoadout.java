package data.intermediates.hunt;

import data.database.huntData.gun.AmmoType;
import data.database.huntData.gun.HuntGun;
import data.database.huntData.item.HuntItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class HuntLoadout {

    private HuntGun primary;
    private HuntGun secondary;
    private LinkedList<AmmoType> primaryAmmo;
    private LinkedList<AmmoType> secondaryAmmo;
    private LinkedList<HuntItem> tools;
    private LinkedList<HuntItem> consumables;

    public void addTool(HuntItem item){
        if(tools == null) tools = new LinkedList<>();
        tools.add(item);
    }

    public void addConsumable(HuntItem item){
        if(consumables == null) consumables = new LinkedList<>();
        consumables.add(item);
    }
}
