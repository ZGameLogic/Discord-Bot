package data.intermediates.messaging;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;

@Data
@NoArgsConstructor
public class Message {
    private String message;
    private Long guildId;
    private LinkedList<String> mentionIds;
}
