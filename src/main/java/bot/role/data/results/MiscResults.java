package bot.role.data.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Getter
public class MiscResults extends SavableData {

    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    public MiscResults(String message){
        this.message = message;
        date = new Date();
    }
}
