package bot.role.data;

import java.time.OffsetDateTime;

import data.serializing.SaveableData;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ShopItem extends SaveableData {
	
	private static final long serialVersionUID = -931308626344266341L;

	private Item item;
	@Setter
	private int cost;
	@Setter
	private long currentBidder;
	
	private OffsetDateTime timeDepart;

	/**
	 * @param id
	 * @param item
	 * @param cost
	 * @param currentBidder
	 * @param timeDepart
	 */
	public ShopItem(long id, Item item, int cost, long currentBidder, OffsetDateTime timeDepart) {
		super(id);
		this.item = item;
		this.cost = cost;
		this.currentBidder = currentBidder;
		this.timeDepart = timeDepart;
	}
	
	
	
}
