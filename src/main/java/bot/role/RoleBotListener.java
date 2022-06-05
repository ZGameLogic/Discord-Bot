package bot.role;

import bot.role.data.Data;
import bot.role.data.ResultsData;
import bot.role.data.results.ChallengeFightResults;
import bot.role.data.structures.Player;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.structures.StatBlock;
import bot.role.helpers.EmbedMessageGenerator;
import controllers.atlassian.JiraInterfacer;
import data.ConfigLoader;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RoleBotListener extends ListenerAdapter {

    private Data data;
    private ResultsData resultsData;
    private ConfigLoader config;
    private Guild guild;
    private TextChannel warChannel;

    public RoleBotListener(ConfigLoader config){
        DataCacher<Strings> strings = new DataCacher<Strings>("arena\\strings");
        if(!strings.exists("strings")){
            strings.saveSerialized(new Strings());
        }
        this.config = config;
        data = new Data();
        resultsData = new ResultsData();
    }

    @Override
    public void onReady(ReadyEvent event){
        guild = event.getJDA().getGuildById(data.getGameConfig().loadSerialized().getGuildId());
        guild.getTextChannelById(data.getGameConfig().loadSerialized().getGeneralChannelId());
        List<Member> members = guild.getMembers();
        for(Member m : members){
            // Check if any members don't have player data
            if(!m.getUser().isBot() && !data.getPlayers().exists(m.getIdLong())){
                createNewPlayer(m);
            }
        }
        Role kingRole = getKingRole();
        // assign king if there is no king
        if(guild.getMembersWithRoles(kingRole).size() == 0){
            Random random = new Random();
            Member newKing = members.get(random.nextInt(members.size()));
            assignNewKing(newKing);
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member newMember = event.getMember();
        createNewPlayer(newMember);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Member leavingMember = event.getMember();
        data.getPlayers().delete(leavingMember.getId()); // delete player
    }

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
            case "fight-stats":
                postComplexFightResults(event);
                break;
        }
    }

    /**
     * Creates a new player with a random caste role.
     * Also saves the player.
     * @param member new player
     */
    private void createNewPlayer(Member member){
        Player newPlayer = new Player(member.getIdLong(), member.getEffectiveName(), data.getGameConfig().loadSerialized());
        guild.modifyMemberRoles(member, null, getCasteRoles()).queue();
        assignRandomCasteRole(member);
        data.getPlayers().saveSerialized(newPlayer); // save new player
    }

    /**
     * Automatically sends a message to the default war channel about whom the new king is.
     * Removes any other caste roles, and assigns the king role
     * @param king Player to become the new king
     */
    private void assignNewKing(Member king){
        // TODO send message

        // remove old caste role if any
        // add king role
        guild.modifyMemberRoles(king, new LinkedList<Role>(Arrays.asList(new Role[]{getKingRole()})), getCasteRoles()).queue();
    }

    /**
     * Swaps the caste roles between two player of the server
     * @param player1
     * @param player2
     */
    private void swapCasteRoles(Player player1, Player player2){
        Member am = guild.getMemberById(player1.getId());
        Member ad = guild.getMemberById(player2.getId());
        swapCasteRoles(am, ad);
    }


    /**
     * Swaps the caste roles between two members of the server
     * @param member1 Member 1 to swap roles with
     * @param member2 Member 2 to swap roles with
     */
    private void swapCasteRoles(Member member1, Member member2){
        Role attackerRole = getCasteRoleOfPlayer(member1);
        Role defenderRole = getCasteRoleOfPlayer(member2);
        List<Role> attackerRoles = new LinkedList<>(Arrays.asList(new Role[]{attackerRole}));
        List<Role> defenderRoles = new LinkedList<>(Arrays.asList(new Role[]{defenderRole}));
        guild.modifyMemberRoles(member1, defenderRoles, attackerRoles).queue();
        guild.modifyMemberRoles(member2, attackerRoles, defenderRoles).queue();
    }

    /**
     * Assigns a random caste role that isn't the king to a player
     * @param member Member to assign the role too
     */
    private void assignRandomCasteRole(Member member){
        Random random = new Random();
        List<Long> roleIds = data.getGameConfig().loadSerialized().getRoleIds();
        Role randomRole = guild.getRoleById(roleIds.get(random.nextInt(roleIds.size())));
        guild.addRoleToMember(member, randomRole).queue();
    }

    /**
     * Gets a list of caste roles in role form
     * @return a list of caste roles in role form
     */
    private List<Role> getCasteRoles(){
        List<Role> roles = new LinkedList<>();
        for(Role role : guild.getRoles()){
            if(data.getGameConfig().loadSerialized().getRoleIds().contains(role.getIdLong())){
                roles.add(role);
            }
        }
        return roles;
    }

    /**
     * @param player Player to be checking for the caste roles for
     * @return The caste role they are assigned
     */
    private Role getCasteRoleOfPlayer(Player player){
        return getCasteRoleOfPlayer(guild.getMemberById(player.getId()));
    }

    /**
     * @param member Member to be checking for the caste roles for
     * @return The role they are assigned
     */
    private Role getCasteRoleOfPlayer(Member member){
        List<Role> roles = getCasteRoles();
        long kingRoleId = getKingRole().getIdLong();
        for(Role role : member.getRoles()){
            if(roles.contains(role) || kingRoleId == role.getIdLong()){
                return role;
            }
        }
        return null;
    }


    /**
     * @param player Player to be checking for the caste roles for
     * @return The name of the role
     */
    private String getCasteRoleNameOfPlayer(Player player){
        return getCasteRoleNameOfPlayer(guild.getMemberById(player.getId()));
    }

    /**
     * @param member Member to be checking for the caste roles for
     * @return The name of the role
     */
    private String getCasteRoleNameOfPlayer(Member member){
        Role role = getCasteRoleOfPlayer(member);
        if(role != null){
            return role.getName();
        } else {
            return "";
        }
    }

    /**
     * @return The king caste role as a role object
     */
    private Role getKingRole(){
        return guild.getRoleById(data.getGameConfig().loadSerialized().getKingRoleId());
    }

    /**
     * @return war text channel object
     */
    private TextChannel getWarTextChannel(){
        return guild.getTextChannelById(data.getGameConfig().loadSerialized().getGeneralChannelId());
    }

    private void postComplexFightResults(SlashCommandInteractionEvent event){
        String fightId = event.getOption("id").getAsString();
        if(resultsData.getChallenges().exists(fightId)) {
            ChallengeFightResults cfr = resultsData.getChallenges().loadSerialized(fightId);
            event.replyEmbeds(EmbedMessageGenerator.generate(cfr, EmbedMessageGenerator.Detail.COMPLEX)).queue();
        } else {
            event.reply("No fight exists with that id.").queue();
        }
    }
}
