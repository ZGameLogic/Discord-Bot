package controllers.team;

import java.util.LinkedList;

import lombok.Getter;

public class Group implements Comparable<Group> {
	
	@Getter
	private LinkedList<String> players;
	
	@Getter
	private LinkedList<Group> avoid;
	
	public Group() {
		players = new LinkedList<>();
		avoid = new LinkedList<>();
	}
	
	/**
	 * Checks if one group is compatible with another group
	 * @param group To be checked with
	 * @return true if groups are compatible
	 */
	public boolean isCompatible(Group group) {
		return !(group.getAvoid().contains(this) || avoid.contains(group));
	}
	
	public void addPlayer(String player) {
		players.add(player);
	}
	
	public void removePlayer(String player) {
		players.remove(player);
	}
	
	public void addAvoid(Group group) {
		avoid.add(group);
	}
	
	public void removeAvoid(Group group) {
		avoid.remove(group);
	}
	
	public boolean equals(Group group) {
		return players.equals(group.getPlayers());
	}
	
	public int getSize() {
		return players.size();
	}

	@Override
	public int compareTo(Group group) {
		if(getSize() > group.getSize()) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public String toString() {
		String players = "";
		
		for(String player : this.players) {
			players += " " + player;
		}
		
		return players;
	}

}
