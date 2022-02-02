package bot.role.data;

import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class ShopItem implements Serializable {
	
	private static final long serialVersionUID = -931308626344266341L;
	@Setter
	private long id;
	private Item item;
	@Setter
	private int cost;
	@Setter
	private long currentBidder;
	
	private OffsetDateTime timeDepart;
	
}
