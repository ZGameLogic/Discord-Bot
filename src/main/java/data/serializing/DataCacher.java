package data.serializing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class DataCacher <T extends SaveableData> {

	private final String filePath;

	public DataCacher(String filePath) {
		File f = new File(filePath);
		if(!f.exists()) {
			f.mkdirs();
		}
		this.filePath = filePath;
	}

	public boolean exists(String file) {
		file = file.replace(".json", "");
		File f = new File(filePath + "//" + file + ".json");
		return f.exists();
	}

	public boolean exists(long id) {
		return exists(id + "");
	}

	public void saveSerialized(T...yourObject) {
		for(T t : yourObject){
			saveSerialized(t, t.getId());
		}
	}

	/**
	 * Saves the object into the file path that has been given
	 * @param yourObject Object to be serialized
	 * @param file File path to save object too
	 */
	public void saveSerialized(T yourObject, String file) {
		ObjectMapper om = new ObjectMapper();
		try {
			file = file.replace(".json", "");
			om.writerWithDefaultPrettyPrinter().writeValue(new File(filePath + "//" + file + ".json"), yourObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * loads the first datafile in the directory
	 * @return The first data file in the directory
	 */
	public T loadSerialized() {
		return loadSerialized(getFiles()[0].getName());
	}

	/**
	 * Loads the object from file path that has been given
	 * @param id id of the file you want loaded
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T loadSerialized(String id) {
		T data1;
		ObjectMapper om = new ObjectMapper();
		try {
			id = id.replace(".json", "");
			data1 = (T) om.readValue(new File(filePath + "//" + id + ".json"), SaveableData.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return data1;
	}

	/**
	 * Loads the object from file path that has been given
	 * @param id id of the file you want loaded
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T loadSerialized(long id) {
		return loadSerialized(id + "");
	}

	public void delete(String file) {
		file = file.replace(".json", "");
		File fileToDelete = new File(filePath + "//" + file + ".json");
		fileToDelete.delete();
	}

	public void deleteDir(){
		File file = new File(filePath);
		deleteDirectory(file);
	}

	public File[] getFiles() {
		File f = new File(filePath);
		return f.listFiles();
	}

	public void saveSerialized(LinkedList<T> list) {
		for(T object : list) {
			saveSerialized(object);
		}
	}

	public LinkedList<T> getData(){
		LinkedList<T> list = new LinkedList<>();
		for(File f : getFiles()) {
			list.add(loadSerialized(f.getName()));
		}
		return list;
	}

	/**
	 * Gets a map of the data with the key being the ID of the object
	 * @return Map of data with the IDs as the keys
	 */
	public HashMap<String, T> getMappedData(){
		HashMap<String, T> map = new HashMap<>();
		for(File f : getFiles()) {
			map.put(f.getName(), loadSerialized(f.getName()));
		}
		return map;
	}

	/**
	 * Generates an id that has not been taken yet in the data directory
	 * @return unique long id
	 */
	public long generateID(){
		File dir = new File(filePath);
		Random random = new Random();
		long id = random.nextLong();
		while(id < 0 || new File(filePath + "\\" + id).exists()){
			id = random.nextLong();
		}
		return id;
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}