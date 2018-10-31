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
		
		
		//grant all players an additional live if they beat the high score		
		population.stream().filter(p->record != null).filter(p -> p.getScore() > record.getScore()).forEach(p->{			
			p.setLives(p.getLives()+1);
			System.out.println("adding a live!");
		});
		
		
		
		//TODO the entire life thing is still wip
		
		
		List<Player> playersWithLives = population.stream().filter(p->p.getLives() > 0 ).collect(Collectors.toList());
		players.addAll(playersWithLives);
		
		playersWithLives.forEach(p->{
			System.out.println(p.getLives());
		});
		
		
		
		if ( playersWithLives.size() > 0 ) {
			System.out.println();
		}
		
		int newPlayers = populationSize - players.size();
		
		System.out.println("new players required: " + newPlayers);
		
		
		int playersToKeep = (int) (newPlayers * 0.2d);
		int playersToBreed = (int) (newPlayers * 0.7d);
		

//		System.out.println("generation result: ");
		Optional<Player> best = population.stream().sorted().collect(Collectors.maxBy(Comparator.naturalOrder()));
		
		best.ifPresent(p->{
//			System.out.println("best: " + p.getScore() + "_" + (int)(p.getExploration()*100));
			if ( record == null || p.getScore() > record.getScore() ) {
				record = p;
				System.out.println("new record: " + p.getScore());
			}
		});
		
		//keep the good ones
		List<Player> survivors = population.stream().sorted()
				.skip(populationSize  - playersToKeep).peek(p->{
					p.setScore(0);
					p.setSteps(0);
					p.setExploration(0);		
				}).collect(Collectors.toList());		
		players.addAll(survivors);
		System.out.println("survivors: " + survivors.size());
		
		//breed new ones from the good ones
		if ( survivors.size() > 0) {
			List<Player> newBorns = IntStream.range(0, playersToBreed)
					.mapToObj(n -> createAIPlayer((AIPlayer)survivors.get(R.nextInt(survivors.size()))))
					.collect(Collectors.toList());
			players.addAll(newBorns);
			System.out.println("breeds: " + newBorns.size());
		}

		
		//top up the list with new ones
		while ( players.size() < populationSize ) {
			players.add(new AIPlayer());
			System.out.println("brand new player added");
		}


		

		
		population.clear();
		population.addAll(players);
		return players;
	}
}
