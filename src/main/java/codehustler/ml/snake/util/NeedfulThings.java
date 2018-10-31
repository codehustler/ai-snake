package codehustler.ml.snake.util;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.la4j.Vector;

public class NeedfulThings {
	public static Vector combine(Vector... vectors) {
		List<Double> doubles = Arrays.stream(vectors)//
				.flatMap(v -> StreamSupport.stream(v.spliterator(), false))//
				.collect(Collectors.toList());

		return Vector.fromCollection(doubles);
	}
	
//	public static Vector subVector(Vector v) {
//		List<Double> doubles = Arrays.stream(vectors)//
//				.flatMap(v -> StreamSupport.stream(v.spliterator(), false))//
//				.collect(Collectors.toList());
//
//		return Vector.fromCollection(doubles);
//	}

	public static void drawVector(Vector start, Vector delta, double multiplier, Graphics2D g) {
		Vector end = start.add(delta.multiply(multiplier));
		g.drawLine((int) start.get(0), //
				(int) start.get(1), //
				(int) end.get(0), //
				(int) end.get(1));
	}
	
	public static Vector vector(double... d) {
		return Vector.fromArray(d);
	}
	
	public static double[] invertInputs(double[] input) {
		for (int n = 0; n < input.length; n++ ) {
			input[n] = 1/input[n];
		}
		return input;
	}
	
	public static double[] maxNormalize(double[] input) {
		Double max = Arrays.stream(input).boxed().collect(Collectors.maxBy(Double::compareTo)).get();
		
		if ( max == 0 ) return input;
		
		return Arrays.stream(input).boxed().map(v -> v / max).mapToDouble(Double::doubleValue).toArray();
	}
	
	public static double[] minMaxNormalize(double[] input) {
		Double max = Arrays.stream(input).boxed().collect(Collectors.maxBy(Double::compareTo)).get();
		Double min = Arrays.stream(input).boxed().collect(Collectors.minBy(Double::compareTo)).get();
		
		if ( max == 0 ) return input;
		
		return Arrays.stream(input).boxed().map(v -> (v-min) / (max-min)).mapToDouble(Double::doubleValue).toArray();
	}
}
