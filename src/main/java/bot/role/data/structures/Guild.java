package bot.role.data.structures;

import bot.role.data.item.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Guild extends SavableData {

    private int guildReputationLevel;
    private Map<Item.Material, Integer> craftingMaterials;
    private Map<String, Long> ids;
    private List<Long> members;
    private int guildReputationLevelXP;
    private int nextGuildLevelThreshold;
    private boolean publicGuild;

    public Guild(String id, long ownerId, long textChannelId, long voiceChannelId, long ownerRoleId, long officerRoleId, long memberRoleId, boolean publicGuild) {
        super(id);
        members = new LinkedList<>();
        ids = new HashMap<>();
        ids.put("ownerRole", ownerRoleId);
        ids.put("ownerId", ownerId);
        ids.put("officerRole", officerRoleId);
        ids.put("memberRole", memberRoleId);
        ids.put("textChannel", textChannelId);
        ids.put("voiceChannel", voiceChannelId);
        members.add(ownerId);
        this.publicGuild = publicGuild;
        guildReputationLevel = 1;
        craftingMaterials = new HashMap<>();
        guildReputationLevelXP = 0;
        nextGuildLevelThreshold = 10;
    }

    @JsonIgnore
    public boolean isInGuild(long id){
        return members.contains(id);
    }

    @JsonIgnore
    public boolean isGuildOwner(long id){
        return ids.get("ownerId") == id;
    }

    public void setOwnerId(long id){
        ids.put("ownerId", id);
    }

    public void addToGuild(long id){
        members.add(id);
    }

    public void removeFromGuild(long id){
        members.remove(id);
    }

    @JsonIgnore
    public boolean isEmpty(){
        return members.isEmpty();
    }

}
