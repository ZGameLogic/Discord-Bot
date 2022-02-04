package data;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class SaveableData implements Serializable {

	private static final long serialVersionUID = 4768645644849086833L;

	private String id;
	
	public SaveableData(long id) {
		this.id = id + "";
	}
	
	public void setId(long id) {
		this.id = id + "";
	}
	
	public long getIdLong() {
		return Long.parseLong(id);
	}
}
