package data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DataCacher {
	
	/**
	 * Saves the object into the file path that has been given 
	 * @param yourObject Object to be serialized 
	 * @param filePath File path to save object too
	 */
	public static void saveSerialized(Object yourObject, String filePath) {
		
		ObjectOutputStream outputStream = null;
		
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(filePath));
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
	 * @param filePath File path to load object from
	 * @return
	 */
	public static Object loadSerialized(String filePath) {
		
		Object data1 = null;
		
		try {
		
			FileInputStream fileIn = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			try {
				data1 = in.readObject();
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
	

}
