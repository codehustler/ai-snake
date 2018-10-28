package codehustler.ml.snake.ai;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.data.norm.MaxMinNormalizer;
import org.neuroph.util.random.DistortRandomizer;

import codehustler.ml.snake.AbstractPlayer;
import codehustler.ml.snake.ui.Snake;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode(of="uuid", callSuper = false)
public class AIPlayer extends AbstractPlayer {
	
	private static Random R = new Random(System.currentTimeMillis());


	private MaxMinNormalizer normalizer;
	
	private final String uuid; 

	private NeuralNetwork<BackPropagation> model;	
	private double distortionRate = 1.05;
	
	private int inputSize = 24+3 /*x/y distance to food*/;
	private int outputSize = 3;

	DataSet currentSet = new DataSet(inputSize, outputSize);

	public AIPlayer() {
		this.uuid = UUID.randomUUID().toString();
		createModel();
	}

	public AIPlayer(AIPlayer otherPlayer) {
		this.uuid = UUID.randomUUID().toString();
		
		cloneModel(otherPlayer.getModel());
	}

	public AIPlayer(String weightsFile) {
		this.uuid = UUID.randomUUID().toString();
		createModel();
		restore(weightsFile);
	}
	
	private MultiLayerPerceptron createBasicModel() {
		int[] layerConfig = new int[] { inputSize, inputSize, outputSize };
		MultiLayerPerceptron model = new MultiLayerPerceptron(TransferFunctionType.LINEAR, layerConfig);
		model.setLearningRule(new BackPropagation());
		return model;
	}

	private void cloneModel(NeuralNetwork<BackPropagation> sourceModel) {
		MultiLayerPerceptron model = createBasicModel();
		
		model.setWeights(Arrays.stream(sourceModel.getWeights()).mapToDouble(Double::doubleValue).toArray());
		DistortRandomizer distortRandomizer = new DistortRandomizer(distortionRate);
		distortRandomizer.setRandomGenerator(R);
		model.randomizeWeights(distortRandomizer);
		this.model = model;
	}

	private void createModel() {
		MultiLayerPerceptron model = createBasicModel();
		model.randomizeWeights(R);
		this.model = model;
	}

	public void setInputs(double[] inputs) {
//		this.score++;
		
		
//		inputs = maxNormalize(inputs);
//		inputs = invertInputs(inputs);
		
//		Arrays.stream(inputs).forEach(i -> System.out.print(i + "  ###  "));
//		System.out.println();
		
		model.setInput(inputs);
		model.calculate();

		double[] output = model.getOutput();
		
		currentSet.clear();
		currentSet.addRow(inputs, output);
	
		double left = output[0];
		double straight = output[1];
		double right = output[2];
		
		if ( left > right && left > straight ) {
			turnLeft = true;
			turnRight = false;
		} else if ( right > left && right > straight ) {
			turnLeft = false;
			turnRight = true;
		} else {
			turnLeft = false;
			turnRight = false;
		}
	}
	
	public void givePositiveFeedBack() {
		//model.getLearningRule().doLearningEpoch(currentSet);
		this.score++;
	}

	@Override
	public void giveNegativeFeedBack() {
		this.score--;
	}
	
	public static double[] invertInputs(double[] input) {
		for (int n = 0; n < input.length; n++ ) {
			input[n] = 1/input[n];
		}
		return input;
	}
	
	public static double[] maxNormalize(double[] input) {
		Double max = Math.min(Arrays.stream(input).boxed().collect(Collectors.maxBy(Double::compareTo)).get(), 40000);
		return Arrays.stream(input).boxed().map(v -> v / max).mapToDouble(Double::doubleValue).toArray();
	}

	@Override
	public void save(String name) {
		WeightsManager.saveWeights(model.getWeights(), name + ".weights");
	}

	@Override
	public void restore(String id) {
		this.model.setWeights(
				Arrays.stream(WeightsManager.loadWeights(id)).mapToDouble(Double::doubleValue).toArray());
	}
}
