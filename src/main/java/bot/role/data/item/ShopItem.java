package bot.role.data.item;

import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class ShopItem extends SaveableData {

    private Item item;
    private Date dateToDelete;

    public ShopItem(long id, Item item, Date dateToDelete){
        super(id);
        this.item = item;
        this.dateToDelete = dateToDelete;
    }
}
