package data.intermediates.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@NoArgsConstructor
public class Message {
    private String message;
    private Long guildId;
    private String channelId;
    private LinkedList<String> mentionIds;
}
