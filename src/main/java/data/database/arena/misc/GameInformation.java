package data.database.arena.misc;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
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
    @Column
	@ElementCollection
    private List<Long> playersFoughtKing;
    @Column(columnDefinition = "bigint default 0")
    private long kingID;
    @Column(columnDefinition = "bigint default 0")
    private int kingRun;
    
    // daily remind stuff
    @Column
	@ElementCollection
    private List<Long> dailyRemindIDs;
    
    /* Daily remind stuff */
    
	public void addDailyID(long id) {
		if(!dailyRemindIDs.contains(id)) {
			dailyRemindIDs.add(id);
		}
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
