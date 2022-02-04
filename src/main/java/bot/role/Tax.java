package bot.role;

import data.SaveableData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tax extends SaveableData {

	private static final long serialVersionUID = 2306193137000575912L;
	
	private int taxAmount;
	private long roleID;
	
	public Tax(String id, int taxAmount, long roleID) {
		super(id);
		this.taxAmount = taxAmount;
		this.roleID = roleID;
	}

}
