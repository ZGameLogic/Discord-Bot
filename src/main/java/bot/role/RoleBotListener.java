package bot.role;

import java.awt.Color;
import java.io.File;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.role.data.Activity;
import bot.role.data.Activity.ActivityReward;
import controllers.EmbedMessageMaker;
import controllers.dice.DiceRollingSimulator;
import data.ConfigLoader;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
	private DataCacher<KingPlayer> kingData;
	private DataCacher<Tax> taxData;
	private DataCacher<HonorablePromotion> hpData;
	private final int boosterChange;
	private long encountersID;
	private long fightEmojiID;
	private String[] iconIDs;
	private long generalID;
	private long activitiesID;
	private long spawnChance;
	
	DataCacher<Activity> activityData;
	private long activitySpawnChance;
	private long activityDuration;
	
	private Guild guild;
	
	private TextChannel encountersChannel;
	private TextChannel activitiesChannel;
	private TextChannel generalChannel;
	
	public RoleBotListener(ConfigLoader cl) {
		guildID = cl.getGuildID();
		roleIDs = cl.getRoleIDs();
		data = new DataCacher<>("arena//players");
		encounterData = new DataCacher<>("arena//encounter");
		kingData = new DataCacher<>("arena//king");
		taxData = new DataCacher<>("arena//tax");
		hpData = new DataCacher<>("arena//honorablePromotion");
		activityData = new DataCacher<>("arena//activities");
		
		if(kingData.getFiles().length == 0) {
			kingData.saveSerialized(new KingPlayer(), "king");
		}
		
		kingRoleID = cl.getKingRoleID();
		encountersID = cl.getEncountersID();
		generalID = cl.getGeneralID();
		activitiesID = cl.getActivitiesID();
		boosterChange = cl.getBoosterChange();
		fightEmojiID = cl.getFightEmojiID();
		
		iconIDs = cl.getIconIDS();
		
		paddingMultiplier = cl.getPaddingMultiplier();
		statBaseChange = cl.getStatBaseChange();
		statRandomChange = cl.getStatRandomChange();
		encounterStatMultiplier = cl.getEncounterStatMultiplier();
		spawnChance = cl.getSpawnChance();
		activitySpawnChance = cl.getActivitySpawnChance();
		activityDuration = cl.getDaysToStoreActivities();
		
		dailyChallengeLimit = cl.getDailyChallengeLimit();
		dailyDefendLimit = cl.getDailyDefendLimit();
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Role bot listener activated");
		guild = event.getJDA().getGuildById(guildID);
		encountersChannel = guild.getTextChannelById(encountersID);
		generalChannel = guild.getTextChannelById(generalID);
		activitiesChannel = guild.getTextChannelById(activitiesID);
		
		checkGuildRoles(guild);
		// start midnight counter
		new Thread(() -> midnightReset()).start();
		// start random encounter
		new Thread(() -> randomRolls()).start();
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
			if(event.getReactionEmote().getIdLong() == fightEmojiID) {
				if(event.getChannel().getIdLong() == encountersID) {
					event.retrieveMessage().queue(message -> {
						long encounterID = Long.parseLong(message.getEmbeds().get(0).getFooter().getText());
						EncounterPlayer ep = encounterData.loadSerialized(message.getId());
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
				} else if(event.getChannel().getIdLong() == activitiesID) {
					event.retrieveMessage().queue(message -> {
						Activity activity = activityData.loadSerialized(message.getId());
						if(activity.canPlayerWork(event.getUserIdLong())) {
							Player p = data.loadSerialized(event.getUserId());
							if(dailyChallengeLimit - p.getHasChallengedToday() >= activity.getActionCost()) {
								if(p.getGold() >= activity.getGoldCost()) {
									activity.addPlayerWorked(event.getUserIdLong());
									p.decreaseGold(activity.getGoldCost());
									switch(activity.getReward()) {
									case Agility:
										p.increaseAgility(activity.getRewardAmount());
										break;
									case Gold:
										p.increaseGold(activity.getRewardAmount());
										break;
									case Knowledge:
										p.increaseKnowledge(activity.getRewardAmount());
										break;
									case Magic:
										p.increaseMagic(activity.getRewardAmount());
										break;
									case Stamina:
										p.increaseStamina(activity.getRewardAmount());
										break;
									case Strength:
										p.increaseStrength(activity.getRewardAmount());
										break;
									}
									
									for(int i = 0; i < activity.getActionCost(); i++) {
										p.hasChallenged();
									}
									
									generalChannel.sendMessage("<@" + event.getUserId() + ">").queue();
									generalChannel.sendMessageEmbeds(EmbedMessageMaker.activityResults(event.getMember().getEffectiveName(), activity.getReward().name(), activity.getRewardAmount()).build()).queue();
									
									data.saveSerialized(p, event.getUserId());
									activityData.saveSerialized(activity, message.getId());
								} else {
									generalChannel.sendMessage("<@" + event.getUserId() + ">, you do not have enough gold to pay the tutor.").queue();
								}
							} else {
								generalChannel.sendMessage("<@" + event.getUserId() + ">, you are too tired to participate in this activity today.").mention(event.getUser()).queue();
							}
						} else {
							generalChannel.sendMessage("<@" + event.getUserId() + ">, you have already participated in this activity.").mention(event.getUser()).queue();
						}
					});
				}
			}
		}
	}

	public void challenge(SlashCommandEvent event) {
		
		if(event.getChannel().getIdLong() != generalID) {
			event.reply("You can only challenge in <#" + generalID + ">").queue();
			return;
		}
		
		Member attackerMember = event.getMember();
		Member defenderMember = event.getOption("player").getAsMember();
		
		Player attacker = data.loadSerialized(attackerMember.getIdLong() + "");
		Player defender = data.loadSerialized(defenderMember.getIdLong() + "");
		if(!defenderMember.getUser().isBot()) {
			if(attacker.canChallenge()) {
				if(attackerMember.getIdLong() != defenderMember.getIdLong()) {
					if((defender.canDefend() && getCasteRoleIndex(defenderMember) != 0) || (getCasteRoleIndex(defenderMember) == 0 && canChallengeKing(attackerMember.getIdLong()))) {
						if(getCasteRoleIndex(defenderMember) == 0) {
							KingPlayer kp = kingData.loadSerialized("king");
							kp.addPlayer(attackerMember.getIdLong());
							kingData.saveSerialized(kp, "king");
						}
						int attackIndex = getCasteRoleIndex(attackerMember);
						int defendIndex = getCasteRoleIndex(defenderMember);
						int padding = 0;
						if(attackIndex > defendIndex && attackIndex - defendIndex != 1) {
							padding = attackIndex - defendIndex;
						}
						// Hail to the king
						if(getCasteRoleIndex(defenderMember) == 0) {
							padding++;
						}
						
						fight(attackerMember, defenderMember, padding, event);
						
					} else {
						if(getCasteRoleIndex(defenderMember) == 0) {
							event.reply("You already challenged the king today.").queue();
						} else {
							event.reply("The defender has been challenged too many times today.").queue();
						}
					}
				}else {
					event.reply("Are you drunk or something?").queue();
				}
			} else {
				event.reply("You are weary and cannot attack anymore today.").queue();
			}
		} else {
			event.reply("You can't fight a robot").queue();
		}
	}

	public void sendRoleStats(SlashCommandEvent event) {
		Role role = event.getOption("role").getAsRole();
		if(roleIDs.contains(role.getIdLong()) || role.getIdLong() == kingRoleID) {
			event.replyEmbeds(EmbedMessageMaker.roleStats(event, data).build()).queue();
		} else {
			event.reply("This is not a valid role to get stats for").queue();
		}
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
		
		event.replyEmbeds(EmbedMessageMaker.playerStats(member, data, iconIDs[getCasteRoleIndex(member)]).build()).queue();
	}

	public void leaderBoard(SlashCommandEvent event) {
		Function<Player, Integer> function;
		boolean showAll = false;
		switch(event.getSubcommandName()) {
		case "factions":
			leaderboardFaction(event);
			return;
		case "strength":
			function = Player::getStrength;
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "knowledge":
			function = Player::getKnowledge;
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "magic":
			function = Player::getMagic;
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "agility":
			function = Player::getAgility;
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "stamina":
			function = Player::getStamina;
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "gold":
			function = Player::getIntGold;
			break;
		case "wins":
			function = Player::getWins;
			break;
		case "losses":
			function = Player::getLosses;
			break;
		case "total":
			function = Player::getTotal;
			break;
		default:
			event.reply("Invalid stat. Valid stats are Strength, Knowledge, Magic, Agility, Stamina, Gold, Wins, Losses, Total and Factions").queue();
			return;
		}
		
		Comparator<Player> comparitor = Comparator.comparing(function);
		
		comparitor = Collections.reverseOrder(comparitor);
		LinkedList<Player> players = new LinkedList<>();
		HashMap<Player, String> idLink = new HashMap<>();
		
		for(File f : data.getFiles()) {
			Player current = data.loadSerialized(f.getName());
			players.add(current);
			idLink.put(current, f.getName());
		}
		
		Collections.sort(players, comparitor);
		
		EmbedBuilder eb = new EmbedBuilder();
		String stat = event.getSubcommandName();
		stat = stat.substring(0,1).toUpperCase() + stat.substring(1);
		eb.setTitle("Leaderboard for stat: " + stat);
		eb.setColor(new Color(102, 107, 14));
		if(showAll) {
			eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina Gold Wins/Losses");
			for(int i = 0; i < 10 && !players.isEmpty(); i++) {
				Player current = players.remove();
				Member member = event.getGuild().getMemberById(idLink.get(current));
				String icon = "";
				if(getCasteRoleIndex(member) != -1) {
					icon = " " + iconIDs[getCasteRoleIndex(member)];
				}
				eb.addField(member.getEffectiveName()
						+ icon, current.getCompactStats(), true);
			}
		} else {
			for(int i = 0; i < 10 && !players.isEmpty(); i++) {
				Player current = players.remove();
				Member member = event.getGuild().getMemberById(idLink.get(current));
				String icon = "";
				if(getCasteRoleIndex(member) != -1) {
					icon = " " + iconIDs[getCasteRoleIndex(member)];
				}
				eb.addField(member.getEffectiveName() + icon, function.apply(current) + "", true);
			}
		}
		
		event.replyEmbeds(eb.build()).queue();
	}

	public void distributeWealth(SlashCommandEvent event) {
		if(isKing(event.getMember())) {
			if(event.getChannel().getIdLong() != generalID) {
				event.reply("You can only distribute wealth in <#" + generalID + ">").queue();
				return;
			}
			// if the king is the person giving the command
			Role role = event.getOption("role").getAsRole();
			if(roleIDs.contains(role.getIdLong())) {
				// if the role is a valid role
				long goldAmount = event.getOption("gold").getAsLong();
				if(goldAmount > 0) {
					Player king = data.loadSerialized(event.getMember().getId());
					if(king.getGold() >= goldAmount) {
						// if the king has enough gold to give
						
						LinkedList<Player> players = new LinkedList<>();
						HashMap<Player, String> idLinks = new HashMap<>();
						for(Member m : event.getGuild().getMembersWithRoles(role)) {
							Player player = data.loadSerialized(m.getId());
							players.add(player);
							idLinks.put(player, m.getId());
						}
						// Take the gold from the king
						king.decreaseGold(goldAmount);
						long initialAmount = goldAmount;
						// Randomly give each player a gold until the pool is empty
						while(goldAmount > 0) {
							players.get((int)(Math.random() * players.size())).increaseGold(1);
							goldAmount--;
						}
						
						while(players.size() > 0) {
							Player player = players.remove();
							data.saveSerialized(player, idLinks.get(player));
						}
						
						data.saveSerialized(king, event.getMember().getId());
						String iconURL = "";
						if(role.getIcon() != null) {
							iconURL = role.getIcon().getIconUrl();
						}
						event.replyEmbeds(EmbedMessageMaker.distributeGold(initialAmount, role.getName(), iconURL).build()).queue();
					} else {
						event.reply("You do not have enough gold to give this much").queue();
					}
				} else {
					event.reply("You cannot give negative gold!").queue();
				}
			} else {
				event.reply("This is not a valid role to give your gold too").queue();
			}
		} else {
			event.reply("Only the king can use this command").queue();
		}
	}
	
	public void submitTax(SlashCommandEvent event) {
		if(isKing(event.getMember())) {
			
			if(event.getChannel().getIdLong() != generalID) {
				event.reply("You can only tax in <#" + generalID + ">").queue();
				return;
			}
			
			// if the king is the person giving the command
			Role role = event.getOption("role").getAsRole();
			if(roleIDs.contains(role.getIdLong())) {
				// if the role is a valid role
				long goldAmount = event.getOption("gold").getAsLong();
				if(goldAmount > 0) {
					if(goldAmount <= 10) {
						System.out.println(1);
						Tax tax = new Tax((int)goldAmount, role.getIdLong());
						taxData.saveSerialized(tax, "tax");
						String iconURL = "";
						if(role.getIcon() != null) {
							iconURL = role.getIcon().getIconUrl();
						}
						event.replyEmbeds(EmbedMessageMaker.proposeTax(goldAmount, role.getName(), iconURL).build()).queue();
					} else {
						event.reply("You cannot tax more than 10 gold").queue();
					}
				} else {
					event.reply("You cannot tax negative gold!").queue();
				}
			} else {
				event.reply("This is not a valid role to give your gold too").queue();
			}
		} else {
			event.reply("Only the king can use this command").queue();
		}
	}
	
	public void honorablePromotion(SlashCommandEvent event) {
		if(isKing(event.getMember())) {
			
			if(event.getChannel().getIdLong() != generalID) {
				event.reply("You can only promote in <#" + generalID + ">").queue();
				return;
			}
			
			Member member1 = event.getOption("citizen-one").getAsMember();
			Member member2 = event.getOption("citizen-two").getAsMember();
			if(!member1.getUser().isBot() && !member2.getUser().isBot()) {
				if(getCasteRoleIndex(member1) != -1 && getCasteRoleIndex(member2) != -1 && getCasteRoleIndex(member1) != getCasteRoleIndex(member2)) {
					if(member1.getIdLong() != member2.getIdLong()) {
						if(hpData.getFiles().length == 0 || !hpData.loadSerialized("hp").isUsedToday()) {
							hpData.saveSerialized(new HonorablePromotion(true), "hp");
							Role r1 = getCasteRole(member1);
							Role r2 = getCasteRole(member2);
							guild.removeRoleFromMember(member1, r1).queue();
							guild.addRoleToMember(member1, r2).queue();
							guild.removeRoleFromMember(member2, r2).queue();
							guild.addRoleToMember(member2, r1).queue();
							event.replyEmbeds(EmbedMessageMaker.honorablePromotion(member1, member2).build()).queue();
							if(getCasteRoleIndex(member2) == 0) {
								newKing(member1);
							} else if(getCasteRoleIndex(member1) ==0 ) {
								newKing(member2);
							}
						} else {
							event.reply("You can only use this once per day!").queue();
						}
					} else {
						event.reply("You cannot swap the role of the same citizen").queue();
					}
				} else {
					event.reply("Both citizens must be in a caste, but not the same caste").queue();
				}
			} else {
				event.reply("Both citizens must not be a bot").queue();
			}
		} else {
			event.reply("Only the king can use this command").queue();
		}	
	}
	
	public void payCitizen(SlashCommandEvent event) {
		if(event.getChannel().getIdLong() != generalID) {
			event.reply("You can only pay citizens in <#" + generalID + ">").queue();
			return;
		}
		Member citizen = event.getOption("citizen").getAsMember();
		if(!citizen.getUser().isBot()) {
			if(citizen.getIdLong() != event.getMember().getIdLong()) {
				long gold = event.getOption("gold").getAsLong();
				if(gold > 0) {
					Player giver = data.loadSerialized(event.getMember().getId());
					if(gold <= giver.getGold()) {
						Player taker = data.loadSerialized(citizen.getId());
						giver.decreaseGold(gold);
						taker.increaseGold(gold);
						data.saveSerialized(giver, event.getMember().getId());
						data.saveSerialized(taker, citizen.getId());
						event.replyEmbeds(EmbedMessageMaker.giveGold(event.getMember().getEffectiveName(), citizen.getEffectiveName(), gold).build()).queue();
					} else {
						event.reply("You do not have that much gold to give").queue();
					}
				} else {
					event.reply("You must give a positive amount of gold").queue();
				}
			} else {
				event.reply("You cannot give yourself gold, but I appreciate your creativity").queue();
			}
		} else {
			event.reply("You cannot give a robot gold").queue();
		}
	}
	
	private void leaderboardFaction(SlashCommandEvent event) {
		HashMap<String, Integer> rolePop = new HashMap<>();
		for(long id: roleIDs) {
			Role r = guild.getRoleById(id);
			rolePop.put(r.getName(), guild.getMembersWithRoles(r).size());
		}
		event.replyEmbeds(EmbedMessageMaker.leaderboardFaction(rolePop).build()).queue();
	}

	private void newKing(Member king) {
		hpData.saveSerialized(new HonorablePromotion(false), "hp");
		generalChannel.sendMessageEmbeds(EmbedMessageMaker.newKing(king.getEffectiveName(), king.getAvatarUrl()).build()).queue();
	}
	
	private boolean isKing(Member member) {
		return getCasteRoleIndex(member) == 0;
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
	
	private FightResults fight(Player attacker, EncounterPlayer ep) {
		Player defender = new Player(ep);
		return fight(attacker, defender, 0, 0, false);
	}
	
	private FightResults fight(Player attacker, Player defender, int defenderPadding, int boosterPadding, boolean isKingDefending) {
		
		attacker.hasChallenged();
		if(!isKingDefending) {
			defender.wasChallenged();
		}
		
		int stamina = attacker.getStamina() + defender.getStamina() + defenderPadding * paddingMultiplier + boosterPadding;
		int strength = attacker.getStrength() + defender.getStrength() + defenderPadding * paddingMultiplier + boosterPadding;
		int magic = attacker.getMagic() + defender.getMagic() + defenderPadding * paddingMultiplier + boosterPadding;
		int agility = attacker.getAgility() + defender.getAgility() + defenderPadding * paddingMultiplier + boosterPadding;
		int knowledge = attacker.getKnowledge() + defender.getKnowledge() + defenderPadding * paddingMultiplier + boosterPadding;
		
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
		
		FightResults results = fight(attacker, defender, defenderPadding, booster, getCasteRoleIndex(defenderMember) == 0);
		
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

			if(getCasteRoleIndex(defenderMember) == 0) {
				newKing(attackerMember);
			}
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
			eb.setDescription(attackerMember.getEffectiveName() +  " vs " + defenderMember.getEffectiveName() + "\nBetter luck next time. " + statChanged + " has been increased by " + statNumChanged + " points."
					+ " Gold lost: " + goldLost);
			eb.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(), false);
			eb.setTimestamp(Instant.now());
			
			event.replyEmbeds(eb.build()).queue();
		}
		
		data.saveSerialized(attacker, attackerMember.getId());
		data.saveSerialized(defender, defenderMember.getId());
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
	
	private boolean canChallengeKing(long id) {
		return kingData.loadSerialized("king").canFight(id);
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
		} else if (message.contains("!reset-activities")) {
			logger.info("Resetting activities");
			for(File f : data.getFiles()) {
				Player player = data.loadSerialized(f.getName());
				player.newDay();
				data.saveSerialized(player, f.getName());
			}
			KingPlayer kp = kingData.loadSerialized("king");
			kp.resetList();
			kingData.saveSerialized(kp, "king");
			event.getPrivateChannel().sendMessage("Reset activities").queue();
		} else if(message.contains("!roll-encounter")) {
			logger.info("Rolling random activity");
			rollEncounter();
			event.getPrivateChannel().sendMessage("Rolled encounter").queue();
		} else if(message.contains("!roll-activity")) {
			String left = message.replace("!roll-activity", "");
			if(left.length() > 0) {
				for(int i = 0; i < Integer.parseInt(left.trim()); i++) {
					logger.info("Rolling random encounter");
					rollActivity();
				}
			} else {
				logger.info("Rolling random encounter");
				rollActivity();
			}
			event.getPrivateChannel().sendMessage("Rolled activity").queue();
		} else if(message.contains("!new-day")){
			logger.info("Its a new day!");
			dayPassed();
			event.getPrivateChannel().sendMessage("Its a new day!").queue();
		}
	}
	
	private void rollActivity() {
		int change = 5;
		int actionCost = 2;
		int goldCost = 9 + (int)DiceRollingSimulator.rollDice(4, 4);
		ActivityReward reward = ActivityReward.random();
		if(reward == ActivityReward.Gold) {
			change = (int)DiceRollingSimulator.rollDice(3, 8);
			Random r = new Random(System.currentTimeMillis());
			if(r.nextBoolean()) {
				actionCost = 2;
				change *= 2;
			} else {
				actionCost = 1;
			}
			goldCost = 0;
		}
		
		Activity item = new Activity(actionCost, change, goldCost, reward);
		Clock c = Clock.systemUTC();
		c = Clock.offset(c, Duration.ofDays(activityDuration / 2));
		activitiesChannel.sendMessageEmbeds(EmbedMessageMaker.activity(item, c).build()).queue(message -> {
			long id = message.getIdLong();
			activityData.saveSerialized(item, id + "");
			message.addReaction(encountersChannel.getGuild().getEmoteById(fightEmojiID)).queue();
		});
		
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
		
		eb.addField("Strength", strength + "", true);
		eb.addField("Knowledge", knowledge + "", true);
		eb.addField("Magic", magic + "", true);
		eb.addField("Agility", agility + "", true);
		eb.addField("Stamina", stamina + "", true);

		eb.setImage("https://zgamelogic.com/downloads/" + tier + "-" + types.get(type) + ".png");
		
		eb.setTitle("A " + tiers.get(tier) + " " + types.get(type) + " challenges the kingdom!");
		eb.setColor(new Color(56, 79, 115));
				
		eb.setFooter(encounterID + "");

		encountersChannel.sendMessageEmbeds(eb.build()).queue(message -> {
			baddy.setEncounterID(message.getIdLong());
			encounterData.saveSerialized(baddy, baddy.getEncounterID() + "");
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
	
	private void randomRolls() {
		while(true) {
			// Delete old arrivals
			for(File f : activityData.getFiles()){
				activitiesChannel.retrieveMessageById(f.getName()).queue(message -> {
					OffsetDateTime departTime = message.getEmbeds().get(0).getTimestamp();
					OffsetDateTime now = OffsetDateTime.now();
					if(now.isAfter(departTime)) {
						// its time to depart
						message.delete().queue();
						activityData.delete(f.getName());
					}
				});
			}
			
			if(((int)(Math.random() * activitySpawnChance * 4)) == 1) {
				rollActivity();
			}
			
			Calendar date = new GregorianCalendar();
			if(date.get(Calendar.MINUTE) % 15 == 0) {
				// n% chance to spawn an encounter
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
	
	private void payTax() {
		if(taxData.getFiles().length > 0) {
			Tax tax = taxData.loadSerialized("tax");
			Member kingMember = guild.getMembersWithRoles(guild.getRoleById(kingRoleID)).get(0);
			Player king = data.loadSerialized(kingMember.getId());
			for(Member m : guild.getMembersWithRoles(guild.getRoleById(tax.getRoleID()))) {
				if(!m.getUser().isBot()) {
					Player p = data.loadSerialized(m.getId());
					int payAmount = tax.getTaxAmount();
					if(p.getGold() < payAmount) {
						payAmount = p.getIntGold();
					}
					p.decreaseGold(payAmount);
					king.increaseGold(payAmount);
					data.saveSerialized(p, m.getId());
				}
			}
			data.saveSerialized(king, kingMember.getId());
			taxData.delete("tax");
		}
	}
	
	private void dailyGoldIncrease() {
		for(File f : data.getFiles()) {
			Player p = data.loadSerialized(f.getName());
			if(p.getHasChallengedToday() != 0) {
				p.increaseGold(DiceRollingSimulator.rollDice(2, 5));
			}
			p.newDay();
			if(isKing(f.getName())) {
				p.increaseGold((int)(Math.random() * 300) + 150);
			}
			data.saveSerialized(p, f.getName());
		}
	}
	
	private void dayPassedEncounter() {
		for(File f : encounterData.getFiles()) {
			EncounterPlayer ep = encounterData.loadSerialized(f.getName());
			boolean delete = ep.dayPassed();
			if(delete) {
				encountersChannel.retrieveMessageById(ep.getEncounterID()).queue(message -> {
					message.delete().queue();
				});
				encounterData.delete(f.getName());
			} else {
				Message m = encountersChannel.retrieveMessageById(ep.getEncounterID()).complete();
				EmbedBuilder eb = new EmbedBuilder(m.getEmbeds().get(0));
				eb.setDescription("This encounter is " + ep.getDaysOld() + " day(s) old");
				m.editMessageEmbeds(eb.build()).queue();
				encounterData.saveSerialized(ep, f.getName());
			}
		}
	}
	
	private void dayPassed() {
		payTax();
		dailyGoldIncrease();
		dayPassedEncounter();
		hpData.saveSerialized(new HonorablePromotion(false), "hp");
		
		KingPlayer kp = kingData.loadSerialized("king");
		kp.resetList();
		kingData.saveSerialized(kp, "king");
		
		LinkedList<String> starts = new LinkedList<>();
		starts.add("The sun rises on our wonderful kingdom once again.");
		starts.add("The light shines through the stained glass portrait of myself, the king/queen.");
		starts.add("Like honey slowly drooping, light bathes the kingdom in its golden glow.");
		starts.add("The bees buzz with content as a night of rest comes to an end.");
		starts.add("New day, new prospects.");
		starts.add("Like an angry hive, the denizens buzz excitedly for the start of a new day.");
		starts.add("Good morning my children!");
		starts.add("The glint from my crown awakens me.");
		starts.add("Light begins to pour into the streets below.");
		starts.add("Chiming sounds throughout the castle halls as the sun rises above the horizon.");
		starts.add("A rooster bores its noise into the ears of all who were in the village, abruptly seizing as a morning�s hunger sets in.");

		LinkedList<String> endings = new LinkedList<>();
		endings.add(" I can already hear swords being drawn");
		endings.add(" I wonder what threats we shall see today");
		endings.add(" I can hear the birds chirping, the cows mooing, and my children already beating the crap out of each other");
		endings.add(" Is the red color in the streets new?");
		endings.add(" Another day, another bloodbath");
		endings.add(" *Screams of agony* .....Wonderful");
		endings.add(" Squire, does any of this really matter? Like really *really* matter?");
		endings.add(" Is shlongbot opposed to all the violence or does it like to watch it like some festivities?");
		endings.add(" The church of shlongbot should be crowded today. A lot of questionable things happened yesterday...");
		endings.add(" Will the monarchy be overthrown today? Or perhaps the communist revolution? I can hardly wait to find out");
		endings.add(" Nothing like the scent of fresh stats in the morning");
		endings.add(" Do keep it down today children, we do not want to upset the bees");
		endings.add(" Revealed are hordes of undefeated monsters. Perhaps you should get on that?");
		
		String message = starts.get((int)(Math.random() * starts.size())) + endings.get((int)(Math.random() * endings.size()));
		
		generalChannel.sendMessageEmbeds(EmbedMessageMaker.goodMorningMessage(message).build()).queue();
	}
	
	private boolean isKing(String playerID) {
		Member player = guild.getMemberById(playerID);
		for(Role r : player.getRoles()) {
			if(r.getIdLong() == kingRoleID) {
				return true;
			}
		}
		return false;
	}
	
	private void midnightReset() {
		while (true) {
			Calendar date = new GregorianCalendar();
			if(date.get(Calendar.HOUR) == 0 && date.get(Calendar.MINUTE) == 0) {
				logger.info("A new day rises on the kingdom");
				dayPassed();
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
