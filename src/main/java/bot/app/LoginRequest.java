package bot.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import data.serializing.SavableData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;

@NoArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoginRequest extends SavableData {
    private String uid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expires;
    @Setter
    private boolean approved;

    public LoginRequest(String id, String uid) {
        Clock c = Clock.systemUTC();
        c = Clock.offset(c, Duration.ofHours(1)); // set to expire in 1 hour
        expires = new Date(c.millis());
        approved = false;
        setId(id);
        this.uid = uid;
    }
}
