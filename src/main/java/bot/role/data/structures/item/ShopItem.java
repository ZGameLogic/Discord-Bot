package bot.role.data.structures.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.Random;

@Getter
@NoArgsConstructor
public class ShopItem extends SavableData {

    private Item item;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateToDelete;
    private int goldCost;

    public ShopItem(long id, Item item, int goldCost, Date dateToDelete){
        super(id);
        this.item = item;
        this.goldCost = goldCost;
        this.dateToDelete = dateToDelete;
    }

    public ShopItem(Item item, Date date, int cost) {
        this.item = item;
        dateToDelete = date;
        goldCost = cost;
    }

    public static ShopItem random() {
        Random r = new Random();
        Item item = Item.random();
        Clock c = Clock.systemUTC();
        c = Clock.offset(c, Duration.ofDays(6 / 2));
        Date date = new Date(c.millis());
        int offset = r.nextInt(3) - 1;
        int cost = r.nextInt((int)(item.getRarity().getMerit() + offset + 1) * 100) + 37;
        return new ShopItem(item, date, cost);
    }
}
