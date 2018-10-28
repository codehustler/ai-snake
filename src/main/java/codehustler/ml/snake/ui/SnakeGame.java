package codehustler.ml.snake.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import codehustler.ml.snake.HumanPlayer;
import codehustler.ml.snake.Player;
import codehustler.ml.snake.PlayerFactory;
import lombok.Getter;
import lombok.Setter;

public class SnakeGame extends JFrame {
	public static final Random R = new Random(System.currentTimeMillis());
	
	private static final long serialVersionUID = 1L;
	private static final int SLOW_DELAY = 800;
	private static final int FAST_DELAY = 0;
	
	
	@Getter @Setter
	private int targetLaps = 3;

	private GameScreen gameScreen;
	@Getter
	private GameMap map;

	private int generationCounter = 0;
	
	@Getter
	private List<Snake> runners = Collections.synchronizedList(new ArrayList<>());

	@Setter
	private int selectedDelay = FAST_DELAY;

	@Setter
	private boolean rendergame = true;

	@Getter
	private long timecode = 0;

	@Setter
	private PlayerFactory playerFactory;

	private GameInputHandler inputHandler;
	
	/** 
	 * starts the game with a human player
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SnakeGame game = new SnakeGame(30);
		game.setPlayerFactory(new PlayerFactory() {
			@Override
			public Set<Player> createPlayers() {
				return Collections.singleton(new HumanPlayer(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT));
			}
		});
		game.run();
	}
	

	public SnakeGame(int mapSize) throws Exception {
		this.setSize(400, 400);
		this.setTitle("AI Runner");
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.inputHandler = new GameInputHandler(this);
		this.addKeyListener(inputHandler);

		this.map = new GameMap(mapSize, this);

		gameScreen = new GameScreen(this);

		this.add(gameScreen, BorderLayout.CENTER);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				SnakeGame.this.setVisible(true);
			}
		});
	}


	private List<Snake> createRunners(Set<Player> players) {
		players.stream().filter(HumanPlayer.class::isInstance).forEach(inputHandler::addHumanPlayer);
		return players.stream().map(p -> new Snake(p, this)).collect(Collectors.toList());
	}

	public void run() {
		System.out.println("creating initial population");
		runners.clear();
		runners.addAll(createRunners(playerFactory.createPlayers()));

		while (true) {
			updateGame();
			if (rendergame && selectedDelay > 0) {
				try {
					Thread.sleep(selectedDelay);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	private void resetPopulation() {
		generationCounter++;
		map.clearVisitorCache();
		runners.clear();
		runners.addAll(createRunners(playerFactory.createPlayers()));
	}

	private boolean isPopulationAlive() {
		return this.runners.stream().filter(m -> !m.isGameOver()).count() > 0;
	}

	public void updateGame() {
		if (!isPopulationAlive()) {
			resetPopulation();
		}

		runners.parallelStream().forEach(Snake::update);
		this.setTitle(String.format("AI Runner - Generation: %s Population: %s", generationCounter,
				runners.stream().filter(m -> !m.isGameOver()).count()));

		if (rendergame) {
			gameScreen.repaint();
		}

		timecode++;
	}

	public void toggleAnimationSpeed() {
		if (selectedDelay == SLOW_DELAY) {
			selectedDelay = FAST_DELAY;
		} else {
			selectedDelay = SLOW_DELAY;
		}
	}

	public void killCurrentPopulation() {
		runners.forEach(m -> m.setGameOver(true));
	}

	public void toggleRender() {
		rendergame = !rendergame;
	}
}
