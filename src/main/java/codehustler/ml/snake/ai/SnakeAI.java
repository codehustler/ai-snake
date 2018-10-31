package codehustler.ml.snake.ai;

import codehustler.ml.snake.ui.SnakeGame;

public class SnakeAI {
	
	public static void main(String[] args) throws Exception {
		SnakeGame game = new SnakeGame(60);
		game.setPlayerFactory(new AIPlayerFactory(20));
		game.run();
	}
}
