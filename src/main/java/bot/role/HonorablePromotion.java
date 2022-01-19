package bot.role;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HonorablePromotion implements Serializable {
	
	private static final long serialVersionUID = 7943149063173279147L;
	
	private boolean usedToday;

}
