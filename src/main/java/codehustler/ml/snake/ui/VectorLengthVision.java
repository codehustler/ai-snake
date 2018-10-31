package codehustler.ml.snake.ui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.la4j.Matrix;
import org.la4j.Vector;

import codehustler.ml.snake.GameOptions;
import codehustler.ml.snake.util.Algebra;
import codehustler.ml.snake.util.NeedfulThings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VectorLengthVision implements SnakeVision {

	private final Snake snake;
	
	private final int angleIncrement = 15;


	@Getter
	private List<Double> observations = new ArrayList<>();
	
	private List<Vector> visionRays = new ArrayList<>();


	@Override
	public int getInputSize() {
		return 360/angleIncrement + 3;
	}

	@Override
	public void updateObservations() {
		observations.clear();
		visionRays.clear();

		// part 1: distance to food tile
		Tile foodTile = snake.getFoodTile();
//	
		Tile leftTile = snake.getMap()
				.getTileUnderPosition(snake.getPosition().add(snake.getVelocity().multiply(Algebra.ROT_MATRIX_CCW_90)));
		Tile frontTile = snake.getMap().getTileUnderPosition(snake.getPosition().add(snake.getVelocity()));
		Tile rightTile = snake.getMap()
				.getTileUnderPosition(snake.getPosition().add(snake.getVelocity().multiply(Algebra.ROT_MATRIX_CW_90)));


		Vector left2FoodVec = foodTile.getCenter().subtract(leftTile.getCenter());
		Vector front2FoodVec = foodTile.getCenter().subtract(frontTile.getCenter());
		Vector right2FoodVec = foodTile.getCenter().subtract(rightTile.getCenter());
		

		double[] foodDistances = NeedfulThings.minMaxNormalize(new double[] {left2FoodVec.euclideanNorm(), front2FoodVec.euclideanNorm(), right2FoodVec.euclideanNorm()});

		observations.add(foodDistances[0]);
		observations.add(foodDistances[1]);
		observations.add(foodDistances[2]);
		
		if ( foodDistances[0] <= Math.min(foodDistances[1], foodDistances[2]) ) {
			visionRays.add(NeedfulThings.combine(leftTile.getCenter(), left2FoodVec));
		}
		
		if ( foodDistances[1] <= Math.min(foodDistances[0], foodDistances[2]) ) {
			visionRays.add(NeedfulThings.combine(frontTile.getCenter(), front2FoodVec));
		}
		
		if ( foodDistances[2] <= Math.min(foodDistances[1], foodDistances[0]) ) {
			visionRays.add(NeedfulThings.combine(rightTile.getCenter(), right2FoodVec));
		}

		
		

		double visionDistance = Tile.TILE_SIZE * 6;
		
		// part 2: distance to bad tiles
		Vector visionRay = snake.getVelocity().multiply(visionDistance / snake.getVelocity().euclideanNorm());
		

		Matrix matrix = Algebra.createRotationMatrix(angleIncrement);

		List<Double> collisionDistances = new ArrayList<>();

		for (int angle = angleIncrement; angle < 360; angle += angleIncrement) {
			
			Vector a_b = NeedfulThings.combine(snake.getPosition(), snake.getPosition().add(visionRay));
			Optional<Double> distanceOpt = Algebra.intersectionDistance(a_b, snake.getMap().getWalls());
			collisionDistances.add(distanceOpt.orElse(0d));
			
			visionRays.add(visionRay.multiply(distanceOpt.orElse(visionDistance) / visionDistance));
			visionRay = visionRay.multiply(matrix);
		}
		
		collisionDistances.stream().map(d -> d != 0 ? Tile.TILE_SIZE / d : 1).forEach(d -> {			
			observations.add(d);
		});
		

	}

	@Override
	public void render(Graphics2D g) {
		if (GameOptions.isRenderFieldOfView()) {
			visionRays.forEach(v->{
				if ( v.length() == 2 ) {
					NeedfulThings.drawVector(snake.getPosition(), v, 1, g);
				} else {
					Vector a = Vector.fromArray(new double[] {v.get(0), v.get(1)});
					Vector b = Vector.fromArray(new double[] {v.get(2), v.get(3)});
					NeedfulThings.drawVector(a, b, 1, g);
				}
			});
		}
	}
}
