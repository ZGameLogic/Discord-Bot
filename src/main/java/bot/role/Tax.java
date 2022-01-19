package bot.role;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tax implements Serializable {

	private static final long serialVersionUID = 2306193137000575912L;
	
	private int taxAmount;
	private long roleID;
	
	public Tax(int taxAmount, long roleID) {
		this.taxAmount = taxAmount;
		this.roleID = roleID;
	}

}
