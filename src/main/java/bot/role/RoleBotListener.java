package bot.role;

import java.awt.Color;
import java.io.File;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleBotListener extends ListenerAdapter {
	
	public static int dailyChallengeLimit;
	public static int dailyDefendLimit;
	
	private final int paddingMultiplier;
	private final int statBaseChange;
	private final int statRandomChange;
	private final int encounterStatMultiplier;
	
	private Logger logger = LoggerFactory.getLogger(RoleBotListener.class);

	private long guildID;
	private long kingRoleID;
	private LinkedList<Long> roleIDs;
	private DataCacher<Player> data;
	private DataCacher<EncounterPlayer> encounterData;
	private final int boosterChange;
	private long encountersID;
	private long fightEmojiID;
	private long generalID;
	private long spawnChance;
	
	private TextChannel encountersChannel;
	private TextChannel generalChannel;
	
	public RoleBotListener(ConfigLoader cl) {
		guildID = cl.getGuildID();
		roleIDs = cl.getRoleIDs();
		data = new DataCacher<>("arena");
		encounterData = new DataCacher<>("encounter");
		
		kingRoleID = cl.getKingRoleID();
		encountersID = cl.getEncountersID();
		generalID = cl.getGeneralID();
		boosterChange = cl.getBoosterChange();
		fightEmojiID = cl.getFightEmojiID();
		
		paddingMultiplier = cl.getPaddingMultiplier();
		statBaseChange = cl.getStatBaseChange();
		statRandomChange = cl.getStatRandomChange();
		encounterStatMultiplier = cl.getEncounterStatMultiplier();
		spawnChance = cl.getSpawnChance();
		
		dailyChallengeLimit = cl.getDailyChallengeLimit();
		dailyDefendLimit = cl.getDailyDefendLimit();
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Role bot listener activated");
		Guild guild = event.getJDA().getGuildById(guildID);
		encountersChannel = guild.getTextChannelById(encountersID);
		generalChannel = guild.getTextChannelById(generalID);
		
		checkGuildRoles(guild);
		// start midnight counter
		new Thread(() -> midnightReset()).start();
		// start random encounter
		new Thread(() -> randomEncounters()).start();
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		assignRole(event.getMember(), event.getGuild());
		assignStats(event.getMember());
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.isFromType(ChannelType.PRIVATE)) {
			if(event.getAuthor().getIdLong() == 232675572772372481l) {
				String message = event.getMessage().getContentRaw();
				processCommand(message, event);
			}
		}
	}

	

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(!event.getUser().isBot()) {
			if(event.getChannel().getIdLong() == encountersID && event.getReactionEmote().getIdLong() == fightEmojiID) {
				event.retrieveMessage().queue(message -> {
					long encounterID = Long.parseLong(message.getEmbeds().get(0).getFooter().getText());
					EncounterPlayer ep = encounterData.loadSerialized(encounterID + "");
					if(ep.canFightPlayer(event.getMember().getIdLong())) {
						Player p = data.loadSerialized(event.getUserId());
						if(p.canChallenge()) {
							fightEncounter(event.getMember(), ep, event);
							ep.addPlayerFought(event.getUserIdLong());
							encounterData.saveSerialized(ep, ep.getEncounterID() + "");
						} else {
							generalChannel.sendMessage("<@" + event.getUserId() + ">, you are too tired to fight again today").mention(event.getUser()).queue();
						}
					} else {
						generalChannel.sendMessage("<@" + event.getUserId() + ">, you have already fought this encounter. Encounter ID:" + encounterID).mention(event.getUser()).queue();
					}
				});
			}
		}
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
		int gold = (int)(Math.random() * 30);
		Player p = new Player(strength, agility, knowledge, magic, stamina, gold, 0, 0, 0, 0, 0);
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
			if(roleIDs.contains(role.getIdLong()) || kingRoleID == role.getIdLong()){
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
			if(!m.getUser().isBot() && !checkRoles(m)) {
				assignRole(m, guild);
			}
			if(!m.getUser().isBot() && !data.exists(m.getIdLong() + "")) {
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
		
		eb.setFooter("Can attack " + (dailyChallengeLimit - player.getHasChallengedToday()) + " more time(s) today\n"
				+ "Can defend " + (dailyDefendLimit - player.getChallengedToday()) + " more time(s) today");
		event.replyEmbeds(eb.build()).queue();
	}
	
	private FightResults fight(Player attacker, EncounterPlayer ep) {
		Player defender = new Player(ep);
		return fight(attacker, defender, 0, 0);
	}
	
	private FightResults fight(Player attacker, Player defender, int defenderPadding, int boosterPadding) {
		
		int booster = 0;
		
		attacker.hasChallenged();
		defender.wasChallenged();
		
		int stamina = attacker.getStamina() + defender.getStamina() + defenderPadding * paddingMultiplier + booster;
		int strength = attacker.getStrength() + defender.getStrength() + defenderPadding * paddingMultiplier + booster;
		int magic = attacker.getMagic() + defender.getMagic() + defenderPadding * paddingMultiplier + booster;
		int agility = attacker.getAgility() + defender.getAgility() + defenderPadding * paddingMultiplier + booster;
		int knowledge = attacker.getKnowledge() + defender.getKnowledge() + defenderPadding * paddingMultiplier + booster;
		
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
		
		return new FightResults(attackerPoint > defenderPoint, attackerPoint, defenderPoint);
	}
	
	/**
	 * 
	 * @param challenger
	 * @param defender
	 */
	private void fight(Member attackerMember, Member defenderMember, int defenderPadding, SlashCommandEvent event) {
		
		Player attacker = data.loadSerialized(attackerMember.getId());
		Player defender = data.loadSerialized(defenderMember.getId());
		
		int booster = 0;
		
		// Add a level of defending for the server boosters
		if(defenderMember.getTimeBoosted() != null) {
			booster += boosterChange;
		}
		
		FightResults results = fight(attacker, defender, defenderPadding, booster);
		
		if(results.isAttackerWon()) {
			// Attacker wins
			attacker.won();
			defender.lost();
			
			int dif = getCasteRoleIndex(attackerMember) - getCasteRoleIndex(defenderMember);
			EmbedBuilder eb = new EmbedBuilder();
			if(dif > 0) {
				// if attacker was attacking up the caste
				Guild guild = event.getGuild();
				Role attackerRole = getCasteRole(attackerMember);
				Role defenderRole = getCasteRole(defenderMember);
				guild.addRoleToMember(attackerMember, defenderRole).queue();
				guild.removeRoleFromMember(attackerMember, attackerRole).queue();
				
				guild.addRoleToMember(defenderMember, attackerRole).queue();
				guild.removeRoleFromMember(defenderMember, defenderRole).queue();
				
				long goldWon = 7 + (int)(Math.random() * 3);
				if(defender.getGold() < goldWon) {
					goldWon = defender.getGold();
				}
				defender.decreaseGold(goldWon);
				attacker.increaseGold(goldWon);
				
				eb.setDescription(attackerMember.getEffectiveName() +  " vs " + defenderMember.getEffectiveName() + "\n" + attackerMember.getEffectiveName() + " is now a rank of " + defenderRole.getName() + ". Gold obtained: " + goldWon);
			} else {
				long goldWon = 3 + (int)(Math.random() * 3);
				if(defender.getGold() < goldWon) {
					goldWon = defender.getGold();
				}
				defender.decreaseGold(goldWon);
				attacker.increaseGold(goldWon);
				eb.setDescription(attackerMember.getEffectiveName() +  " vs " + defenderMember.getEffectiveName() + "\nGold obtained: " + goldWon);
			}
			
			
			eb.setTitle("Fight results: " + attackerMember.getEffectiveName() + " wins!");
			eb.setColor(new Color(25, 84, 43));
			eb.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(), false);
			eb.setTimestamp(Instant.now());
			
			event.replyEmbeds(eb.build()).queue();
		} else {
			// Attacker loses
			attacker.lost();
			defender.won();
			
			int dif = getCasteRoleIndex(attackerMember) - getCasteRoleIndex(defenderMember);
			long goldLost = 0;
			if(dif > 0) {
				// if attacker was attacking up the caste
				goldLost = 3 + (int)(Math.random() * 3);
				if(attacker.getGold() < goldLost) {
					goldLost = attacker.getGold();
				}
				defender.increaseGold(goldLost);
				attacker.decreaseGold(goldLost);
			} else {
				goldLost = 7 + (int)(Math.random() * 3);
				if(attacker.getGold() < goldLost) {
					goldLost = attacker.getGold();
				}
				defender.increaseGold(goldLost);
				attacker.decreaseGold(goldLost);
			}
			
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
			int statNumChanged = statBaseChange + (int)(Math.random() * statRandomChange) + 1 - statRandomChange / 2;
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
			eb.setDescription("Better luck next time. " + statChanged + " has been increased by " + statNumChanged + " points."
					+ " Gold lost: " + goldLost);
			eb.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(), false);
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
					// Hail to the king
					if(getCasteRoleIndex(defenderMember) == 0) {
						padding++;
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
			eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina Gold");
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
	
	private void processCommand(String message, MessageReceivedEvent event) {
		if(message.contains("!reroll")) {
			message = message.replace("!reroll ", "");
			long userID = Long.parseLong(message);
			data.delete(userID + "");
			Member member = event.getJDA().getGuildById(guildID).getMemberById(userID);
			logger.info("Private command recieved for stat reroll for " + member.getEffectiveName());
			assignStats(member);
			Player stats = data.loadSerialized(userID + "");
			event.getPrivateChannel().sendMessage("Re-rolled stats for " + member.getEffectiveName() 
					+ "Strength: " + stats.getStrength() + "\n"
					+ "Knowledge: " + stats.getKnowledge() + "\n"
					+ "Magic: " + stats.getMagic() + "\n"
					+ "Agility: " + stats.getAgility() + "\n"
					+ "Stamina: " + stats.getStamina() + "\n").queue();
		} else if (message.contains("!reset-gold")) {
			logger.info("Resetting gold");
			for(File f : data.getFiles()) {
				Player player = data.loadSerialized(f.getName());
				player.setGold((int)(Math.random() * 30));
				data.saveSerialized(player, f.getName());
			}
			event.getPrivateChannel().sendMessage("Reset gold").queue();
		} else if (message.contains("!reset-challenges")) {
			logger.info("Resetting challenges");
			for(File f : data.getFiles()) {
				Player player = data.loadSerialized(f.getName());
				player.newDay();
				data.saveSerialized(player, f.getName());
			}
			event.getPrivateChannel().sendMessage("Reset challenges").queue();
		} else if(message.contains("!roll-encounter")) {
			logger.info("Rolling random encounter");
			rollEncounter();
			event.getPrivateChannel().sendMessage("Rolled encounter").queue();
		}
	}
	
	private void rollEncounter() {
		EmbedBuilder eb = new EmbedBuilder();
		
		LinkedList<String> tiers = new LinkedList<>();
		LinkedList<String> types = new LinkedList<>();
		tiers.add("Pitiful");
		tiers.add("Wimpy");
		tiers.add("Normal");
		tiers.add("Skilled");
		tiers.add("Master");
		
		types.add("Bandit");
		types.add("Blob");
		types.add("Wizard");
		types.add("Skeleton");
		types.add("Wolf");
		types.add("Ghoul");
		types.add("Giant");
		types.add("Troll");
		
		int tier = (int)(Math.random() * tiers.size());
		int type = (int)(Math.random() * types.size());
		
		int strength = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int knowledge = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int magic = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int agility = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int stamina = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		
		long encounterID = (long)(Math.random() * 100000000);
		while(encounterData.exists(encounterID + "")) {
			encounterID = (long)(Math.random() * 100000000);
		}
		
		EncounterPlayer baddy = new EncounterPlayer(strength, agility, knowledge, magic, stamina, encounterID, tiers.get(tier) + " " + types.get(type));
		encounterData.saveSerialized(baddy, baddy.getEncounterID() + "");
		
		eb.addField("Strength", strength + "", true);
		eb.addField("Knowledge", knowledge + "", true);
		eb.addField("Magic", magic + "", true);
		eb.addField("Agility", agility + "", true);
		eb.addField("Stamina", stamina + "", true);
		
		eb.setTitle("A " + tiers.get(tier) + " " + types.get(type) + " challenges the kingdom!");
		eb.setColor(new Color(56, 79, 115));
				
		eb.setFooter(encounterID + "");

		encountersChannel.sendMessageEmbeds(eb.build()).queue(message -> {
			message.addReaction(encountersChannel.getGuild().getEmoteById(fightEmojiID)).queue();
		});
		
		logger.info("Adding encounter");
	}
	
	private void fightEncounter(Member player, EncounterPlayer ep, MessageReactionAddEvent event) {
		Player attacker = data.loadSerialized(player.getId());
		FightResults results = fight(attacker, ep);
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Encounter fight results for " + player.getEffectiveName());
		
		if(results.isAttackerWon()) {
			// we win the encounter
			// add gold
			long goldWon = (int)(Math.random() * 20);
			attacker.increaseGold(goldWon);
			attacker.won();
			eb.setColor(new Color(25, 84, 43));
			String skillName = "";
			int skillInc = (int)(Math.random() * 3) + 1;

			switch ((int) (Math.random() * 5)) {
			case 0:
				skillName = "Strength";
				attacker.increaseStrength(skillInc);
				break;
			case 1:
				skillName = "Agility";
				attacker.increaseAgility(skillInc);
				break;
			case 2:
				skillName = "Magic";
				attacker.increaseMagic(skillInc);
				break;
			case 3:
				skillName = "Stamina";
				attacker.increaseStamina(skillInc);
				break;
			case 4:
				skillName = "Knowledge";
				attacker.increaseKnowledge(skillInc);
				break;
			}
			
			eb.setDescription("You have won the fight against a " + ep.getName() + "! Gold gained: " + goldWon + "\n"
					+ skillName + " increased by: " + skillInc);
		} else {
			// we lose the encounter
			// take away gold
			long goldLost = (int)(Math.random() * 15);
			if(attacker.getGold() < goldLost) {
				goldLost = attacker.getGold();
			}
			attacker.lost();
			attacker.decreaseGold(goldLost);
			eb.setColor(new Color(84, 25, 25));
			eb.setDescription("You have lost the fight against a " + ep.getName() + ". Gold lost: " + goldLost);
		}
		
		eb.addField("Fight statistices", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(),true);
		eb.setTimestamp(Instant.now());
		generalChannel.sendMessage("<@" + event.getUserId() + ">").queue();
		generalChannel.sendMessageEmbeds(eb.build()).queue();
		data.saveSerialized(attacker, player.getId());
	}
	
	private void randomEncounters() {
		
		while(true) {
			Calendar date = new GregorianCalendar();
			if(date.get(Calendar.MINUTE) % 15 == 0) {
				// 50% chance to spawn an encounter
				if(((int)(Math.random() * spawnChance)) == 1) {
					rollEncounter();
				}
			}
			// sleep for 1 minute
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void midnightReset() {
		while (true) {
			Calendar date = new GregorianCalendar();
			if(date.get(Calendar.HOUR) == 0 && date.get(Calendar.MINUTE) == 0) {
				// if its midnight
				for(File f : data.getFiles()) {
					Player p = data.loadSerialized(f.getName());
					p.newDay();
					p.increaseGold((int)(Math.random() * 20));
					if(f.getName().contains(kingRoleID + "") ) {
						p.increaseGold((int)(Math.random() * 100));
					}
					data.saveSerialized(p, f.getName());
				}
			}
			
			// sleep for a minute
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
