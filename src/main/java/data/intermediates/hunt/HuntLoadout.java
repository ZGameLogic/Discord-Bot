package data.intermediates.hunt;

import data.database.huntData.gun.AmmoType;
import data.database.huntData.gun.HuntGun;
import data.database.huntData.item.HuntItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
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

    public String toString(){
        StringBuilder string = new StringBuilder();
        string.append(primary.propperName()).append(": ");
        string.append(String.join(" | ", convertToStringArray(primaryAmmo))).append("\n");
        string.append(secondary.propperName()).append(": ");
        string.append(String.join(" | ", convertToStringArray(secondaryAmmo))).append("\n");
        string.append(String.join(", ", convertToStringArrayT(tools))).append("\n");
        string.append(String.join(", ", convertToStringArrayT(consumables))).append("\n");
        return string.toString();
    }

    public LinkedList<String> convertToStringArray(LinkedList<AmmoType> stuff){
        LinkedList<String> strings = new LinkedList<>();
        for(AmmoType thing: stuff) strings.add(thing.toString());
        return strings;
    }

    public LinkedList<String> convertToStringArrayT(LinkedList<HuntItem> stuff){
        LinkedList<String> strings = new LinkedList<>();
        for(HuntItem thing: stuff) strings.add(thing.toString());
        return strings;
    }
}
