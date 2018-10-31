package codehustler.ml.snake.util;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.la4j.Matrix;
import org.la4j.Vector;

import codehustler.ml.snake.ui.GameMap;
import codehustler.ml.snake.ui.Wall;

public class Algebra {
	
	public static final Matrix ROT_MATRIX_CW_90 = createRotationMatrix(-90);
	public static final Matrix ROT_MATRIX_CCW_90 = createRotationMatrix(90);
	public static final Matrix ROT_MATRIX_180 = createRotationMatrix(180);
	
	public static Matrix createRotationMatrix(int angleDeg) {
		double angle = Math.toRadians(angleDeg);
		
		Matrix rotationMatrix = Matrix.zero(2, 2);
		rotationMatrix.set(0, 0, Math.cos(angle));
		rotationMatrix.set(1, 0, Math.sin(angle));
		rotationMatrix.set(0, 1, -Math.sin(angle));
		rotationMatrix.set(1, 1, Math.cos(angle));
		
		return  rotationMatrix;
	}
	
	/**
	 * calculate the distance of the line of sight vector intersection point
	 * 
	 * @param lineOfSight
	 * @param walls
	 * @return
	 */
	public static Optional<Double> intersectionDistance(Vector lineOfSight, Set<Wall> walls) {
		AtomicReference<Double> closestDistance = new AtomicReference<>();
		Vector position = Vector.fromArray(new double[] {lineOfSight.get(0), lineOfSight.get(1)});
		
		walls.stream().flatMap(wall -> wall.getEdges().stream()).forEach(tileEdge -> {
			Optional<Vector> intersection = lineIntersect(lineOfSight, tileEdge);
			intersection.ifPresent(intersectionPoint -> {
				double distance = position.subtract(intersectionPoint).euclideanNorm();

				if (closestDistance.get() == null || distance < closestDistance.get()) {
//					System.out.println(position + " --> " + intersectionPoint + " = " + distance);
					closestDistance.set(distance);
				}
			});
		});

		return Optional.ofNullable(closestDistance.get());
	}


	/**
	 * calculate the intersection point between the two lines
	 *  
	 * @param a
	 * @param b
	 * @return
	 */
	public static Optional<Vector> lineIntersect(Vector a, Vector b) {
		double p1X = a.get(0);
		double p1Y = a.get(1);
		double p2X = a.get(2);
		double p2Y = a.get(3);

		double p3X = b.get(0);
		double p3Y = b.get(1);
		double p4X = b.get(2);
		double p4Y = b.get(3);

		double denom = (p4Y - p3Y) * (p2X - p1X) - (p4X - p3X) * (p2Y - p1Y);
		if (denom == 0.0) { // Lines are parallel.
			return Optional.empty();
		}
		double ua = ((p4X - p3X) * (p1Y - p3Y) - (p4Y - p3Y) * (p1X - p3X)) / denom;
		double ub = ((p2X - p1X) * (p1Y - p3Y) - (p2Y - p1Y) * (p1X - p3X)) / denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersection point.
			return Optional.of(Vector.fromArray(new double[] { p1X + ua * (p2X - p1X), p1Y + ua * (p2Y - p1Y) }));
		}

		return Optional.empty();
	}	
}
