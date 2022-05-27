package bot.role;

import bot.role.data.Data;
import bot.role.data.Player;
import data.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Random;

public class RoleBotListener extends ListenerAdapter {

    private Data data;
    private ConfigLoader config;

    public RoleBotListener(ConfigLoader config){
        this.config = config;
        data = new Data();
    }

    @Override
    public void onReady(ReadyEvent event){
        boolean isKing = false;
        long kingId = data.getGameConfig().loadSerialized().getKingRoleId();
        Guild guild = event.getJDA().getGuildById(data.getGameConfig().loadSerialized().getGuildId());
        List<Member> members = guild.getMembers();
        for(Member m : members){
            // Check if any members don't have player data
            if(!data.getPlayers().exists(m.getIdLong())){
                Player newPlayer = new Player(m.getIdLong(), m.getEffectiveName());
                data.getPlayers().saveSerialized(newPlayer);
            }
            // Check if there is a king
            if(!isKing) {
                for (Role r : m.getRoles()) {
                    if (r.getIdLong() == kingId) {
                        isKing = true;
                        break;
                    }
                }
            }
        }
        // assign king if there is no king
        if(!isKing){
            Random random = new Random();
            Role kingRole = guild.getRoleById(kingId);
            guild.addRoleToMember(members.get(random.nextInt(members.size())), kingRole).queue();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member newMember = event.getMember();
        Player newPlayer = new Player(newMember.getIdLong(), newMember.getEffectiveName());
        data.getPlayers().saveSerialized(newPlayer);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {}

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {}

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {}

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {}

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {

        }
    }

}
