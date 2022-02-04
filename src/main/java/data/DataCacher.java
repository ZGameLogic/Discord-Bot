package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;

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
		File f = new File(filePath + "//" + file);
		return f.exists();
	}
	
	public void saveSerialized(T yourObject) {
		saveSerialized(yourObject, yourObject.getId());
	}
	
	/**
	 * Saves the object into the file path that has been given 
	 * @param yourObject Object to be serialized 
	 * @param file File path to save object too
	 */
	public void saveSerialized(T yourObject, String file) {
		
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(filePath + "//" + file));
			outputStream.writeObject(yourObject);	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(outputStream != null) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}		
	}
	
	/**
	 * Loads the object from file path that has been given 
	 * @param file File path to load object from
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T loadSerialized(String file) {
		
		T data1 = null;
		
		try {
			FileInputStream fileIn = new FileInputStream(filePath + "//" + file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			try {
				data1 = (T) in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data1;
	}
	
	public void delete(String file) {
		File fileToDelete = new File(filePath + "//" + file);
		fileToDelete.delete();
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
}