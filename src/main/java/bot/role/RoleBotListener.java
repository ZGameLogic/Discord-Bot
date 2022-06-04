package bot.role;

import bot.role.data.Data;
import bot.role.data.ResultsData;
import bot.role.data.results.ChallengeFightResults;
import bot.role.data.structures.Player;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.structures.StatBlock;
import bot.role.generators.EmbedMessageGenerator;
import data.ConfigLoader;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getAuthor().isBot()) {
            Player attacker = data.getPlayers().loadSerialized(369303799581507585l);
            Player defender = data.getPlayers().loadSerialized(232675572772372481l);
            ChallengeFightResults cfr =
                    new ChallengeFightResults(63l,
                            attacker,
                            defender,
                            3,
                            new StatBlock(1, 1, 1, attacker.getStrengthStat() + defender.getStrengthStat(), attacker.getAgilityStat() + defender.getAgilityStat()),
                            new StatBlock(0, 0, 0, 0, 0),
                            getCasteRoleOfPlayer(attacker),
                            getCasteRoleOfPlayer(defender),
                            0,
                            10,
                            1,
                            5);
            resultsData.getChallenges().saveSerialized(cfr);
            event.getChannel().sendMessageEmbeds(EmbedMessageGenerator.generate(cfr, EmbedMessageGenerator.Detail.COMPLEX)).queue();
        }
    }

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

    private String getCasteRoleOfPlayer(Player player){
        return getCasteROleOfPlayer(guild.getMemberById(player.getId()));
    }

    private String getCasteROleOfPlayer(Member member){
        List<Role> roles = getCasteRoles();
        for(Role role : member.getRoles()){
            if(roles.contains(role)){
                return role.getName();
            }
        }
        return "";
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
