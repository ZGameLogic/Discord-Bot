package bot.role;

import java.awt.Color;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

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
	
	private static final int PADDING_MULTIPLIER = 8;
	private static final int STAT_BASE_CHANGE = 2;
	private static final int STAT_RANDOM_CHANGE = 2;
	
	private Logger logger = LoggerFactory.getLogger(RoleBotListener.class);

	private long guildID;
	private long kingRoleID;
	private LinkedList<Long> roleIDs;
	private LinkedList<Long> adminIDs;
	private DataCacher<Player> data;
	private final int boosterChange;
	
	public RoleBotListener(ConfigLoader cl) {
		guildID = cl.getGuildID();
		roleIDs = cl.getRoleIDs();
		adminIDs = cl.getAdminRoleIDs();
		data = new DataCacher<>("arena");
		kingRoleID = cl.getKingRoleID();
		boosterChange = cl.getBoosterChange();
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Role bot listener activated");
		checkGuildRoles(event.getJDA().getGuildById(guildID));
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
	 * Checks the guild for a king role
	 * @param guild
	 * @return true if the kind exists
	 */
	private boolean checkForKing(Guild guild) {
		for(Member m : guild.getMembers()) {
			for(Role r : m.getRoles()) {
				if(r.getIdLong() == kingRoleID) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks the roles of a guild
	 * @param guild
	 */
	private void checkGuildRoles(Guild guild) {
		logger.info("Checking roles on shlongshot");
		for(Member m: guild.getMembers()) {
			if(!checkRoles(m)) {
				assignRole(m, guild);
			}
			if(!data.exists(m.getIdLong() + "")) {
				assignStats(m);
			}
		}
		logger.info("Checking for king");
		
		if(checkForKing(guild)) {
			logger.info("King has been found!");
		} else {
			logger.info("King has not been found. Apointing new king");
			List<Member> members = guild.getMembers();
			Member king = members.get((int)(Math.random() * members.size()));
			while(king.getUser().isBot()) {
				king = members.get((int)(Math.random() * members.size()));
			}
			logger.info("Hail to the new king: " + king.getEffectiveName());
			guild.removeRoleFromMember(king, getCasteRole(king)).queue();;
			guild.addRoleToMember(king, guild.getRoleById(kingRoleID)).queue();
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
		
		if(member.getUser().isBot()) {
			event.reply("Bots can't play and don't have stats......yet").queue();
			return;
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		Player player = data.loadSerialized(member.getIdLong() + "");
		if(player == null) {
			
		}
		eb.setTitle("Fighter stats for " + member.getEffectiveName());
		eb.setColor(new Color(113, 94, 115));
		eb.setThumbnail(member.getEffectiveAvatarUrl());
		
		eb.addField("Strength", player.getStrength() + "", true);
		eb.addField("Knowledge", player.getKnowledge() + "", true);
		eb.addField("Magic", player.getMagic() + "", true);
		eb.addField("Agility", player.getAgility() + "", true);
		eb.addField("Stamina", player.getStamina() + "", true);
		
		eb.addField("Statistics", "Gold: " + player.getGold() + "\nTournament victories: " + player.getTournamentWins() + "\nVictories: " + player.getWins()
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
	private void fight(Member attackerMember, Member defenderMember, int defenderPadding, SlashCommandEvent event) {
		
		Player attacker = data.loadSerialized(attackerMember.getIdLong() + "");
		Player defender = data.loadSerialized(defenderMember.getIdLong() + "");
		
		int booster = 0;
		
		// Add a level of defending for the server boosters
		if(defenderMember.getTimeBoosted() != null) {
			booster += boosterChange;
		}
		
		// Hail to the king
		if(getCasteRoleIndex(defenderMember) == 0) {
			defenderPadding++;
		}
		
		attacker.hasChallenged();
		defender.wasChallenged();
		
		int stamina = attacker.getStamina() + defender.getStamina() + defenderPadding * PADDING_MULTIPLIER + booster;
		int strength = attacker.getStrength() + defender.getStrength() + defenderPadding * PADDING_MULTIPLIER + booster;
		int magic = attacker.getMagic() + defender.getMagic() + defenderPadding * PADDING_MULTIPLIER + booster;
		int agility = attacker.getAgility() + defender.getAgility() + defenderPadding * PADDING_MULTIPLIER + booster;
		int knowledge = attacker.getKnowledge() + defender.getKnowledge() + defenderPadding * PADDING_MULTIPLIER + booster;
		
		int attackerPoint = 0;
		int defenderPoint = 0;
		
		int stamWin = (int) (Math.random() * stamina + 1);
		int streWin = (int) (Math.random() * strength + 1);
		int magiWin = (int) (Math.random() * magic + 1);
		int agilWin = (int) (Math.random() * agility + 1);
		int knowWin = (int) (Math.random() * knowledge + 1);
		
		if(stamWin <= attacker.getStamina()) {attackerPoint++;} else {defenderPoint++;}
		if(streWin <= attacker.getStrength()) {attackerPoint++;} else {defenderPoint++;}
		if(magiWin <= attacker.getMagic()) {attackerPoint++;} else {defenderPoint++;}
		if(agilWin <= attacker.getAgility()) {attackerPoint++;} else {defenderPoint++;}
		if(knowWin <= attacker.getKnowledge()) {attackerPoint++;} else {defenderPoint++;}
		
		if(attackerPoint > defenderPoint) {
			// Attacker wins
			attacker.won();
			defender.lost();
			
			int dif = getCasteRoleIndex(attackerMember) - getCasteRoleIndex(defenderMember);
			EmbedBuilder eb = new EmbedBuilder();
			if(dif > 0) {	
				Guild guild = event.getGuild();
				Role attackerRole = getCasteRole(attackerMember);
				Role defenderRole = getCasteRole(defenderMember);
				guild.addRoleToMember(attackerMember, defenderRole).queue();
				guild.removeRoleFromMember(attackerMember, attackerRole).queue();
				
				guild.addRoleToMember(defenderMember, attackerRole).queue();
				guild.removeRoleFromMember(defenderMember, defenderRole).queue();
				
				eb.setDescription(attackerMember.getEffectiveName() + " is now a rank of " + defenderRole.getName());
			}
			
			
			eb.setTitle("Fight results: " + attackerMember.getEffectiveName() + " wins!");
			eb.setColor(new Color(25, 84, 43));
			eb.addField("Fight statistics", "Attacker points: " + attackerPoint + "\nDefender points: " + defenderPoint, false);
			eb.setTimestamp(Instant.now());
			
			event.replyEmbeds(eb.build()).queue();
		} else {
			// Attacker loses
			attacker.lost();
			defender.won();
			
			int stamDif = defender.getStamina() - attacker.getStamina();
			int streDif = defender.getStrength() - attacker.getStrength();
			int magiDif = defender.getMagic() - attacker.getMagic();
			int agilDif = defender.getAgility() - attacker.getAgility();
			int knowDif = defender.getKnowledge() - attacker.getKnowledge();
			
			PriorityQueue<Integer> stats = new PriorityQueue<>(Collections.reverseOrder());
			stats.add(stamDif);
			stats.add(streDif);
			stats.add(magiDif);
			stats.add(agilDif);
			stats.add(knowDif);
			
			int statDif = stats.remove();
			String statChanged = "";
			int statNumChanged = STAT_BASE_CHANGE + (int)(Math.random() * STAT_RANDOM_CHANGE) + 1 - STAT_RANDOM_CHANGE / 2;
			if(stamDif == statDif) {
				statChanged = "Stamina";
				attacker.increaseStamina(statNumChanged);
			} else if (streDif == statDif) {
				statChanged = "Strength";
				attacker.increaseStrength(statNumChanged);
			} else if (magiDif == statDif) {
				statChanged = "Magic";
				attacker.increaseMagic(statNumChanged);
			} else if (agilDif == statDif) {
				statChanged = "Agility";
				attacker.increaseAgility(statNumChanged);
			} else if (knowDif == statDif) {
				statChanged = "Knowledge";
				attacker.increaseKnowledge(statNumChanged);
			}
			
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Fight results: " + attackerMember.getEffectiveName() + " lost!");
			eb.setColor(new Color(84, 25, 25));
			eb.setDescription("Better luck next time. " + statChanged + " has been increased by " + statNumChanged + " points");
			eb.addField("Fight statistics", "Attacker points: " + attackerPoint + "\nDefender points: " + defenderPoint, false);
			eb.setTimestamp(Instant.now());
			
			event.replyEmbeds(eb.build()).queue();
		}
		
		data.saveSerialized(attacker, attackerMember.getId());
		data.saveSerialized(defender, defenderMember.getId());
	}

	public void challenge(SlashCommandEvent event) {
		Member attackerMember = event.getMember();
		Member defenderMember = event.getOption("player").getAsMember();
		
		Player attacker = data.loadSerialized(attackerMember.getIdLong() + "");
		Player defender = data.loadSerialized(defenderMember.getIdLong() + "");
		if(!defenderMember.getUser().isBot()) {
			if(attacker.canChallenge()) {
				if(defender.canDefend()) {
					int attackIndex = getCasteRoleIndex(attackerMember);
					int defendIndex = getCasteRoleIndex(defenderMember);
					int padding = 0;
					if(attackIndex > defendIndex) {
						padding = attackIndex - defendIndex;
					}
					fight(attackerMember, defenderMember, padding, event);
					
				} else {
					event.reply("The defender has been challenged too many times today.").queue();
				}
			} else {
				event.reply("You are weary and cannot attack anymore today.").queue();
			}
		} else {
			event.reply("You can't fight a robot").queue();
		}
	}
	
	private int getCasteRoleIndex(Member member) {
		for(Role r : member.getRoles()) {
			if(roleIDs.contains(r.getIdLong())) {
				return roleIDs.indexOf(r.getIdLong()) + 1;
			}
			if(r.getIdLong() == kingRoleID) {
				return 0;
			}
		}
		return -1;
	}
	
	private Role getCasteRole(Member member) {
		for(Role r : member.getRoles()) {
			if(roleIDs.contains(r.getIdLong())) {
				return r;
			}
			if(r.getIdLong() == kingRoleID) {
				return r;
			}
		}
		return null;
	}

	public void sendRoleStats(SlashCommandEvent event) {
		Role role = event.getOption("role").getAsRole();
		if(roleIDs.contains(role.getIdLong()) || role.getIdLong() == kingRoleID) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Fighter stats for " + role.getName() + "s");
			eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina");
			eb.setColor(new Color(113, 94, 115));
			if(role.getIcon() != null) {
				eb.setImage(role.getIcon().getIconUrl());
			}
			
			for(Member m : event.getGuild().getMembersWithRoles(role)) {
				if(!m.getUser().isBot()) {
					Player p = data.loadSerialized(m.getId());
					if(p.canDefend()) {
						eb.addField(m.getEffectiveName(), p.getCompactStats(), true);
					}
				}
			}
			
			event.replyEmbeds(eb.build()).queue();
		} else {
			event.reply("This is not a valid role to get stats for").queue();
		}
	}
}
