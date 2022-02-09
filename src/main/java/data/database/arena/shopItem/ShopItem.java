package data.database.arena.shopItem;

import java.time.OffsetDateTime;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import data.database.arena.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Shop_Items")
public class ShopItem {
	@Id
    private long id;

	@Embedded
	private Item item;
	private int cost;
	private long currentBidder;
	private OffsetDateTime timeDepart;
	
	
	public ShopItem(bot.role.data.ShopItem item2) {
		id = item2.getIdLong();
		item = new Item(item2.getItem());
		cost = item2.getCost();
		currentBidder = item2.getCurrentBidder();
		timeDepart = item2.getTimeDepart();
	}
}
