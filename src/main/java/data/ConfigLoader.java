package data;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

@Configuration
@PropertySource("File:discordbot.properties")
@Getter
public class ConfigLoader {

	@Value("${bot.token}")
	private String botToken;
	
	@Value("${keystore.password}")
	private String keystorePassword;
	
	@Value("${keystore.location}")
	private String keystoreLocation;
	
	@Value("${Webhook.port}")
	private int webHookPort;
	
	@Value("${chatroom.cat.id}")
	private long chatroomCatID;
	
	@Value("${create.chat.id}")
	private long createChatID;
	
	@Value("${guild.id}")
	private long guildID;
	
	@Value("${afk.id:0}")
	private long AFKID;
	
	@Value("${bitbucket.id}")
	private long bitbucketID;
	
	@Value("${admin.password}")
	private String adminPassword;
	
	@Value("${role.ids}")
	private String[] roleIDs;
	
	@Value("${admin.role.ids}")
	private String[] adminRoleIDs;
	
	@Value("${king.role.id}")
	private long kingRoleID;
	
	@Value("${server.booster.padding:0}")
	private int boosterChange;
	
	@Value("${padding.multiplier:8}")
	private int paddingMultiplier;
	
	@Value("${daily.challenge.limit:3}")
	private int dailyChallengeLimit;
	
	@Value("${daily.defend.limit:3}")
	private int dailyDefendLimit;
	
	@Value("${stat.base.change:2}")
	private int statBaseChange;
	
	@Value("${stat.random.change:2}")
	private int statRandomChange;
	
	@Value("${spawn.chance}")
	private int spawnChance;
	
	@Value("${encounter.stat.multiplier:7}")
	private int encounterStatMultiplier;
	
	@Value("${encounters.id}")
	private long encountersID;
	
	@Value("${general.id}")
	private long generalID;
	
	@Value("${activities.id}")
	private long activitiesID;
	
	@Value("${fight.id}")
	private long fightEmojiID;
	
	@Value("${activity.spawn.chance}")
	private int activitySpawnChance;
	
	@Value("${days.to.store.items}")
	private int daysToStoreItems;
	
	@Value("${icons.ids}")
	private String[] iconIDS;
	
	
	public LinkedList<Long> getRoleIDs(){
		LinkedList<Long> ids = new LinkedList<>();
		for(String id : roleIDs ) {
			ids.add(Long.parseLong(id));
		}
		return ids;
	}
	
	public LinkedList<Long> getAdminRoleIDs(){
		LinkedList<Long> ids = new LinkedList<>();
		for(String id : adminRoleIDs) {
			ids.add(Long.parseLong(id));
		}
		return ids;
	}
}
