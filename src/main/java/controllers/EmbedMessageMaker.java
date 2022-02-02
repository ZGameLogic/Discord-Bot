package controllers;

import java.awt.Color;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import bot.role.Player;
import bot.role.RoleBotListener;
import bot.role.data.Activity;
import bot.role.data.Activity.ActivityReward;
import bot.role.data.Item.Rarity;
import bot.role.data.Item.StatType;
import bot.role.data.ShopItem;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public abstract class EmbedMessageMaker {
	
	public static EmbedBuilder shopItem(ShopItem item, Clock retireTime) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(68, 145, 143));
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
	
	public static EmbedBuilder activityLeaderboard(HashMap<String, Player> playerMap, Guild guild) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Leaderboard for activities");
		eb.setColor(new Color(102, 107, 14));
		LinkedList<Player> players = new LinkedList<>(playerMap.values());
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
		eb.setColor(new Color(64, 141, 148));
		eb.setTitle("A message to all those people who like to forget");
		eb.setDescription("Do not forget to do your activities for the day!");
		eb.setFooter("From your lord and savior, Shlongbot");
		return eb;
	}
	
	public static EmbedBuilder activityResults(String user, String stat, int amount) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(176, 103, 44));
		eb.setTitle("Activity results for " + user);
		eb.addField(stat, amount + "", true);
		return eb;
	}
	
	public static EmbedBuilder activity(Activity activity, Clock retireTime) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(176, 103, 44));
		
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
	
	public static EmbedBuilder goodMorningMessage(String message) {
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
		eb.setColor(new Color(102, 107, 14));
		for(String key : rolePop.keySet()) {
			eb.addField(key, rolePop.get(key) + "", true);
		}
		return eb;
	}
	
	public static EmbedBuilder honorablePromotion(Member member1, Member member2) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen has decreed a role change!");
		eb.setDescription("By the command of our king/queen: " + member1.getEffectiveName() + " and " + member2.getEffectiveName() + " have had their roles swapped!");
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder newKing(String kingName, String memberIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("All hail the new King/Queen " + kingName + "!");
		eb.setImage(memberIcon);
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder proposeTax(long amount, String roleName, String roleIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen has proposed a tax on the caste of: " + roleName);
		eb.setDescription("At the begining of the next day, everyone in that caste will pay the king/queen " + amount + " gold");
		if(!roleIcon.equals("")) {
			eb.setThumbnail(roleIcon);
		}
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder distributeGold(long initialAmount, String roleName, String roleIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen bequeathes the caste of " + roleName + " with " + initialAmount + " gold!");
		if(!roleIcon.equals("")) {
			eb.setThumbnail(roleIcon);
		}
		eb.setDescription("Go now! Check your banks! All hail the King/Queen!!!");
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder roleStats(SlashCommandEvent event, DataCacher<Player> data) {
		Role role = event.getOption("role").getAsRole();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Fighter stats for " + role.getName() + "s");
		eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina Gold Wins/Losses");
		eb.setColor(new Color(113, 94, 115));
		if(role.getIcon() != null) {
			eb.setThumbnail(role.getIcon().getIconUrl());
		}
		
		boolean includeAll = false;
		
		if(event.getOption("include-all") != null) {
			includeAll = event.getOption("include-all").getAsBoolean();
		}
		
		for(Member m : event.getGuild().getMembersWithRoles(role)) {
			if(!m.getUser().isBot()) {
				Player p = data.loadSerialized(m.getId());
				if(p.canDefend() || includeAll) {
					eb.addField(m.getEffectiveName(), p.getCompactStats(), true);
				}
			}
		}
		return eb;
	}
	
	public static EmbedBuilder playerStats(Member member, DataCacher<Player> data, String icon) {
		EmbedBuilder eb = new EmbedBuilder();
		Player player = data.loadSerialized(member.getIdLong() + "");
		eb.setTitle("Fighter stats for " + member.getEffectiveName() + " " + icon);
		eb.setColor(new Color(113, 94, 115));
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
			eb.addField("Item: " + player.getItem().getItemName(), player.getItem().getItemDescription(), false);
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
