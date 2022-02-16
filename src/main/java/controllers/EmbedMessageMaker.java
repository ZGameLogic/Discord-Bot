package controllers;

import java.awt.Color;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import bot.role.RoleBotListener;
import data.database.arena.achievements.Achievements;
import data.database.arena.activity.Activity;
import data.database.arena.activity.Activity.ActivityReward;
import data.database.arena.item.Item.Rarity;
import data.database.arena.item.Item.StatType;
import data.database.arena.player.Player;
import data.database.arena.player.PlayerRepository;
import data.database.arena.shopItem.ShopItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public abstract class EmbedMessageMaker {
	
	private static Color ITEM_COLOR_COMMON = new Color(68, 145, 143);
	private static Color ITEM_COLOR_UNCOMMON = new Color(40, 184, 180);
	private static Color ITEM_COLOR_RARE = new Color(0, 250, 243);
	private static Color ITEM_COLOR_EPIC = new Color(250, 237, 0);
	private static Color ITEM_COLOR_LEGENDARY = new Color(250, 171, 0);
	private static Color ITEM_COLOR_MYTHIC = new Color(250, 29, 0);
	
	private static Color LEADERBOARD_COLOR = new Color(102, 107, 14);
	private static Color REMIND_COLOR = new Color(64, 141, 148);
	private static Color ACTIVITY_COLOR = new Color(176, 103, 44);
	private static Color KING_COLOR = new Color(252, 211, 3);
	private static Color STATS_COLOR = new Color(113, 94, 115);
	private static Color ACHIEV_COLOR = new Color(224, 63, 95);
	private static Color ADMIN_COLOR = new Color(123, 50, 168);
	
	public static EmbedBuilder newDayMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		
		return eb;
	}
	
	public static EmbedBuilder adminMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(ADMIN_COLOR);
		eb.setTitle("Admin commands");
		eb.addField("audit <player id>", "Returns the stats for each player on each day at the end of the day", true);
		eb.addField("fight-summary <fight id>", "Returns the fight summary for that specific id", true);
		eb.addField("reroll <user id>", "Re-rolls the stats for user id", true);
		eb.addField("new-day", "Resets everyones activities", true);
		eb.addField("roll-encounter [count]", "Rolls count encounters", true);
		eb.addField("roll-activity [count]", "Rolls count activities", true);
		eb.addField("roll-item [count]", "Rolls count items", true);
		eb.addField("set <user id> <stat/gold> <amount>", "Sets user id's stat/gold to amount", true);
		return eb;
	}
	
	public static EmbedBuilder playerAchievement(String playerName, Achievements a) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(ACHIEV_COLOR);
		eb.setTitle("Achievements earned for " + playerName);
		for(String title : a.getEarnedAchievements()) {
			eb.appendDescription(title + "\n");
		}
		return eb;
	}
	
	public static EmbedBuilder achievement(String playerName, String name, String desc) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(ACHIEV_COLOR);
		eb.setTitle(playerName + " has unlocked the achievement: " + name);
		eb.setDescription(desc);
		return eb;
	}	
	
	public static EmbedBuilder shopItem(ShopItem item, Clock retireTime) {
		EmbedBuilder eb = new EmbedBuilder();
		switch(item.getItem().getRarity()) {
		case COMMON:
			eb.setColor(ITEM_COLOR_COMMON);
			break;
		case EPIC:
			eb.setColor(ITEM_COLOR_EPIC);
			break;
		case LEGENDARY:
			eb.setColor(ITEM_COLOR_LEGENDARY);
			break;
		case MYTHIC:
			eb.setColor(ITEM_COLOR_MYTHIC);
			break;
		case RARE:
			eb.setColor(ITEM_COLOR_RARE);
			break;
		case UNCOMMON:
			eb.setColor(ITEM_COLOR_UNCOMMON);
			break;
		
		}
		eb.setTitle(item.getItem().getItemName() + " is in stock today!");
		eb.setDescription(item.getItem().getItemDescription());
		eb.setAuthor(item.getItem().getRarity().rarityName());
		if(item.getItem().getRarity() == Rarity.MYTHIC) {
			if(item.getItem().isActive()) {
				if(item.getItem().getItemType() == StatType.ACTIVE_GOLD) {
					eb.addField("Effect", item.getItem().getItemType().getStatDescription(), true);
				} else {
					eb.addField("Effect", item.getItem().getItemType().getStatDescription() + item.getItem().getStatIncrease(), true);
				}
			} else if (item.getItem().isBane()) {
				eb.addField("Effect", item.getItem().getItemType().getStatDescription(), true);
			}
			eb.addField("Current bid", "0", true);
			eb.addField("Current bidder", "No one has bid on this item yet", true);
		} else {
			eb.addField("Effect", item.getItem().getItemType().getStatDescription() + item.getItem().getStatIncrease(), true);
			eb.addField("Cost", item.getCost() + "", true);
		}
		
		eb.setTimestamp(Instant.now(retireTime));
		eb.setFooter("Expires");
		
		return eb;
	}
	
	public static EmbedBuilder activityLeaderboard(List<Player> list, Guild guild) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Leaderboard for activities");
		eb.setColor(LEADERBOARD_COLOR);
		LinkedList<Player> players = new LinkedList<>(list);
		Comparator<Player> comparitor = Comparator.comparing(Player::getActivitiesLeft);
		comparitor = comparitor.reversed();
		Collections.sort(players, comparitor);
		for(Player p : players) {
			if(p.isActive()) {
				eb.appendDescription(guild.getMemberById(p.getId()).getEffectiveName() + " (" + p.getActivitiesLeft() + ")\n");
			}
		}
		
		return eb;
	}
	
	public static EmbedBuilder remindMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(REMIND_COLOR);
		eb.setTitle("A message to all those people who like to forget");
		eb.setDescription("Do not forget to do your activities for the day!");
		eb.setFooter("From your lord and savior, Shlongbot");
		return eb;
	}
	
	public static EmbedBuilder activityResults(String user, String stat, int amount) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(ACTIVITY_COLOR);
		eb.setTitle("Activity results for " + user);
		eb.addField(stat, amount + "", true);
		return eb;
	}
	
	public static EmbedBuilder activity(Activity activity, Clock retireTime) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(ACTIVITY_COLOR);
		
		LinkedList<String> possibleVendorNames = new LinkedList<>();
		switch(activity.getReward()) {
		case Agility:
			possibleVendorNames.add("Tinúviel");
			possibleVendorNames.add("Knife Master Uragu");
			possibleVendorNames.add("A Nimble Thief");
			possibleVendorNames.add("Robin the hooded");
			possibleVendorNames.add("Vestman Victor");
			possibleVendorNames.add("A Highwayman Voronin");
			break;
		case Knowledge:
			possibleVendorNames.add("A Renown Scholar");
			possibleVendorNames.add("A Penniless Educator");
			possibleVendorNames.add("A Curious Librarian");
			possibleVendorNames.add("Bee farmer Herrold");
			possibleVendorNames.add("Professor Rendtyk");
			break;
		case Magic:
			possibleVendorNames.add("A Mystical Conjuror");
			possibleVendorNames.add("A Luthwin Caster");
			possibleVendorNames.add("Zack the Mage");
			possibleVendorNames.add("Azathoth the cursed");
			possibleVendorNames.add("A Hooded Figure");
			possibleVendorNames.add("Ymir the fallen");
			break;
		case Stamina:
			possibleVendorNames.add("Boic the Brave");
			possibleVendorNames.add("A Seasoned Warrior");
			possibleVendorNames.add("A dark Assassin");
			possibleVendorNames.add("Charlie Lehorse");
			possibleVendorNames.add("Sword Master Sketh");
			break;
		case Strength:
			possibleVendorNames.add("A Goliath");
			possibleVendorNames.add("Sir Kendrith the 4th");
			possibleVendorNames.add("Garrison Commander Lukasz");
			possibleVendorNames.add("Strongarm Zuq");
			possibleVendorNames.add("Nagrog the beast");
			break;
		case Gold:
			possibleVendorNames.add("Clean the stables");
			possibleVendorNames.add("Scrub the church stones");
			possibleVendorNames.add("Stone the criminals");
			possibleVendorNames.add("Sweep the castle");
			possibleVendorNames.add("Dump the chamberpots");
			possibleVendorNames.add("Clean the bloody swords");
			possibleVendorNames.add("Educate the children");
			possibleVendorNames.add("Hold the Shlongbot trials");
			possibleVendorNames.add("Break up the worker strikes");
			possibleVendorNames.add("Harvest the beehives");
			possibleVendorNames.add("Breastfeed the babies");
			possibleVendorNames.add("Sweeten the honey");
			possibleVendorNames.add("Pray at the shrine for shlongbot");
			possibleVendorNames.add("Revive the peasants");
			possibleVendorNames.add("Fight Kat's principal Jeff");
			break;
		}
		
		if(activity.getReward() != ActivityReward.Gold) {
			// if its training
			eb.setTitle(possibleVendorNames.get(new Random().nextInt(possibleVendorNames.size())) + " has posted on the city board: " + activity.getReward().name() + " training!");
			eb.addField("Activity cost", activity.getActionCost() + "", true);
			eb.addField("Gold cost", activity.getGoldCost() + "", true);
			eb.addField("Reward", activity.getRewardAmount() + " " + activity.getReward().name(), true);
		} else {
			// if its working
			eb.setTitle("A job has been posted on the city board: " + possibleVendorNames.get(new Random().nextInt(possibleVendorNames.size())));
			eb.addField("Activity cost", activity.getActionCost() + "", true);
			eb.addField("Reward", activity.getRewardAmount() + " Gold", true);
		}
		
		eb.setTimestamp(Instant.now(retireTime));
		eb.setFooter("Departs: ");
		return eb;
	}
	
	public static EmbedBuilder goodMorningMessage() {
		
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
		starts.add("A rooster bores its noise into the ears of all who were in the village, abruptly seizing as a morningï¿½s hunger sets in.");

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
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(119, 21, 161));
		eb.setTitle("A message from the king/queen");
		eb.setDescription(message);
		return eb;
	}
	
	public static EmbedBuilder giveGold(String giver, String taker, long amount) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(252, 211, 3));
		eb.setTitle(giver + " has given some gold to " + taker);
		eb.setDescription("Amount: " + amount);
		return eb;
	}
	
	public static EmbedBuilder leaderboardFaction(HashMap<String, Integer> rolePop) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Leaderboard for caste population ");
		eb.setColor(LEADERBOARD_COLOR);
		for(String key : rolePop.keySet()) {
			eb.addField(key, rolePop.get(key) + "", true);
		}
		return eb;
	}
	
	public static EmbedBuilder honorablePromotion(Member member1, Member member2) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen has decreed a role change!");
		eb.setDescription("By the command of our king/queen: " + member1.getEffectiveName() + " and " + member2.getEffectiveName() + " have had their roles swapped!");
		eb.setColor(KING_COLOR);
		return eb;
	}
	
	public static EmbedBuilder newKing(String kingName, String memberIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("All hail the new King/Queen " + kingName + "!");
		eb.setImage(memberIcon);
		eb.setColor(KING_COLOR);
		return eb;
	}
	
	public static EmbedBuilder proposeTax(long amount, String roleName, String roleIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen has proposed a tax on the caste of: " + roleName);
		eb.setDescription("At the begining of the next day, everyone in that caste will pay the king/queen " + amount + " gold");
		if(!roleIcon.equals("")) {
			eb.setThumbnail(roleIcon);
		}
		eb.setColor(KING_COLOR);
		return eb;
	}
	
	public static EmbedBuilder distributeGold(long initialAmount, String roleName, String roleIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen bequeathes the caste of " + roleName + " with " + initialAmount + " gold!");
		if(!roleIcon.equals("")) {
			eb.setThumbnail(roleIcon);
		}
		eb.setDescription("Go now! Check your banks! All hail the King/Queen!!!");
		eb.setColor(KING_COLOR);
		return eb;
	}
	
	public static EmbedBuilder roleStats(SlashCommandEvent event, PlayerRepository playerData) {
		Role role = event.getOption("role").getAsRole();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Fighter stats for " + role.getName() + "s");
		eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina Gold Wins/Losses");
		eb.setColor(STATS_COLOR);
		if(role.getIcon() != null) {
			eb.setThumbnail(role.getIcon().getIconUrl());
		}
		
		boolean includeAll = false;
		
		if(event.getOption("include-all") != null) {
			includeAll = event.getOption("include-all").getAsBoolean();
		}
		
		for(Member m : event.getGuild().getMembersWithRoles(role)) {
			if(!m.getUser().isBot()) {
				data.database.arena.player.Player p = playerData.findById(m.getIdLong()).get();
				if(p.canDefend() || includeAll) {
					eb.addField(m.getEffectiveName(), p.getCompactStats(), true);
				}
			}
		}
		return eb;
	}
	
	public static EmbedBuilder playerStats(Member member, data.database.arena.player.Player player, String icon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Fighter stats for " + member.getEffectiveName() + " " + icon);
		eb.setColor(STATS_COLOR);
		eb.setThumbnail(member.getEffectiveAvatarUrl());
		if(player.getItem() != null && player.getItem().getItemType() == StatType.STATIC_STRENGTH) {
			eb.addField("Strength", player.getRawStrength() + " (+" + player.getItem().getStatIncrease() + ")", true);
		} else {
			eb.addField("Strength", player.getRawStrength() + "", true);
		}
		if (player.getItem() != null && player.getItem().getItemType() == StatType.STATIC_KNOWLEDGE) {
			eb.addField("Knowledge", player.getRawKnowledge() + " (+" + player.getItem().getStatIncrease() + ")", true);
		} else {
			eb.addField("Knowledge", player.getRawKnowledge() + "", true);
		}
		if (player.getItem() != null && player.getItem().getItemType() == StatType.STATIC_MAGIC) {
			eb.addField("Magic", player.getRawMagic() + " (+" + player.getItem().getStatIncrease() + ")", true);
		} else {
			eb.addField("Magic", player.getRawMagic() + "", true);
		}
		if (player.getItem() != null && player.getItem().getItemType() == StatType.STATIC_AGILITY) {
			eb.addField("Agility", player.getRawAgility() + " (+" + player.getItem().getStatIncrease() + ")", true);
		} else {
			eb.addField("Agility", player.getRawAgility() + "", true);
		}
		if (player.getItem() != null && player.getItem().getItemType() == StatType.STATIC_STAMINA) {
			eb.addField("Stamina", player.getRawStamina() + " (+" + player.getItem().getStatIncrease() + ")", true);
		} else {
			eb.addField("Stamina", player.getRawStamina() + "", true);
		}
		
		if(player.getItem() != null) {
			String description = player.getItem().getItemDescription();
			if(player.getItem().isActive()) {
				description += "\n" + player.getItem().getItemType().getStatDescription() + player.getItem().getStatIncrease();
			}
			eb.addField("Item: " + player.getItem().getItemName(), description, false);
		}
		
		eb.addField("Statistics", "Gold: " + player.getGold() + "\nTournament victories: " + player.getTournamentWins() + "\nVictories: " + player.getWins()
			+ "\nDefeats: " + player.getLosses(), false);
		int activityCount;
		if(player.getItem() != null && player.getItem().getItemType() == StatType.STATIC_MAX_ACTIVITIES) {
			activityCount = player.getItem().getStatIncrease() + RoleBotListener.dailyChallengeLimit - player.getHasChallengedToday();
		} else {
			activityCount =  RoleBotListener.dailyChallengeLimit - player.getHasChallengedToday();
		}
		String activity = activityCount != 1 ? "activities" : "activity";
		String time = RoleBotListener.dailyDefendLimit - player.getChallengedToday() != 1 ? "times" : "time";
		eb.setFooter("Can do " + activityCount + " more " + activity + " today\n"
				+ "Can defend " + (RoleBotListener.dailyDefendLimit - player.getChallengedToday()) + " more " + time + " today");
		return eb;
	}
}
