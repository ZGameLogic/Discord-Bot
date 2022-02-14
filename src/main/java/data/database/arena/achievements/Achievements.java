package data.database.arena.achievements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import lombok.Getter;

@Getter
@Embeddable
public class Achievements {

	// Challenge and beat the king with lower than a 1% chance of winning
	private boolean againstAllOdds;
	
	// win a challenge against everyone at least once
	private boolean runningTheGauntlet;
	
	// Challenge and win against a member in each caste
	private boolean completingTheRounds;
	
	// Lose to a member in each caste
	private boolean punchingBag;
	
	// Have 1,000,000 gold or more at one time
	private boolean millionare;
	
	// Have more than 1000 gold at one time
	private boolean goldenTouch;
	
	// wins: 10, 100, 200, 1000
	private boolean bloodOnYourHands;
	private boolean redLedger;
	private boolean itsJustForSport;
	private boolean betterThanThePlague;
	
	// Win a fight against Anthony
	private boolean oneBirdWithOneStone;
	
	@Column
	@ElementCollection
	private List<Long> runningTheGauntletProgress;
	@Column
	@ElementCollection
	private List<Long> completingTheRoundsProgress;
	@Column
	@ElementCollection
	private List<Long> punchingBagProgress;
	
	private HashMap<String, String> announce;
	
	public Achievements() {
		againstAllOdds = false;
		runningTheGauntlet = false;
		completingTheRounds = false;
		punchingBag = false;
		millionare = false;
		bloodOnYourHands = false;
		redLedger = false;
		itsJustForSport = false;
		betterThanThePlague = false;
		oneBirdWithOneStone = false;
		goldenTouch = false;
		
		runningTheGauntletProgress = new LinkedList<>();
		completingTheRoundsProgress = new LinkedList<>();
		punchingBagProgress = new LinkedList<>();
		announce = new HashMap<>();
	}

	public LinkedList<String> getEarnedAchievements(){
		LinkedList<String> names = new LinkedList<>();
		if(againstAllOdds)
			names.add("Against all odds");
		if(runningTheGauntlet)
			names.add("Running the gauntlet");
		if(completingTheRounds)
			names.add("Completeing the rounds");
		if(punchingBag)
			names.add("Punching bag");
		if(millionare)
			names.add("Millionare");
		if(bloodOnYourHands)
			names.add("Blood on your hands");
		if(redLedger)
			names.add("Red Ledger");
		if(itsJustForSport)
			names.add("Its just for sport");
		if(betterThanThePlague)
			names.add("Better than the plague");
		if(oneBirdWithOneStone)
			names.add("One bird with one stone");
		if(goldenTouch)
			names.add("Golden touch");
		return names;
	}
	
	public int getRunningTheGauntletProgress() {
		return runningTheGauntletProgress.size();
	}
	
	public int getCompletingTheRoundsProgress() {
		return completingTheRoundsProgress.size();
	}
	
	public int getPunchingBagProgress() {
		return punchingBagProgress.size();
	}
	
	public void progressRunningTheGauntlet(long id) {
		runningTheGauntletProgress.add(id);
	}
	
	public void progressCompletingTheRounds(long id) {
		completingTheRoundsProgress.add(id);
	}
	
	public void progressPunchingBad(long id) {
		punchingBagProgress.add(id);
	}
	
	public void setGoldenTouch(boolean goldenTouch) {
		if(!this.goldenTouch && goldenTouch) {
			announce.put("Golden touch", "Have 1,000 or more gold at one time");
		}
		this.goldenTouch = goldenTouch;
	}

	public void setAgainstAllOdds(boolean againstAllOdds) {
		if(!this.againstAllOdds && againstAllOdds) {
			announce.put("Against all odds", "Challenge and beat the king with lower than a 1% chance of winning");
		}
		this.againstAllOdds = againstAllOdds;
	}

	public void setRunningTheGauntlet(boolean runningTheGauntlet) {
		if(!this.runningTheGauntlet && runningTheGauntlet) {
			announce.put("Running the guantlet", "Challange and win against everyone at least once");
		}
		this.runningTheGauntlet = runningTheGauntlet;
	}

	public void setCompletingTheRounds(boolean completingTheRounds) {
		if(!this.completingTheRounds && completingTheRounds) {
			announce.put("Completing the Rounds", "Challange and win against a player in every caste");
		}
		this.completingTheRounds = completingTheRounds;
	}

	public void setPunchingBag(boolean punchingBag) {
		if(!this.punchingBag && punchingBag) {
			announce.put("Punching bag", "Challange and lose to a player in each caste");
		}
		this.punchingBag = punchingBag;
	}

	public void setMillionare(boolean millionare) {
		if(!this.millionare && millionare) {
			announce.put("Millionare", "Have 1,000,000 gold");
		}
		this.millionare = millionare;
	}

	public void setBloodOnYourHands(boolean bloodOnYourHands) {
		if(!this.bloodOnYourHands && bloodOnYourHands) {
			announce.put("Blood on your hands", "Win 10 fights");
		}
		this.bloodOnYourHands = bloodOnYourHands;
	}

	public void setRedLedger(boolean redLedger) {
		if(!this.redLedger && redLedger) {
			announce.put("Red ledger", "Win 100 fights");
		}
		this.redLedger = redLedger;
	}

	public void setItsJustForSport(boolean itsJustForSport) {
		if(!this.itsJustForSport && itsJustForSport) {
			announce.put("Its just for sport", "Win 200 fights");
		}
		this.itsJustForSport = itsJustForSport;
	}

	public void setBetterThanThePlague(boolean betterThanThePlague) {
		if(!this.betterThanThePlague && betterThanThePlague) {
			announce.put("Better than the plague", "Win 1000 fights");
		}
		this.betterThanThePlague = betterThanThePlague;
	}

	public void setOneBirdWithOneStone(boolean oneBirdWithOneStone) {
		if(!this.oneBirdWithOneStone && oneBirdWithOneStone) {
			announce.put("One bird with one stone", "Challenge Anthony and win");
		}
		this.oneBirdWithOneStone = oneBirdWithOneStone;
	}
	
	public void clearAnnounce() {
		announce = new HashMap<String, String>();
	}
}
