package data.database.arena.misc;

import java.util.HashSet;
import java.util.LinkedList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Game_Information")
public class GameInformation {
    @Id
    private String id;
    
    // tax stuff
    private long taxRoleID;
    private int taxAmount;
	
    // king stuff
    private LinkedList<Long> playersFoughtKing;
    
    // daily remind stuff
    private HashSet<Long> dailyRemindIDs;
    
    /* Daily remind stuff */
    
	public void addDailyID(long id) {
		dailyRemindIDs.add(id);
	}
	
	public void removeID(long id) {
		dailyRemindIDs.remove(id);
	}
    
    /* King stuff */
    
	public void addPlayerKingFought(long id) {
		playersFoughtKing.add(id);
	}
	
	public void resetList() {
		playersFoughtKing = new LinkedList<>();
	}
	
	public boolean canFightKing(long id) {
		return !playersFoughtKing.contains(id);
	}
    
    /* Tax Stuff */
	
    public void setTax(long taxRoleID, int taxAmount) {
    	this.taxRoleID = taxRoleID;
    	this.taxAmount = taxAmount;
    }
    
    public void resetTax() {
    	taxRoleID = 0;
    	taxAmount = 0;
    }
}
