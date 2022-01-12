package controllers.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TeamBuilder {
	
	private int maxTeamSize;
	private boolean overflow;
	private String name;
	
	public TeamBuilder() {
		this("");
	}
	
	public TeamBuilder(String name) {
		maxTeamSize = -1;
		overflow = false;
		this.name = name;
	}

}
