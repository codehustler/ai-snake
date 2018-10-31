package codehustler.ml.snake.ai;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import codehustler.ml.snake.Player;
import codehustler.ml.snake.PlayerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AIPlayerFactory implements PlayerFactory {
	
	private static final Random R = new Random(System.currentTimeMillis()); 

	private final int populationSize;
	
	private Set<Player> population = new HashSet<>();
	
	private Player record = null;


	public Player createAIPlayer(AIPlayer player) {
		return new AIPlayer(player);
	} 

	@Override
	public Set<Player> createPlayers() {
		Set<Player> players = new HashSet<>();
		
		int playersToKeep = (int) (population.size() * 0.2d);
		int playersToBreed = (int) (population.size() * 0.7d);
		int newRandomPlayers = (int) (populationSize-(playersToKeep+playersToBreed));

//		System.out.println("generation result: ");
		Optional<Player> best = population.stream().sorted().collect(Collectors.maxBy(Comparator.naturalOrder()));
		
		best.ifPresent(p->{
//			System.out.println("best: " + p.getScore() + "_" + (int)(p.getExploration()*100));
			if ( record == null || p.getScore() > record.getScore() ) {
				record = p;
			}
		});
		
		List<Player> survivors = population.stream().sorted()
				.skip(populationSize  - playersToKeep).collect(Collectors.toList());
		
		List<Player> newBorns = IntStream.range(0, playersToBreed)
				.mapToObj(n -> createAIPlayer((AIPlayer)survivors.get(R.nextInt(survivors.size()))))
				.collect(Collectors.toList());
		
		 List<Player> newRandoms = IntStream.range(0, newRandomPlayers).mapToObj(n -> new AIPlayer()).collect(Collectors.toList());
		 if ( newRandoms.size() > 0 ) {
			 //System.out.println("new random payers created: " + newRandoms.size());
		 }


		survivors.forEach(p -> {
			p.setScore(0);
			p.setSteps(0);
			p.setExploration(0);
		});

		players.addAll(survivors);
		players.addAll(newBorns);
		players.addAll(newRandoms);

		population.clear();
		population.addAll(players);
		return players;
	}
}
