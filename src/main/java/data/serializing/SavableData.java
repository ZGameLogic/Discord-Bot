package data.serializing;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({})
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