package bot.role.data.structures;

import bot.role.data.structures.item.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Guild extends SavableData {

    private int guildReputationLevel;
    private Map<Item.Material, Integer> craftingMaterials;
    private Ids ids;
    private List<Long> members;
    private int guildReputationLevelXP;
    private int nextGuildLevelThreshold;
    private boolean publicGuild;


    @Getter
    @AllArgsConstructor
    public class Ids {
        @Setter
        private long owner;
        private long ownerRole;
        private long officerRole;
        private long memberRole;
        private long textChannel;
        private long voiceChannel;
    }

    public Guild(String id, long ownerId, long textChannelId, long voiceChannelId, long ownerRoleId, long officerRoleId, long memberRoleId, boolean publicGuild) {
        super(id);
        members = new LinkedList<>();
        ids = new Ids(ownerId, ownerRoleId, officerRoleId, memberRoleId, textChannelId, voiceChannelId);
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
        return ids.getOwnerRole() == id;
    }

    @JsonIgnore
    public boolean isGuildOfficer(long id){
        return ids.getOfficerRole() == id;
    }

    public void setOwnerId(long id){
        ids.setOwner(id);
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
