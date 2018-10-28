package codehustler.ml.snake.ai;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WeightsManager {

	public static void saveWeights(Double[] weights, String filename) {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
			outputStream.writeObject(weights);			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static Double[] loadWeights(String filename) {
		try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
			return (Double[])inputStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		} 
	}

}
