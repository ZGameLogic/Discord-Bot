package data.serializing;

import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.jsonConfig.Strings;
import bot.role.data.results.ActivityResults;
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
		@JsonSubTypes.Type(ActivityResults.class)
	}
)
public abstract class SaveableData {

	private String id;

	public SaveableData() {}

	@JsonCreator
	public SaveableData(@JsonProperty("id") String id) {
		this.id = id;
	}

	public SaveableData(long id) {
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