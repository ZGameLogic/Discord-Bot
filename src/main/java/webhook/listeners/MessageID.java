package webhook.listeners;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageID implements Serializable {

	private static final long serialVersionUID = 9177194505177887262L;

	private long id;
	
}
