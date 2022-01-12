package controllers.team;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public abstract class TeamGenerator {
	
	@SuppressWarnings("unchecked")
	public static LinkedList<Team> generateTeams(String command) throws GroupCreationException, TeamNameException {
		HashMap<String, Object> map = parseString(command);
		return generateTeams((LinkedList<TeamBuilder>)map.get("teams"), (LinkedList<Group>)map.get("groups"), null);
	}
	
	private static HashMap<String, Object> parseString(String command) {
		LinkedList<TeamBuilder> teams = new LinkedList<>();
		LinkedList<Group> groups = new LinkedList<>();
		
		// Check for teams
		String stringTeam = extractString(command, "[", "]");
		if(stringTeam != null) {
			command = command.replace("[" + stringTeam + "]", "");
			for(String current : stringTeam.split(",")) {
				TeamBuilder tb = new TeamBuilder();
				for(String parm : current.trim().split(" ")) {
					if(parm.contains("-o")) {
						tb.setOverflow(true);
					}else if(parm.contains("-m")) {
						try {
							tb.setMaxTeamSize(Integer.parseInt(parm.replace("-m", "")));
						}catch (NumberFormatException e) {
						}
					}else {
						tb.setName(parm);
					}
				}
				teams.add(tb);
			}
		} else {
			teams.add(new TeamBuilder("One"));
			teams.add(new TeamBuilder("Two"));
		}
		
		// Check for groups
		LinkedList<int[]> groupTies = new LinkedList<int[]>();
		String avoidString = extractString(command, "<", ">");
		if(avoidString != null) {
			command = command.replace("<" + avoidString + ">", "");
			for(String line : avoidString.split(",")) {
				int[] index = new int[2];
				int spot = 0;
				for(String num : line.trim().split(" ")) {
					try {
						index[spot++] = Integer.parseInt(num);
					}catch (NumberFormatException e) {
						
					}
				}
				groupTies.add(index);
			}
		}
		
		boolean groupsLeft = true;
		
		while(groupsLeft) {
			String groupString = extractString(command, "{", "}");
			if(groupString != null) {
				command = command.replace("{" + groupString + "}", "").trim();
				Group g = new Group();
				for(String name : groupString.split(" ")) {
					g.addPlayer(name.trim());
				}
				groups.add(g);
			} else {
				groupsLeft = false;
			}
		}
		
		for(String name : command.split(" ")) {
			Group g = new Group();
			g.addPlayer(name.trim());
			groups.add(g);
		}
		
		
		for(int[] index : groupTies) {
			groups.get(index[0]).addAvoid(groups.get(index[1]));
		}
		HashMap<String, Object> map = new HashMap<>();
		map.put("teams", teams);
		map.put("groups", groups);
		
		return map;
	}
	
	public static String extractString(String all, String start, String end) {
		try {
			if(all.contains(start) && all.contains(end)) {
				String chunk = all.substring(all.indexOf(start) + 1, all.indexOf(end));
				return chunk;
			}
		} catch (StringIndexOutOfBoundsException e) {
			
		}
		return null;
	}
	
	public static LinkedList<Team> generateTeams(LinkedList<TeamBuilder> teamBuilders, LinkedList<Group> groups, LinkedList<String> players) throws GroupCreationException, TeamNameException {
		LinkedList<Team> teams = new LinkedList<>();
		
		if(groups == null) {
			groups = new LinkedList<>();
		}
		
		if(players == null) {
			players = new LinkedList<>();
		}
		
		Collections.shuffle(groups);
		Collections.shuffle(players);
		
		for(String player : players) {
			Group solo = new Group();
			solo.addPlayer(player);
			groups.add(solo);
		}
		
		LinkedList<Team> fillFirst = new LinkedList<>();
		LinkedList<Team> fillLast = new LinkedList<>();
		
		for(TeamBuilder tb : teamBuilders) {
			if(tb.getName().contains(",") || tb.getName().contains("-")) {
				throw new TeamNameException("Unable to have ',' or '-' in team names");
			}
			Team current = new Team(tb);
			if(tb.isOverflow()) {
				fillLast.add(current);
			}else {
				fillFirst.add(current);
			}
		}
		
		LinkedList<Group> groupsToAdd = new LinkedList<>(groups);
		
		Collections.shuffle(groupsToAdd);
		
		while(groupsToAdd.size() > 0) {
			Group nextGroup = groupsToAdd.poll();
			PriorityQueue<Team> pff = new PriorityQueue<>(fillFirst);
			PriorityQueue<Team> sff = new PriorityQueue<>(fillLast);
			boolean inserted = false;
			while(!inserted) {
				Team t;
				if(pff.size() > 0) {
					t = pff.poll();
				} else if(sff.size() > 0) {
					t = sff.poll();
				} else {
					throw new GroupCreationException("Not enough spaces on teams available");
				}
				if(t.canFit(nextGroup)) {
					if(t.isCompatible(nextGroup)) {
						inserted = true;
						t.addGroup(nextGroup);
					}
				}
			}
		}
		
		teams.addAll(fillFirst);
		teams.addAll(fillLast);
		
		return teams;
	}
	
	
	static public class GroupCreationException extends Exception {

		private static final long serialVersionUID = 1L;

		public GroupCreationException(String string) {
			super(string);
		}
	}
	
	static public class TeamNameException extends Exception {

		private static final long serialVersionUID = 1L;

		public TeamNameException(String string) {
			super(string);
		}
	}
}
//[team names/arguments] players space delmited {groups of players} <avoid>
//<team name> -o -m
//
//Examples
//========
//A team with a maximum of 5 players called One
//One -m5
//
//A team that holds overflow called Spectators
//Spectators -o
//
//<2 3, 1 3> [One -m3, Two -m3, Spectators -o] {Reba Jason} Sam Anthony Ethan {Ben Rob}
//
///teams help