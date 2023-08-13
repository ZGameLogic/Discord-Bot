package data.intermediates.hunt;

import data.database.huntData.gun.AmmoType;
import data.database.huntData.gun.HuntGun;
import data.database.huntData.item.HuntItem;
import lombok.*;

import java.util.LinkedList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    public String[] convertToSlashCommandOptions(){
        LinkedList<String> options = new LinkedList<>();
        options.add(primary.getName());
        primaryAmmo.forEach(ammo -> options.add(primary.getName() + " " + ammo.getName()));
        options.add(secondary.getName());
        secondaryAmmo.forEach(ammo -> options.add(secondary.getName() + " " + ammo.getName()));
        tools.forEach(tool -> options.add(tool.getName()));
        consumables.forEach(cons -> options.add(cons.getName()));
        return options.toArray(new String[0]);
    }

    public boolean hasItem(String item){
        String strippedItem = item.replace(primary.getName(), "").replace(secondary.getName(), "").trim();
        if(primary.getName().equals(item)) return true;
        for(AmmoType type: primaryAmmo) if(type.getName().equals(strippedItem)) return true;
        if(secondary.getName().equals(item)) return true;
        for(AmmoType type: secondaryAmmo) if(type.getName().equals(strippedItem)) return true;
        for(HuntItem tool: tools) if(tool.getName().equals(item)) return true;
        for(HuntItem cons: consumables) if(cons.getName().equals(item)) return true;
        return false;
    }

    public void removeItemFromLoadout(String item){
        String strippedItem = item.replace(primary.getName(), "").replace(secondary.getName(), "").trim();

        if(primary.getName().equals(item)){
            primary = null;
            return;
        }

        for(AmmoType type: primaryAmmo) if(type.getName().equals(strippedItem)) {
            primaryAmmo.set(primaryAmmo.indexOf(type), null);
            return;
        }

        if(secondary.getName().equals(item)){
            secondary = null;
            return;
        }

        for(AmmoType type: secondaryAmmo) if(type.getName().equals(strippedItem)){
            secondaryAmmo.set(secondaryAmmo.indexOf(type), null);
            return;
        }

        for(HuntItem tool: tools) if(tool.getName().equals(item)) {
            tools.set(tools.indexOf(tool), null);
            return;
        }

        for(HuntItem cons: consumables) if(cons.getName().equals(item)){
            consumables.set(consumables.indexOf(cons), null);
            return;
        }
    }
}
