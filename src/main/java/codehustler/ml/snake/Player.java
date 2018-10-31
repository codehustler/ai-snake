package codehustler.ml.snake;

import java.util.Comparator;

/**
 * 
 * basic player interface, used for human and ai players
 *
 */
public interface Player extends Comparable<Player> {
	
	/**
	 * @return the score of the player
	 */
	long getScore();
	void setScore(long score);
	
	int getLives();
	void setLives(int score);
	
	long getSteps();
	void setSteps(long steps);
	
	double getExploration();
	void setExploration(double exploration);

	/**
	 * @param inputs the observations made by the player
	 */
	void setInputs(double[] inputs);
	
	boolean isTurnLeft();
	
	boolean isTurnRight();
	
	void save(String name);
	void restore(String name);
	
	default int compareTo(Player o) {
		//return Comparator.comparingLong(Player::getScore).thenComparingDouble(p->1d/p.getSteps()).compare(this, o);
		return Comparator.comparingLong(Player::getScore).thenComparingDouble(Player::getExploration).compare(this, o);
	}
	
	default void givePositiveFeedBack() {};
	default void giveNegativeFeedBack() {};
}
