package data.serializing;

import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.results.ActivityResults;
import bot.role.data.results.ChallengeFightResults;
import bot.role.data.results.MiscResults;
import bot.role.data.structures.*;
import bot.role.data.dungeon.saveable.Dungeon;
import bot.role.data.structures.item.ShopItem;
import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
		@JsonSubTypes.Type(Strings.class),
		@JsonSubTypes.Type(Player.class),
		@JsonSubTypes.Type(GameConfigValues.class),
		@JsonSubTypes.Type(KingData.class),
		@JsonSubTypes.Type(Dungeon.class),
		@JsonSubTypes.Type(ActivityResults.class),
		@JsonSubTypes.Type(ChallengeFightResults.class),
		@JsonSubTypes.Type(General.class),
		@JsonSubTypes.Type(Guild.class),
		@JsonSubTypes.Type(Encounter.class),
		@JsonSubTypes.Type(ShopItem.class),
		@JsonSubTypes.Type(Activity.class),
		@JsonSubTypes.Type(Tournament.class),
		@JsonSubTypes.Type(MiscResults.class)
	}
)
public abstract class SavableData {

	private String id;

	public SavableData() {}

	@JsonCreator
	public SavableData(@JsonProperty("id") String id) {
		this.id = id;
	}

	public SavableData(long id) {
		this(id + "");
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIdLong(long id) {
		this.id = id + "";
	}


	@JsonIgnore
	public long getIdLong() {
		return Long.parseLong(id);
	}
}