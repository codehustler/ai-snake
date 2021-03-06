package codehustler.ml.snake.ai;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.data.norm.MaxMinNormalizer;
import org.neuroph.util.random.DistortRandomizer;

import codehustler.ml.snake.AbstractPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode(of="uuid", callSuper = false)
public class AIPlayer extends AbstractPlayer {
	
	private static Random R = new Random(System.currentTimeMillis());


	private MaxMinNormalizer normalizer;
	
	private final String uuid; 

	private NeuralNetwork<BackPropagation> model;	
	private double mutationRate = 1.5;
	
	private int outputSize = 3;
	
	BackPropagation backPropagationLR = new BackPropagation();

	public AIPlayer() {
		this.uuid = UUID.randomUUID().toString();
		this.lives = 3;
//		createBasicModel();
	}

	public AIPlayer(AIPlayer otherPlayer) {
		this.uuid = UUID.randomUUID().toString();
		
		cloneModel(otherPlayer.getModel());
	}

//	public AIPlayer(String weightsFile) {
//		this.uuid = UUID.randomUUID().toString();
//		createBasicModel();
//		restore(weightsFile);
//	}
	
	private MultiLayerPerceptron createBasicModel(int inputSize) {
		int[] layerConfig = new int[] { inputSize, inputSize, outputSize };
		MultiLayerPerceptron model = new MultiLayerPerceptron(TransferFunctionType.LINEAR, layerConfig);
		model.setLearningRule(backPropagationLR);
		model.randomizeWeights(R);
		return model;
	}

	private void cloneModel(NeuralNetwork<BackPropagation> sourceModel) {
		MultiLayerPerceptron model = createBasicModel(sourceModel.getInputNeurons().length);
		
		model.setWeights(Arrays.stream(sourceModel.getWeights()).mapToDouble(Double::doubleValue).toArray());
		DistortRandomizer distortRandomizer = new DistortRandomizer(mutationRate);
		distortRandomizer.setRandomGenerator(R);
		model.randomizeWeights(distortRandomizer);
		this.model = model;
	}


	public void setInputs(double[] inputs) {

		if ( model == null ) {			
			model = createBasicModel(inputs.length);
		} 
		
//		inputs = maxNormalize(inputs);
//		inputs = invertInputs(inputs);
		
//		Arrays.stream(inputs).forEach(i -> System.out.print(i + "  ###  "));
//		System.out.println();
		
		
		model.setInput(inputs);
		model.calculate();

		double[] output = model.getOutput();
		
//		Arrays.stream(output).forEach(i -> System.out.print(i + "  ###  "));
//		System.out.println();System.out.println();System.out.println();
		
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
