package bot.role;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.Scanner;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import bot.role.data.FightResults;
import bot.role.logging.DailyLogger;
import bot.role.logging.EndOfDayLogger;
import bot.role.logging.FightLogger;
import controllers.EmbedMessageMaker;
import controllers.dice.DiceRollingSimulator;
import data.ConfigLoader;
import data.database.arena.achievements.Achievements;
import data.database.arena.activity.Activity;
import data.database.arena.activity.Activity.ActivityReward;
import data.database.arena.activity.ActivityRepository;
import data.database.arena.encounter.Encounter;
import data.database.arena.encounter.EncounterRepository;
import data.database.arena.item.Item;
import data.database.arena.item.Item.Rarity;
import data.database.arena.item.Item.StatType;
import data.database.arena.misc.GameInformation;
import data.database.arena.misc.GameInformationRepository;
import data.database.arena.player.Player;
import data.database.arena.player.PlayerRepository;
import data.database.arena.shopItem.ShopItem;
import data.database.arena.shopItem.ShopItemRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleBotListener extends ListenerAdapter {
	
	private PlayerRepository playerData;
	private GameInformationRepository gameData;
	private ShopItemRepository shopItemData;
	private EncounterRepository encounterData;
	private ActivityRepository activityData;
	
	public static int dailyChallengeLimit;
	public static int dailyDefendLimit;
	
	private final int paddingMultiplier;
	private final int statBaseChange;
	private final int statRandomChange;
	private final int encounterStatMultiplier;
	
	private Logger logger = LoggerFactory.getLogger(RoleBotListener.class);

	private long guildID;
	private static long kingRoleID;
	private LinkedList<Long> roleIDs;
	private final int boosterChange;
	private long encountersID;
	private long fightEmojiID;
	private long remindMessageID;
	private String[] iconIDs;
	private long generalID;
	private long activitiesID;
	private long itemsID;
	private long spawnChance;
	private int shopDuration;
	private int itemSpawnChance;
	
	private long fiveGoldID, tenGoldID, fiftyGoldID;
	
	private long activitySpawnChance;
	private long activityDuration;
	
	private static Guild guild;
	
	private TextChannel encountersChannel;
	private TextChannel activitiesChannel;
	private TextChannel itemsChannel;
	private TextChannel generalChannel;
	
	public RoleBotListener(ConfigLoader cl, PlayerRepository playerData, GameInformationRepository gameData, ShopItemRepository shopItemData, EncounterRepository encounterData, ActivityRepository activityData) {
		this.playerData = playerData;
		this.gameData = gameData;
		this.shopItemData = shopItemData;
		this.encounterData = encounterData;
		this.activityData = activityData;
		
		guildID = cl.getGuildID();
		roleIDs = cl.getRoleIDs();
		fiveGoldID = cl.getFiveGoldID();
		tenGoldID = cl.getTenGoldID();
		fiftyGoldID = cl.getFiftyGoldID();
		kingRoleID = cl.getKingRoleID();
		encountersID = cl.getEncountersID();
		generalID = cl.getGeneralID();
		activitiesID = cl.getActivitiesID();
		itemsID = cl.getItemsID();
		boosterChange = cl.getBoosterChange();
		fightEmojiID = cl.getFightEmojiID();
		remindMessageID = cl.getRemindMessageID();
		shopDuration = cl.getShopDuration();
		itemSpawnChance = cl.getItemSpawnChance();
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
		itemsChannel = guild.getTextChannelById(itemsID);
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
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		playerData.deleteById(event.getMember().getIdLong());
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.isFromType(ChannelType.PRIVATE)) {
			if(event.getAuthor().getIdLong() == 232675572772372481l) {
				String message = event.getMessage().getContentRaw();
				processAdminCommand(message, event);
			} 
			
			if (event.getAuthor().getIdLong() == 232675572772372481l || event.getAuthor().getIdLong() == 236337128005566464l) {
				String message = event.getMessage().getContentRaw();
				processCommand(message, event);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(!event.getUser().isBot()) {
			long channelID = event.getChannel().getIdLong();
			long reactionID = event.getReactionEmote().getIdLong();
			if(channelID == encountersID || channelID == itemsID || channelID == activitiesID || channelID == event.getGuild().getRulesChannel().getIdLong()) {
				if(reactionID == fightEmojiID) {
					if(channelID == encountersID) {
						encounterReact(event);
					} else if(channelID == activitiesID) {
						activityReact(event);
					} else if(channelID == event.getGuild().getRulesChannel().getIdLong()) {
						if(event.getMessageIdLong() == remindMessageID) {
							reminderReact(event);
						}
					} else if(channelID == itemsID) {
						itemsReact(event);
					} 
				} else if(channelID == itemsID && (reactionID == fiveGoldID || reactionID == tenGoldID || reactionID == fiftyGoldID)) {
					itemsReact(event);
				}
			} 
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		if(!event.getUser().isBot()) {
			if(event.getReactionEmote().getIdLong() == fightEmojiID) {
				if(event.getChannel().getIdLong() == event.getGuild().getRulesChannel().getIdLong()) {
					if(event.getMessageIdLong() == remindMessageID) {
						reminderReactRemove(event);
					}
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
		
		Player attacker = playerData.findById(attackerMember.getIdLong()).get();
		Player defender = playerData.findById(defenderMember.getIdLong()).get();
		if(!defenderMember.getUser().isBot()) {
			if(attacker.canChallenge()) {
				if(attackerMember.getIdLong() != defenderMember.getIdLong()) {
					if((defender.canDefend() && getCasteRoleIndex(defenderMember) != 0) || (getCasteRoleIndex(defenderMember) == 0 && canChallengeKing(attackerMember.getIdLong()))) {
						if(getCasteRoleIndex(defenderMember) == 0) {
							GameInformation info = gameData.findById("game_data").get();
							info.addPlayerKingFought(attackerMember.getIdLong());
							gameData.save(info);
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
							if(attackIndex != 1 && attackIndex != 2) {
								padding += attackIndex;
							}
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
			event.replyEmbeds(EmbedMessageMaker.roleStats(event, playerData).build()).queue();
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
		Player player = playerData.findById(member.getIdLong()).get();
		event.replyEmbeds(EmbedMessageMaker.playerStats(member, player, iconIDs[getCasteRoleIndex(member)]).build()).queue();
	}

	public void leaderBoard(SlashCommandEvent event) {
		String column = "";
		boolean showAll = false;
		Function<Player, Integer> function;
		
		switch(event.getSubcommandName()) {
		case "factions":
			leaderboardFaction(event);
			return;
		case "activities":
			leaderboardActivities(event);
			return;
		case "total":
			leaderboardTotal(event);
			return;
		case "strength":
			function = Player::getRawStrength;
			column = event.getSubcommandName();
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "knowledge":
			function = Player::getRawKnowledge;
			column = event.getSubcommandName();
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "magic":
			function = Player::getRawMagic;
			column = event.getSubcommandName();
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "agility":
			function = Player::getRawAgility;
			column = event.getSubcommandName();
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "stamina":
			function = Player::getRawStamina;
			column = event.getSubcommandName();
			if(event.getOption("show-all") != null) {
				showAll = event.getOption("show-all").getAsBoolean();
			}
			break;
		case "gold":
			function = Player::getIntGold;
			column = event.getSubcommandName();
			break;
		case "wins":
			function = Player::getWins;
			column = event.getSubcommandName();
			break;
		case "losses":
			function = Player::getLosses;
			column = event.getSubcommandName();
			break;
		default:
			event.reply("Invalid stat. Valid stats are Strength, Knowledge, Magic, Agility, Stamina, Gold, Wins, Losses, Total and Factions").queue();
			return;
		}
		
		List<Player> players = playerData.findAll(Sort.by(Sort.Direction.DESC, column));
		
		EmbedBuilder eb = new EmbedBuilder();
		String stat = event.getSubcommandName();
		stat = stat.substring(0,1).toUpperCase() + stat.substring(1);
		eb.setTitle("Leaderboard for stat: " + stat);
		eb.setColor(new Color(102, 107, 14));
		if(showAll) {
			eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina Gold Wins/Losses");
			for(int i = 0; i < 10 && !players.isEmpty(); i++) {
				Player current = players.remove(0);
				Member member = event.getGuild().getMemberById(current.getId());
				String icon = "";
				if(getCasteRoleIndex(member) != -1) {
					icon = " " + iconIDs[getCasteRoleIndex(member)];
				}
				eb.addField(member.getEffectiveName()
						+ icon, current.getCompactStats(), true);
			}
		} else {
			for(int i = 0; i < 10 && !players.isEmpty(); i++) {
				Player current = players.remove(0);
				Member member = event.getGuild().getMemberById(current.getId());
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
					Player king = playerData.findById(event.getMember().getIdLong()).get();
					if(king.getGold() >= goldAmount) {
						// if the king has enough gold to give
						
						LinkedList<Player> players = new LinkedList<>();
						HashMap<Player, String> idLinks = new HashMap<>();
						for(Member m : event.getGuild().getMembersWithRoles(role)) {
							Player player = playerData.findById(m.getIdLong()).get();
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
							playerData.save(player);
						}
						playerData.save(king);
						String iconURL = "";
						if(role.getIcon() != null) {
							iconURL = role.getIcon().getIconUrl();
						}
						DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has given " + goldAmount + " gold to " + role.getName() + "s");
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
					if(goldAmount <= 7) {
						Player king = playerData.findById(event.getMember().getIdLong()).get();
						if(king.getHasChallengedToday() < dailyChallengeLimit) {
							king.hasChallenged();
							playerData.save(king);
							GameInformation game = gameData.findById("game_data").get();
							game.setTax(role.getIdLong(), (int)goldAmount);
							gameData.save(game);
							String iconURL = "";
							if(role.getIcon() != null) {
								iconURL = role.getIcon().getIconUrl();
							}
							DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has set a tax on " + role.getName() + "s for " + goldAmount + " gold");
							event.replyEmbeds(EmbedMessageMaker.proposeTax(goldAmount, role.getName(), iconURL).build()).queue();
						} else {
							event.reply("You do not have enough time to do this today!").queue();
						}
					} else {
						event.reply("You cannot tax more than 7 gold").queue();
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
						Player king = playerData.findById(event.getMember().getIdLong()).get();
						if(king.getHasChallengedToday() < dailyChallengeLimit) {
							king.hasChallenged();
							playerData.save(king);
							
							DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has declared a role swap\n"
									+ "\t" + getNameWithCaste(member1) + " has swapped with " + getNameWithCaste(member2));
							
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
							event.reply("You do not have enough time to do this today!").queue();
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
					Player giver = playerData.findById(event.getMember().getIdLong()).get();
					if(gold <= giver.getGold()) {
						Player taker = playerData.findById(citizen.getIdLong()).get();
						giver.decreaseGold(gold);
						taker.increaseGold(gold);
						playerData.save(giver);
						playerData.save(taker);
						DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has paid " + getNameWithCaste(citizen) + " " + gold + " gold");
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
	
	public void getDayHistory(SlashCommandEvent event) {
		File file;
		if(event.getOption("specific-day") != null) {
			file = DailyLogger.getFile(event.getOption("specific-day").getAsString());
		} else {
			file = DailyLogger.getCurrentFile();
		}
		
		if(file != null) {
			event.reply("Let me go to the library to get that history for you").queue();
			event.getTextChannel().sendFile(file).queue();
		} else {
			event.reply("There doesn't seem to be any data in the library for that day").queue();
		}
	}

	public void sendAchievements(SlashCommandEvent event) {
		Member member;
		if(event.getOption("player") != null) {
			member = event.getOption("player").getAsMember();
		} else {
			member = event.getMember();
		}
		Player player = playerData.findById(member.getIdLong()).get();
		event.replyEmbeds(EmbedMessageMaker.playerAchievement(getNameWithCaste(member), player.getAchievements()).build()).queue();
	}
	
	public static String getKing() {
		return guild.getMembersWithRoles(guild.getRoleById(kingRoleID)).get(0).getUser().getName();
	}

	public String audit(long long1) throws JSONException {
		String output = "";
		Member m = guild.getMemberById(long1);
		if(m != null) {
			for(File f : EndOfDayLogger.getDir().listFiles()) {
				try {
					Scanner in = new Scanner(f);
					while(in.hasNextLine()) {
						String line = in.nextLine();
						if(line.contains(m.getUser().getName())) {
							output += f.getName() + " " + line + "\n";
						}
					}
					in.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
		} else {
			output = "Member not found";
		}
		return output;
	}
	
	public JSONObject getPlayerList() throws JSONException {
		JSONArray jarray = new JSONArray();
		for(Member m : guild.getMembers()) {
			JSONObject member = new JSONObject();
			member.put("user_name", m.getUser().getName());
			member.put("user_id", m.getIdLong());
			jarray.put(member);
		}
		JSONObject j = new JSONObject();
		j.put("player_names", jarray);
		return j;
	}

	public void passLaw(SlashCommandEvent event) {
		if(isKing(event.getMember())) {
			GameInformation game = gameData.findById("game_data").get();
			int reign = game.getKingRun();
			if(reign >= 4) {
				game.setKingRun(0);
				gameData.save(game);
				event.reply("The church of shlongbot will review this request. Thank you and Shlongbot bless.").queue();
				event.getGuild().getMemberById(232675572772372481l).getUser().openPrivateChannel().complete().sendMessage("The kind has delcared a law:\n"
						+ event.getOption("law").getAsString()).queue();
			} else {
				event.reply("You must be king at the start of 4 days in a row before you can use this command!").queue();
			}
		} else {
			event.reply("Only the king can use this command!").queue();
		}
	}

	private void leaderboardTotal(SlashCommandEvent event) {
		List<Player> players = playerData.findAll();
		Comparator<Player> comparitor = Comparator.comparing(Player::getTotal);
		comparitor = Collections.reverseOrder(comparitor);
		EmbedBuilder eb = new EmbedBuilder();
		String stat = event.getSubcommandName();
		stat = stat.substring(0,1).toUpperCase() + stat.substring(1);
		eb.setTitle("Leaderboard for stat: " + stat);
		eb.setColor(new Color(102, 107, 14));
		Collections.sort(players, comparitor);
		
		for(int i = 0; i < 10 && !players.isEmpty(); i++) {
			Player current = players.remove(0);
			Member member = event.getGuild().getMemberById(current.getId());
			String icon = "";
			if(getCasteRoleIndex(member) != -1) {
				icon = " " + iconIDs[getCasteRoleIndex(member)];
			}
			eb.addField(member.getEffectiveName() + icon, current.getTotal() + "", true);
		}
		
		
		event.replyEmbeds(eb.build()).queue();
	}

	private void itemsReact(MessageReactionAddEvent event) {
		ShopItem item = shopItemData.findById(event.getMessageIdLong()).get();
		if(item.getItem().getRarity() == Item.Rarity.MYTHIC) {
			mythicItemReact(event, item);
		} else {
			itemReact(event, item);
		}
	}

	private void itemReact(MessageReactionAddEvent event, ShopItem item) {
		int goldCost = item.getCost();
		Player player = playerData.findById(event.getMember().getIdLong()).get();
		if(player.getGold() >= goldCost) {
			DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has purched a " + item.getItem().getItemName());
			player.setItem(item.getItem());
			player.decreaseGold(goldCost);
			playerData.save(player);
			generalChannel.sendMessage("<@" + event.getUserId() + ">, congratulations on your new purchase of " + item.getItem().getItemName()).queue();
			DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has purchased " + item.getItem().getItemName());
		} else {
			generalChannel.sendMessage("<@" + event.getUserId() + ">, you do not have enough gold to pay the shop keep.").queue();
		}
	}

	private void mythicItemReact(MessageReactionAddEvent event, ShopItem item) {
		int goldCost = item.getCost();
		final int incrementAmount;
		long emoteID = event.getReactionEmote().getIdLong();
		if(emoteID == fiftyGoldID) {
			incrementAmount = 50;
		} else if(emoteID == tenGoldID) {
			incrementAmount = 10;
		} else {
			incrementAmount = 5;
		}
		Player player = playerData.findById(event.getMember().getIdLong()).get();
		if(player.getGold() >= goldCost + incrementAmount || (player.getId() == item.getCurrentBidder() && player.getGold() >= incrementAmount)) {			
			event.retrieveMessage().queue(message -> {
				if(item.getCurrentBidder() == player.getId()) {
					player.decreaseGold(incrementAmount);
				} else if(item.getCurrentBidder() != 0) {
					Player previous = playerData.findById(item.getCurrentBidder()).get();
					previous.setGold(previous.getGold() + goldCost);
					player.decreaseGold(goldCost + incrementAmount);
					playerData.save(previous);
				}
				item.setCost(goldCost + incrementAmount);
				item.setCurrentBidder(player.getId());
				EmbedBuilder eb = new EmbedBuilder(message.getEmbeds().get(0));
				List<Field> fields = eb.getFields();
				fields.remove(1);
				fields.remove(1);
				fields.add(new Field("Current bid", item.getCost() + "", true));
				fields.add(new Field("Current bidder", event.getMember().getEffectiveName(), true));
				message.editMessageEmbeds(eb.build()).queue();
				shopItemData.save(item);
				playerData.save(player);
				message.removeReaction(event.getReactionEmote().getEmote(), event.getUser()).queue();
			});
			
		} else {
			generalChannel.sendMessage("<@" + event.getUserId() + ">, you do not have enough gold to bid on this item.").queue();
			event.retrieveMessage().queue(message -> {
				message.removeReaction(event.getReactionEmote().getEmote(), event.getUser()).queue();
			});
		}
		
	}

	private void leaderboardActivities(SlashCommandEvent event) {
		event.replyEmbeds(EmbedMessageMaker.activityLeaderboard(playerData.findAll(), event.getGuild()).build()).queue();
	}

	private void reminderReact(MessageReactionAddEvent event) {
		logger.info(event.getMember().getEffectiveName() + " has opted in to reminders");
		GameInformation game = gameData.findById("game_data").get();
		game.addDailyID(event.getMember().getIdLong());
		gameData.save(game);
		event.getUser().openPrivateChannel().queue(channel -> {
			channel.sendMessage("You have opted into receiving a message when you have not done any activities for the day.").queue();
		});
	}

	private void reminderReactRemove(MessageReactionRemoveEvent event) {
		logger.info(event.getMember().getEffectiveName() + " has opted out of reminders");
		GameInformation game = gameData.findById("game_data").get();
		game.removeID(event.getMember().getIdLong());
		gameData.save(game);
		event.getUser().openPrivateChannel().queue(channel -> {
			channel.sendMessage("You have opted out of receiving a message when you have not done any activities for the day.").queue();
		});
	}

	/**
	 * @param event
	 */
	private void activityReact(MessageReactionAddEvent event) {
		event.retrieveMessage().queue(message -> {
			Activity activity = activityData.findById(message.getIdLong()).get();
			if(activity.canPlayerWork(event.getUserIdLong())) {
				Player p = playerData.findById(event.getUserIdLong()).get();
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
						if(activity.getReward() == ActivityReward.Gold) {
							DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has done an activity\n"
									+ "\tReward: " + activity.getReward().name() + " " + activity.getRewardAmount());
						} else {
							DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has done an activity\n"
									+ "\tReward: " + activity.getReward().name() + " " + activity.getRewardAmount() + "\n"
									+ "\tCost: " + activity.getGoldCost() + " gold");
						}
						checkForAchievements(p);
						playerData.save(p);
						activityData.save(activity);
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

	/**
	 * @param event
	 */
	private void encounterReact(MessageReactionAddEvent event) {
		event.retrieveMessage().queue(message -> {
			Encounter ep = encounterData.findById(message.getIdLong()).get();
			if(ep.canFightPlayer(event.getMember().getIdLong())) {
				Player p = playerData.findById(event.getUserIdLong()).get();
				if(p.canChallenge()) {
					fightEncounter(event.getMember(), ep, event);
					ep.addPlayerFought(event.getUserIdLong());
					encounterData.save(ep);
				} else {
					generalChannel.sendMessage("<@" + event.getUserId() + ">, you are too tired to fight again today").mention(event.getUser()).queue();
				}
			} else {
				generalChannel.sendMessage("<@" + event.getUserId() + ">, you have already fought this encounter.").mention(event.getUser()).queue();
			}
		});
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
		generalChannel.sendMessageEmbeds(EmbedMessageMaker.newKing(king.getEffectiveName(), king.getAvatarUrl()).build()).queue();
		DailyLogger.writeToFile("A new king has been appointed: " + getNameWithCaste(king));
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
		Player p = new Player(member.getIdLong(), strength, agility, knowledge, magic, stamina, gold, 0, 0, 0, 0, 0, null, 0, new Achievements());
		playerData.save(p);
		logger.info("Creating statis for " + member.getEffectiveName() + ": "
				+ strength + " " + agility + " " + knowledge + " " + magic + " " + stamina);
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
	
	private FightResults fight(Player attacker, Encounter ep) {
		Player defender = new Player(ep);
		return fight(attacker, defender, 0, 0, false);
	}
	
	private FightResults fight(Player attacker, Player defender, int defenderPadding, int boosterPaddingPercentage, boolean isKingDefending) {
		
		attacker.hasChallenged();
		if(!isKingDefending) {
			defender.wasChallenged();
		}
		
		int attackerStamina = attacker.getStamina();
		int attackerStrength = attacker.getStrength();
		int attackerMagic = attacker.getMagic();
		int attackerAgility = attacker.getAgility();
		int attackerKnowledge = attacker.getKnowledge();
		
		int paddingMultiplier = (int)((attacker.getTotal()) * (this.paddingMultiplier / 100.0));
		int boosterPadding = (int)((attacker.getTotal()) * (boosterPaddingPercentage / 100.0));
		
		int defenderStamina = defender.getStamina() + defenderPadding * paddingMultiplier + boosterPadding;
		int defenderStrength = defender.getStrength() + defenderPadding * paddingMultiplier + boosterPadding;
		int defenderMagic = defender.getMagic() + defenderPadding * paddingMultiplier + boosterPadding;
		int defenderAgility = defender.getAgility() + defenderPadding * paddingMultiplier + boosterPadding;
		int defenderKnowledge = defender.getKnowledge() + defenderPadding * paddingMultiplier + boosterPadding;
		
		int stamina = attackerStamina + defenderStamina;
		int strength = attackerStrength  + defenderStrength;
		int magic = attackerMagic + defenderMagic;
		int agility = attackerAgility + defenderAgility;
		int knowledge = attackerKnowledge + defenderKnowledge;
		
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
		
		return new FightResults(attackerPoint > defenderPoint, attackerPoint, defenderPoint,
				strength, streWin, magic, magiWin,
				agility, agilWin, knowledge, knowWin,
				stamina, stamWin, defenderPadding, boosterPadding, paddingMultiplier,
				attackerStrength, defenderStrength, attackerStamina, defenderStamina,
				attackerMagic, defenderMagic, attackerAgility, defenderAgility,
				attackerKnowledge, defenderKnowledge);
	}
	
	/**
	 * 
	 * @param challenger
	 * @param defender
	 */
	private void fight(Member attackerMember, Member defenderMember, int defenderPadding, SlashCommandEvent event) {
		Player attacker = playerData.findById(attackerMember.getIdLong()).get();
		Player defender = playerData.findById(defenderMember.getIdLong()).get();
		
		String logMessage = getNameWithCaste(attackerMember) + " has attacked " + getNameWithCaste(defenderMember) + "\n";
		
		int booster = 0;
		
		// Add a level of defending for the server boosters
		if(defenderMember.getTimeBoosted() != null) {
			booster += boosterChange;
		}
		
		FightResults results = fight(attacker, defender, defenderPadding, booster, getCasteRoleIndex(defenderMember) == 0);
		
		long fightID = new Random().nextLong();
		while(FightLogger.exists(fightID)) {
			fightID = new Random(System.currentTimeMillis()).nextLong();
		}
		
		FightLogger.timestampFight(fightID);
		FightLogger.writeToFile("Fight between " + getNameWithCaste(attackerMember) + " and " + getNameWithCaste(defenderMember), fightID);
		FightLogger.writeToFile(results.toString(), fightID);
		
		if(results.isAttackerWon()) {
			logMessage += "\tResults: Attacker won\n";
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
				logMessage += "\t" + attackerMember.getEffectiveName() + " is now a role of " + defenderRole.getName() + "\n";
				long goldWon = 7 + (int)(Math.random() * 3);
				if(defender.getGold() < goldWon) {
					goldWon = defender.getGold();
				}
				defender.decreaseGold(goldWon);
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_GOLD) {
					attacker.increaseGold(new Random().nextInt(attacker.getItem().getStatIncrease()) + attacker.getItem().getStatIncrease()/2);
				}
				attacker.increaseGold(goldWon);
				logMessage += "\tGold won: " + goldWon + "\n";
				eb.setDescription(attackerMember.getEffectiveName() +  " vs " + defenderMember.getEffectiveName() + "\n" + attackerMember.getEffectiveName() + " is now a rank of " + defenderRole.getName() + ". Gold obtained: " + goldWon);
			} else {
				long goldWon = 3 + (int)(Math.random() * 3);
				if(defender.getGold() < goldWon) {
					goldWon = defender.getGold();
				}
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_GOLD) {
					attacker.increaseGold(new Random().nextInt(attacker.getItem().getStatIncrease()) + attacker.getItem().getStatIncrease()/2);
				}
				defender.decreaseGold(goldWon);
				attacker.increaseGold(goldWon);
				logMessage += "\tGold won: " + goldWon + "\n";
				eb.setDescription(attackerMember.getEffectiveName() +  " vs " + defenderMember.getEffectiveName() + "\nGold obtained: " + goldWon);
			}
			
			
			eb.setTitle("Fight results: " + attackerMember.getEffectiveName() + " wins!");
			eb.setColor(new Color(25, 84, 43));
			eb.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(), false);
			eb.setTimestamp(Instant.now());
			eb.setFooter(fightID + "");
			event.replyEmbeds(eb.build()).queue();

			if(getCasteRoleIndex(defenderMember) == 0) {
				newKing(attackerMember);
			}
			
			// achievement stuff
			if(defender.getId() == 236262235612250142l) {
				attacker.getAchievements().setOneBirdWithOneStone(true);
			}
			attacker.getAchievements().progressCompletingTheRounds(getCasteRole(defenderMember).getIdLong());
			attacker.getAchievements().progressRunningTheGauntlet(defenderMember.getIdLong());
			// end achievement stuff
		} else {
			logMessage += "\tResults: Attacker lost\n";
			String roleSwap = "";
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
				logMessage += "\tGold lost: " + goldLost + "\n";
				defender.increaseGold(goldLost);
				if(defender.getItem() != null && defender.getItem().getItemType() == StatType.ACTIVE_GOLD) {
					defender.increaseGold(new Random().nextInt(defender.getItem().getStatIncrease()) + defender.getItem().getStatIncrease()/2);
				}
				attacker.decreaseGold(goldLost);
			} else {
				// if attacker was attacking down the caste
				goldLost = 7 + (int)(Math.random() * 3);
				if(attacker.getGold() < goldLost) {
					goldLost = attacker.getGold();
				}
				defender.increaseGold(goldLost);
				if(defender.getItem() != null && defender.getItem().getItemType() == StatType.ACTIVE_GOLD) {
					defender.increaseGold(new Random().nextInt(defender.getItem().getStatIncrease()) + defender.getItem().getStatIncrease()/2);
				}
				attacker.decreaseGold(goldLost);
				logMessage += "\tGold lost: " + goldLost + "\n";
				
				if(dif != 0) {
					// lower caste switch rolls 
					Guild guild = event.getGuild();
					Role attackerRole = getCasteRole(attackerMember);
					Role defenderRole = getCasteRole(defenderMember);
					guild.addRoleToMember(attackerMember, defenderRole).queue();
					guild.removeRoleFromMember(attackerMember, attackerRole).queue();
					
					guild.addRoleToMember(defenderMember, attackerRole).queue();
					guild.removeRoleFromMember(defenderMember, defenderRole).queue();
					
					logMessage += "\t" + attackerMember.getEffectiveName() + " is now a role of " + defenderRole.getName() + "\n";
					roleSwap = attackerMember.getEffectiveName() + " is now a rank of " + defenderRole.getName() + ".";
					if(getCasteRoleIndex(attackerMember) == 0) {
						newKing(defenderMember);
					}
				}
				
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
			String itemStatBuff = "";
			if(stamDif == statDif) {
				statChanged = "Stamina";
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_STAMINA) {
					attacker.increaseStamina(attacker.getItem().getStatIncrease());
					itemStatBuff = " (+" + attacker.getItem().getStatIncrease() + ")";
				}
				attacker.increaseStamina(statNumChanged);
			} else if (streDif == statDif) {
				statChanged = "Strength";
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_STRENGTH) {
					attacker.increaseStrength(attacker.getItem().getStatIncrease());
					itemStatBuff = " (+" + attacker.getItem().getStatIncrease() + ")";
				}
				attacker.increaseStrength(statNumChanged);
			} else if (magiDif == statDif) {
				statChanged = "Magic";
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_MAGIC){
					attacker.increaseMagic(attacker.getItem().getStatIncrease());
					itemStatBuff = " (+" + attacker.getItem().getStatIncrease() + ")";
				}
				attacker.increaseMagic(statNumChanged);
			} else if (agilDif == statDif) {
				statChanged = "Agility";
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_AGILITY) {
					attacker.increaseAgility(attacker.getItem().getStatIncrease());
					itemStatBuff = " (+" + attacker.getItem().getStatIncrease() + ")";
				}
				attacker.increaseAgility(statNumChanged);
			} else if (knowDif == statDif) {
				statChanged = "Knowledge";
				if(attacker.getItem() != null && attacker.getItem().getItemType() == StatType.ACTIVE_KNOWLEDGE) {
					attacker.increaseKnowledge(attacker.getItem().getStatIncrease());
					itemStatBuff = " (+" + attacker.getItem().getStatIncrease() + ")";
				}
				attacker.increaseKnowledge(statNumChanged);
			}
			logMessage += "\tStat gained: " + statChanged + " " + statNumChanged + "\n";
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Fight results: " + attackerMember.getEffectiveName() + " lost!");
			eb.setColor(new Color(84, 25, 25));
			eb.setDescription(attackerMember.getEffectiveName() +  " vs " + defenderMember.getEffectiveName() + "\nBetter luck next time. " + statChanged + " has been increased by " + statNumChanged + itemStatBuff + " points."
					+ " Gold lost: " + goldLost + "\n" + roleSwap);
			eb.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(), false);
			eb.setTimestamp(Instant.now());
			eb.setFooter(fightID + "");
			event.replyEmbeds(eb.build()).queue();
			attacker.getAchievements().progressPunchingBad(getCasteRole(defenderMember).getIdLong());
		}
		logMessage += "\tAttacker points: " + results.getAttackerPoints() + "\tDefender points: " + results.getDefenderPoints();
		DailyLogger.writeToFile(logMessage);
		
		checkForAchievements(attacker);
		checkForAchievements(defender);
		
		playerData.save(attacker);
		playerData.save(defender);
	}

	private void checkForAchievements(Player p) {
		Achievements a = p.getAchievements();
		if(a.getRunningTheGauntletProgress() >= guild.getMemberCount()) {
			//a.setRunningTheGauntlet(true);
		}
		if(a.getCompletingTheRoundsProgress() >= roleIDs.size() + 1) {
			a.setCompletingTheRounds(true);
		}
		if(a.getPunchingBagProgress() >= roleIDs.size() + 1) {
			a.setPunchingBag(true);
		}
		announcePlayerAchievments(p);
	}

	private void announcePlayerAchievments(Player p) {
		Member member = guild.getMemberById(p.getId());
		HashMap<String, String> as = p.getAchievements().getAnnounce();
		try {
			for(String key : as.keySet()) {
				generalChannel.sendMessage("<@" + p.getId() + ">").queue();
				generalChannel.sendMessageEmbeds(EmbedMessageMaker.achievement(member.getEffectiveName(),key, as.get(key)).build()).queue();
			}
		} catch(NullPointerException e) {
			
		}
		p.getAchievements().clearAnnounce();
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
	
	private String getCasteRoleName(Member member) {
		Role role = getCasteRole(member);
		if(role == null) {
			return "";
		}
		return role.getName();
	}
	
	private String getNameWithCaste(Member member) {
		if(member != null) {
			if(member.getUser() != null) {
				if(member.getUser().getName() != null) {
					return member.getUser().getName() + " (" + getCasteRoleName(member) + ")";
				}
			}
		}
		return "";
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
		return gameData.findById("game_data").get().canFightKing(id);
	}
	
	private void processCommand(String message, MessageReceivedEvent event) {
		if(message.contains("!fight-summary")) {
			message = message.replace("!fight-summary ", "");
			try {
				long id = Long.parseLong(message);
				if(FightLogger.exists(id)) {
					event.getPrivateChannel().sendFile(FightLogger.getFight(id)).queue();
				} else {
					event.getPrivateChannel().sendMessage("Unable to find fight id: " + message).queue();
				}
			} catch (NumberFormatException e) {
				event.getPrivateChannel().sendMessage("Unable to find fight id: " + message).queue();
			}
		}
	}
	
	private void processAdminCommand(String message, MessageReceivedEvent event) {
		if(message.charAt(0) == '!') {
			String command = message.split(" ")[0].toLowerCase().substring(1);
			switch(command) {
			case "reroll":
				Member memberToReRoll = guild.getMemberById(message.split(" ")[1]);
				if(memberToReRoll != null) {
					try {
					playerData.deleteById(memberToReRoll.getIdLong());
					} catch (Exception e) {
						
					}
					assignStats(memberToReRoll);
					event.getPrivateChannel().sendMessage("Player stats have been reset").queue();
				} else {
					event.getPrivateChannel().sendMessage("Player with that ID does not exist in this guild").queue();
				}
				break;
			case "audit":
				Member member1 = guild.getMemberById(message.split(" ")[1]);
				if(member1 != null) {
					auditPlayer(event, member1.getUser().getName());
				} else {
					event.getPrivateChannel().sendMessage("This player does not exist").queue();
				}
				break;
			case "new-day":
				dayPassed();
				event.getPrivateChannel().sendMessage("A new day has passed").queue();
				break;
			case "fight-summary":
				break;
			case "roll-mythic-item":
					rollMythicItem();
					event.getPrivateChannel().sendMessage("A Mythic item has been rolled").queue();
				break;
			case "roll-encounter":
				for(int i = 0; i < getNumber(message); i++) {
					rollEncounter();
				}
				event.getPrivateChannel().sendMessage("Encounter(s) rolled").queue();
				break;
			case "roll-activity":
				for(int i = 0; i < getNumber(message); i++) {
					rollActivity();
				}
				event.getPrivateChannel().sendMessage("Activity(s) rolled").queue();
				break;
			case "roll-item":
				for(int i = 0; i < getNumber(message); i++) {
					rollItem();
				}
				event.getPrivateChannel().sendMessage("Items(s) rolled").queue();
				break;
			case "set":
				if(message.split(" ").length == 4) {
					try {
						Long userID = Long.parseLong(message.split(" ")[1]);
						String stat = message.split(" ")[2];
						int amount = Integer.parseInt(message.split(" ")[3]);
						Member member = guild.getMemberById(userID);
						if(member != null) {
							Player player = playerData.findById(userID).get();
							switch(stat.toLowerCase()) {
							case "strenght":
								player.setStrength(amount);
								event.getPrivateChannel().sendMessage("Stat changed").queue();
								break;
							case "knowledge":
								player.setKnowledge(amount);
								event.getPrivateChannel().sendMessage("Stat changed").queue();
								break;
							case "stamina":
								player.setStamina(amount);
								event.getPrivateChannel().sendMessage("Stat changed").queue();
								break;
							case "agility":
								player.setAgility(amount);
								event.getPrivateChannel().sendMessage("Stat changed").queue();
								break;
							case "magic":
								player.setMagic(amount);
								event.getPrivateChannel().sendMessage("Stat changed").queue();
								break;
							case "gold":
								player.setGold(amount);
								event.getPrivateChannel().sendMessage("Stat changed").queue();
								break;
							default:
								event.getPrivateChannel().sendMessage("valid stats are strength, knowledge, stamina, agility, magic and gold").queue();
								break;
							}
							playerData.save(player);
						} else {
							event.getPrivateChannel().sendMessage("Player with that ID does not exist in this guild").queue();
						}
					} catch (Exception e) {
						event.getPrivateChannel().sendMessage("Incorrect arguments").queue();
					}
				} else {
					event.getPrivateChannel().sendMessage("Wrong number of arguments").queue();
				}
				break;
			case "help":
			default:
				event.getPrivateChannel().sendMessageEmbeds(EmbedMessageMaker.adminMessage().build()).queue();
				break;
			}
		}
	}

	private void auditPlayer(MessageReceivedEvent event, String playerName) {
		File outFile = new File("audit.txt");
		try {
			outFile.createNewFile();
			PrintWriter out = new PrintWriter(outFile);
			for(File f : EndOfDayLogger.getDir().listFiles()) {
				Scanner in = new Scanner(f);
				while(in.hasNextLine()) {
					String line = in.nextLine();
					if(line.contains(playerName)) {
						out.println(f.getName() + " " + line);
					}
				}
				in.close();
			}
			out.close();
			event.getPrivateChannel().sendFile(outFile).queue();
			outFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getNumber(String message) {
		if(message.split(" ").length > 1) {
			try {
				return Integer.parseInt(message.split(" ")[1]);
			} catch (NumberFormatException e) {
				
			}
		}
		return 1;
	}
	
	private void fightEncounter(Member player, Encounter ep, MessageReactionAddEvent event) {
		Player attacker = playerData.findById(player.getIdLong()).get();
		FightResults results;
		if(attacker.getItem() != null && attacker.getItem().getItemType() == ep.getBane()) {
			attacker.hasChallenged();
			results = new FightResults(true, 5, 0, 100, 1, 100, 1, 100, 1, 100, 1, 100, 1, 0, 0, 0, 100, 0, 100, 0, 100, 0, 100, 0, 100, 0);
		} else {
			results = fight(attacker, ep);
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Encounter fight results for " + player.getEffectiveName());
		
		if(results.isAttackerWon()) {
			// we win the encounter
			// add gold
			long goldWon = DiceRollingSimulator.rollDice(2, (int)(ep.getTotal()/8.0));
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
			
			DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has done an encounter\n"
					+ "\tResult: won\n"
					+ "\tGold: " + goldWon + "\n"
					+ "\t" + skillName + ": " + skillInc + "\n"
					+ "\tAttacker points: " + results.getAttackerPoints() + "\tDefender points: " + results.getDefenderPoints());
			
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
			DailyLogger.writeToFile(getNameWithCaste(event.getMember()) + " has done an encounter\n"
					+ "\tResult: lost\n"
					+ "\tGold lost: " + goldLost + "\n"
					+ "\tAttacker points: " + results.getAttackerPoints() + "\tDefender points: " + results.getDefenderPoints());
		}
		
		eb.addField("Fight statistics", "Attacker points: " + results.getAttackerPoints() + "\nDefender points: " + results.getDefenderPoints(),true);
		eb.setTimestamp(Instant.now());
		generalChannel.sendMessage("<@" + event.getUserId() + ">").queue();
		generalChannel.sendMessageEmbeds(eb.build()).queue();
		checkForAchievements(attacker);
		playerData.save(attacker);
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
		
		Clock c = Clock.systemUTC();
		c = Clock.offset(c, Duration.ofDays(activityDuration / 2));
		Activity activity = new Activity(0l, actionCost, change, goldCost, reward, OffsetDateTime.now(c));
		activitiesChannel.sendMessageEmbeds(EmbedMessageMaker.activity(activity, c).build()).queue(message -> {
			activity.setId(message.getIdLong());
			activityData.save(activity);
			message.addReaction(encountersChannel.getGuild().getEmoteById(fightEmojiID)).queue();
		});
		
	}
	
	private void rollItem() {
		Rarity rarity = Rarity.getRandomRarity();
		StatType stat;
		int change;
		if(rarity == Rarity.MYTHIC) {
			stat = StatType.getRandomMythic();
			change = 1;
		} else {
			stat = StatType.getRandomStatic();
			change = rarity.getMultiplier() * 5 + new Random().nextInt(5);
		}
		
		HashMap<String, String> names = StatType.randomName(stat);
		String itemName = new LinkedList<String>(names.keySet()).get(new Random().nextInt(names.size()));
		String itemDesc = names.get(itemName);
		
		int cost = rarity == Rarity.MYTHIC ? 0 : rarity.getMultiplier() * 20 + new Random().nextInt(20);
		Item item = new Item(rarity, stat, itemName, itemDesc, change);
		Clock c = Clock.systemUTC();
		c = Clock.offset(c, Duration.ofDays(shopDuration / 2));
		ShopItem sItem = new ShopItem(0l, item, cost, 0l, OffsetDateTime.now(c));
		itemsChannel.sendMessageEmbeds(EmbedMessageMaker.shopItem(sItem, c).build()).queue(message -> {
			sItem.setId(message.getIdLong());
			shopItemData.save(sItem);
			if(sItem.getItem().getRarity() == Rarity.MYTHIC) {
				message.addReaction(itemsChannel.getGuild().getEmoteById(fiveGoldID)).queue();
				message.addReaction(itemsChannel.getGuild().getEmoteById(tenGoldID)).queue();
				message.addReaction(itemsChannel.getGuild().getEmoteById(fiftyGoldID)).queue();
			} else {
				message.addReaction(itemsChannel.getGuild().getEmoteById(fightEmojiID)).queue();
			}
		});
	}
	
	private void rollMythicItem() {
		Rarity rarity = Rarity.MYTHIC;
		StatType stat;
		int change;
		if(rarity == Rarity.MYTHIC) {
			stat = StatType.getRandomMythic();
			change = 1;
		} else {
			stat = StatType.getRandomStatic();
			change = rarity.getMultiplier() * 5 + new Random().nextInt(5);
		}
		
		HashMap<String, String> names = StatType.randomName(stat);
		String itemName = new LinkedList<String>(names.keySet()).get(new Random().nextInt(names.size()));
		String itemDesc = names.get(itemName);
		
		int cost = rarity == Rarity.MYTHIC ? 0 : rarity.getMultiplier() * 20 + new Random().nextInt(20);
		Item item = new Item(rarity, stat, itemName, itemDesc, change);
		Clock c = Clock.systemUTC();
		c = Clock.offset(c, Duration.ofDays(shopDuration / 2));
		ShopItem sItem = new ShopItem(0l, item, cost, 0l, OffsetDateTime.now(c));
		itemsChannel.sendMessageEmbeds(EmbedMessageMaker.shopItem(sItem, c).build()).queue(message -> {
			sItem.setId(message.getIdLong());
			shopItemData.save(sItem);
			if(sItem.getItem().getRarity() == Rarity.MYTHIC) {
				message.addReaction(itemsChannel.getGuild().getEmoteById(fiveGoldID)).queue();
				message.addReaction(itemsChannel.getGuild().getEmoteById(tenGoldID)).queue();
				message.addReaction(itemsChannel.getGuild().getEmoteById(fiftyGoldID)).queue();
			} else {
				message.addReaction(itemsChannel.getGuild().getEmoteById(fightEmojiID)).queue();
			}
		});
	}

	private void rollEncounter() {
		EmbedBuilder eb = new EmbedBuilder();
		
		LinkedList<String> tiers = new LinkedList<>();
		LinkedList<String> types = new LinkedList<>();
		LinkedList<StatType> baneTypes = new LinkedList<>();
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
		
		baneTypes.add(StatType.BANE_BANDIT);
		baneTypes.add(StatType.BANE_BLOB);
		baneTypes.add(StatType.BANE_WIZARD);
		baneTypes.add(StatType.BANE_SKELETON);
		baneTypes.add(StatType.BANE_WOLF);
		baneTypes.add(StatType.BANE_GHOUL);
		baneTypes.add(StatType.BANE_GIANT);
		baneTypes.add(StatType.BANE_TROLL);
		
		int tier = (int)(Math.random() * tiers.size());
		int type = (int)(Math.random() * types.size());
		
		int strength = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int knowledge = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int magic = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int agility = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		int stamina = (int)(Math.random() * 10) + (tier * encounterStatMultiplier);
		
		Clock c = Clock.systemUTC();
		c = Clock.offset(c, Duration.ofDays(4 / 2));
		//Encounter baddy = new Encounter(0l, strength, agility, knowledge, magic, stamina, encounterID, tiers.get(tier) + " " + types.get(type), baneTypes.get(type), OffsetDateTime.now(c));
		Encounter baddy = new Encounter(0l, strength, agility, knowledge, magic, stamina, tiers.get(tier) + " " + types.get(type), baneTypes.get(type), OffsetDateTime.now(c));
		eb.addField("Strength", strength + "", true);
		eb.addField("Knowledge", knowledge + "", true);
		eb.addField("Magic", magic + "", true);
		eb.addField("Agility", agility + "", true);
		eb.addField("Stamina", stamina + "", true);

		eb.setImage("https://zgamelogic.com/downloads/" + tier + "-" + types.get(type) + ".png");
		
		eb.setTitle("A " + tiers.get(tier) + " " + types.get(type) + " challenges the kingdom!");
		eb.setColor(new Color(56, 79, 115));
		eb.setFooter("Departs ");
		eb.setTimestamp(baddy.getTimeDepart());

		encountersChannel.sendMessageEmbeds(eb.build()).queue(message -> {
			baddy.setId(message.getIdLong());
			encounterData.save(baddy);
			message.addReaction(encountersChannel.getGuild().getEmoteById(fightEmojiID)).queue();
		});
		
		logger.info("Adding encounter");
	}
	
	private void randomRolls() {
		while(true) {
			OffsetDateTime now = OffsetDateTime.now();
			// Delete old arrivals
			for(Activity activity : activityData.findAll()) {
				OffsetDateTime departTime = activity.getTimeDepart();
				if(now.isAfter(departTime)) {
					activitiesChannel.retrieveMessageById(activity.getId()).queue(message -> {
						// its time to depart
						message.delete().queue();
						activityData.delete(activity);
					});
				}
			}
			
			// Delete old items
			for(ShopItem item : shopItemData.findAll()) {
				OffsetDateTime departTime = item.getTimeDepart();
				if(now.isAfter(departTime)) {
					itemsChannel.retrieveMessageById(item.getId()).queue(message -> {
						if(item.getItem().getRarity() == Rarity.MYTHIC) {
							if(item.getCurrentBidder() != 0) {
								Player player = playerData.findById(item.getCurrentBidder()).get();
								player.setItem(item.getItem());
								playerData.save(player);
								generalChannel.sendMessage("<@" + player.getId() + ">, Congratulations on your new " + item.getItem().getItemName()).mention(guild.getMemberById(player.getId())).queue();
								DailyLogger.writeToFile(getNameWithCaste(guild.getMemberById(player.getId())) + " has won " + item.getItem().getItemName() + " in a bid");
							}
						}
						message.delete().queue();
						shopItemData.delete(item);
					});
				}
			}
			
			// Delete old encounters
			for(Encounter encounter : encounterData.findAll()) {
				OffsetDateTime departTime = encounter.getTimeDepart();
				if(now.isAfter(departTime)) {
					encountersChannel.retrieveMessageById(encounter.getId()).queue(message -> {
						// its time to depart
						message.delete().queue();
						encounterData.delete(encounter);
					});
				}
			}
			
			if(((int)(Math.random() * activitySpawnChance * 4)) == 1) {
				rollActivity();
			}
			
			if(((int)(Math.random() * itemSpawnChance * 6)) == 1) {
				rollItem();
			}
			
			if(((int)(Math.random() * spawnChance * 15)) == 1) {
				rollEncounter();
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
		GameInformation game = gameData.findById("game_data").get();
		if(game.getTaxAmount() > 0) {
			Member kingMember = guild.getMembersWithRoles(guild.getRoleById(kingRoleID)).get(0);
			Player king = playerData.findById(kingMember.getIdLong()).get();
			for(Member m : guild.getMembersWithRoles(guild.getRoleById(game.getTaxRoleID()))) {
				if(!m.getUser().isBot()) {
					Player p = playerData.findById(m.getIdLong()).get();
					int payAmount = game.getTaxAmount();
					if(p.getGold() < payAmount) {
						payAmount = p.getIntGold();
					}
					p.decreaseGold(payAmount);
					king.increaseGold(payAmount);
					DailyLogger.writeToFile(getNameWithCaste(m) + " has paid " + getNameWithCaste(kingMember) + " " + payAmount + " gold in tax");
					playerData.save(p);
				}
			}
			playerData.save(king);
			game.resetTax();
			gameData.save(game);
		}
	}
	
	private void dailyGoldIncrease() {
		for(Player p : playerData.findAll()) {
			if(p.isActive()) {
				p.increaseGold(DiceRollingSimulator.rollDice(1, 4));
			}
			p.newDay();
			if(isKing(p.getId() + "")) {
				p.increaseGold((int)(Math.random() * 2) + 10);
			}
			playerData.save(p);
		}
	}
	
	private void saveStats() {
		for(Player player : playerData.findAll()) {
			EndOfDayLogger.writeToFile(getNameWithCaste(guild.getMemberById(player.getId())) + " " + player.getCompactStats());
		}
	}
	
	private void dayPassed() {
		DailyLogger.writeToFile("A new day has come!");
		saveStats();
		dailyGoldIncrease();
		payTax();
		
		GameInformation game = gameData.findById("game_data").get();
		if(guild.getMembersWithRoles(guild.getRoleById(kingRoleID)).get(0).getUser().getIdLong() != game.getKingID()) {
			game.setKingID(guild.getMembersWithRoles(guild.getRoleById(kingRoleID)).get(0).getUser().getIdLong());
			game.setKingRun(0);
		}
		game.resetList();
		game.setKingRun(game.getKingRun() + 1);
		gameData.save(game);		
		generalChannel.sendMessageEmbeds(EmbedMessageMaker.goodMorningMessage().build()).queue();
	}
	
	private boolean isKing(String playerID) {
		Member player = guild.getMemberById(playerID);
		if(player != null)
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
			if(date.get(Calendar.HOUR) == 11 && date.get(Calendar.MINUTE) == 0) {
				notifyThePeople();
			}
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

	private void notifyThePeople() {
		GameInformation game = gameData.findById("game_data").get();
		for(Long id : game.getDailyRemindIDs()) {
			Player p = playerData.findById(id).get();
			if(p.canChallenge()) {
				guild.getMemberById(id).getUser().openPrivateChannel().queue(channel -> {
					channel.sendMessageEmbeds(EmbedMessageMaker.remindMessage().build()).queue();
				});
			}
		}
	}
}