package controllers.team;

import java.util.LinkedList;

import lombok.Getter;

public class Team implements Comparable<Team>{
	
	@Getter
	private TeamBuilder tb;
	private LinkedList<Group> groups;
	
	public Team(TeamBuilder tb) {
		groups = new LinkedList<>();
		this.tb = tb;
	}
	
	public String toPlayerString() {
		String players = "";
		
		for(Group g : groups) {
			for(String p : g.getPlayers()) {
				players += p + " ";
			}
		}
		
		return players.trim();
	}
	
	public void addGroup(Group group) {
		groups.add(group);
	}
	
	public boolean isCompatible(Group group) {
		for(Group g : groups) {
			if(!group.isCompatible(g)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean canFit(Group group) {
		if(tb.getMaxTeamSize() == -1) {
			return true;
		} else {
			return getSize() + group.getSize() <= tb.getMaxTeamSize();
		}
	}
	
	public int getSize() {
		int total = 0;
			for(Group g : groups) {
				total += g.getSize();
			}
		return total;
	}

	@Override
	public int compareTo(Team o) {
		if(getSize() < o.getSize()) {
			return -1;
		} else if(getSize() > o.getSize()) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		String teamString = "Team " + tb.getName() + ":";
		for(Group g : groups) {
			for(String p : g.getPlayers()) {
				teamString += " " + p;
			}
		}
		return teamString;
	}
}
