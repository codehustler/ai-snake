package codehustler.ml.snake.ui;

import java.awt.Graphics2D;
import java.util.List;

public interface SnakeVision {
	int getInputSize();
	List<Double> getObservations();
	void updateObservations();
	void render(Graphics2D g);
}
