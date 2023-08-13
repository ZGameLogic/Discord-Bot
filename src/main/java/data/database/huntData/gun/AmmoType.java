package data.database.huntData.gun;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AmmoType {

    private String name;
    private String asset;
    private boolean special;
    private boolean secondarySlotOnly;

    public String toString(){ return name; }

}