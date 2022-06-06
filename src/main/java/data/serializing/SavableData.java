package data.serializing;

import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.results.ActivityResults;
import bot.role.data.results.ChallengeFightResults;
import bot.role.data.results.EncounterFightResults;
import bot.role.data.structures.General;
import bot.role.data.structures.KingData;
import bot.role.data.structures.Player;
import bot.role.dungeon.saveable.Dungeon;
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
		@JsonSubTypes.Type(General.class)
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