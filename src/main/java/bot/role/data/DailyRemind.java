package bot.role.data;

import java.util.Collection;
import java.util.LinkedList;

import data.serializing.SaveableData;
import lombok.Getter;

public class DailyRemind extends SaveableData {

	private static final long serialVersionUID = 1162583863169051454L;
	
	@Getter
	LinkedList<Long> ids;
	
	public DailyRemind(String id) {
		super(id);
		ids = new LinkedList<>();
	}
	
	public void addID(long id) {
		ids.add(id);
	}
	
	public void removeID(long id) {
		ids.remove(id);
	}
	
	public void updateData(Collection<Long> collection) {
		ids = new LinkedList<>(collection);
	}
}
