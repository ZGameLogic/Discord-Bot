package bot.role.data.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class ShopItem extends SaveableData {

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
}
