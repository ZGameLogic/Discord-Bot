package bot.role;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleBotListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(RoleBotListener.class);

	private long guildID;
	private LinkedList<Long> roleIDs;
	private LinkedList<Long> adminIDs;
	private DataCacher<Player> data;
	
	public RoleBotListener(ConfigLoader cl) {
		guildID = cl.getGuildID();
		roleIDs = cl.getRoleIDs();
		adminIDs = cl.getAdminRoleIDs();
		data = new DataCacher<>("arena");
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Role bot listener activated");
		checkRoles(event.getJDA().getGuildById(guildID));
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		assignRole(event.getMember(), event.getGuild());
		assignStats(event.getMember());
	}
	
	/**
	 * Assigns a random role to a member
	 * @param member
	 * @param guild
	 */
	private void assignRole(Member member, Guild guild) {
		Collections.shuffle(roleIDs);
		long randomRoleID = roleIDs.get(0);
		Role role = guild.getRoleById(randomRoleID);
		guild.addRoleToMember(member, role).queue();
		logger.info("Assinging " + member.getEffectiveName() + " to role " + role.getName());
	}
	
	private void assignStats(Member member) {
		int strength = (int)(Math.random() * 15);
		int agility = (int)(Math.random() * 15);
		int knowledge = (int)(Math.random() * 15);
		int magic = (int)(Math.random() * 15);
		int stamina = (int)(Math.random() * 15);
		Player p = new Player(strength, agility, knowledge, magic, stamina, 0, 0, 0, 0, 0, 0);
		data.saveSerialized(p, member.getIdLong() + "");
		logger.info("Creating statis for " + member.getEffectiveName() + ": "
				+ strength + " " + agility + " " + knowledge + " " + magic + " " + stamina);
	}
	
	/**
	 * Check a member if they are one of the roles
	 * @param member
	 * @return
	 */
	private boolean checkRoles(Member member) {
		for(Role role : member.getRoles()) {
			if(roleIDs.contains(role.getIdLong()) || adminIDs.contains(role.getIdLong())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks the roles of a guild
	 * @param guild
	 */
	private void checkRoles(Guild guild) {
		logger.info("Checking roles on shlongshot");
		for(Member m: guild.getMembers()) {
			if(!checkRoles(m)) {
				assignRole(m, guild);
			}
			if(!data.exists(m.getIdLong() + "")) {
				assignStats(m);
			}
		}
		logger.info("No more roles to assign");
	}
	
	public void sendStats(SlashCommandEvent event) {
		
		Member member;
		
		if(event.getOption("player") == null) {
			member = event.getMember();
		} else {
			member = event.getOption("player").getAsMember();
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		Player player = data.loadSerialized(member.getIdLong() + "");
		eb.setTitle("Fighter stats for " + member.getEffectiveName());
		eb.setColor(new Color(113, 94, 115));
		eb.setThumbnail(member.getEffectiveAvatarUrl());
		
		eb.addField("Strength", player.getStrength() + "", true);
		eb.addField("Knowledge", player.getKnowledge() + "", true);
		eb.addField("Magic", player.getMagic() + "", true);
		eb.addField("Agility", player.getAgility() + "", true);
		eb.addField("Stamina", player.getStamina() + "", true);
		
		eb.addField("Statistics", "Gold: " + player.getGold() + "\nTournement victories: " + player.getTournamentWins() + "\nVictories: " + player.getWins()
			+ "\nDefeats: " + player.getLosses(), false);
		
		eb.setFooter("Can attack " + (Player.DAILY_CHALLENGE_LIMIT - player.getHasChallengedToday()) + " more time(s) today\n"
				+ "Can defend " + (Player.DAILY_DEFEND_LIMIT - player.getChallengedToday()) + " more time(s) today");
		
		event.replyEmbeds(eb.build()).queue();
		
	}
	
	/**
	 * 
	 * @param challenger
	 * @param defender
	 */
	public void fight(Player attacker, Player defender, SlashCommandEvent event) {
		
	}

	public void challenge(SlashCommandEvent event) {
		Player defender = data.loadSerialized(event.getOption("player").getAsMember().getIdLong() + "");
		Player attacker = data.loadSerialized(event.getMember().getIdLong() + "");
		
		if(attacker.canChallenge()) {
			if(defender.canDefend()) {
				fight(attacker, defender, event);
			} else {
				event.reply("The defender has been challenged too many times today.").queue();
			}
		} else {
			event.reply("You are weary and cannot attack anymore today.").queue();
		}
	}
	
}
