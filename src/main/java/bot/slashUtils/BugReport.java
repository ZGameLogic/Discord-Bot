package bot.slashUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BugReport extends SaveableData {
    private String issueNumber;
    private long reporterId;

    public BugReport(long id, String issueNumber, long reporterId) {
        super(id);
        this.issueNumber = issueNumber;
        this.reporterId = reporterId;
    }
}
