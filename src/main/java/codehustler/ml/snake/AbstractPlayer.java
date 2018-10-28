package codehustler.ml.snake;

import lombok.Getter;
import lombok.Setter;


public abstract class AbstractPlayer implements Player {

	
	@Getter @Setter
	protected long score;
	
	@Getter @Setter
	protected long steps;
	
	@Getter @Setter
	protected double exploration;
	
	@Setter
	protected double[] inputs; 
	
	@Getter @Setter
	protected boolean turnLeft;
	
	@Getter @Setter
	protected boolean turnRight;
		
}
