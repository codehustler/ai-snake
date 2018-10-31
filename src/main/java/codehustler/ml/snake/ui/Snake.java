package codehustler.ml.snake.ui;

import static codehustler.ml.snake.util.Algebra.ROT_MATRIX_CCW_90;
import static codehustler.ml.snake.util.Algebra.ROT_MATRIX_CW_90;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

import com.google.common.collect.EvictingQueue;

import codehustler.ml.snake.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "player")
public strictfp class Snake {
	
	enum CauseOfDeath {STARVATION, SUICIDE, COLLISION}

	private Vector position = BasicVector.fromArray(new double[] { 0d, 0d });
	private Vector velocity;

	private final GameMap map;
	private final Player player;
	private final SnakeGame game;

	private int bodyLength = 4;
	private EvictingQueue<Tile> tail = EvictingQueue.create(bodyLength);
	private Tile headTile;

	private boolean gameOver = false;

	private final int energySizeMultiplzer = 20;
//	private int energy = energySizeMultiplzer * bodyLength;
	private int energy = 200;

	private Tile foodTile;

	private Color snakeColor = new Color(SnakeGame.R.nextInt(256), SnakeGame.R.nextInt(256), SnakeGame.R.nextInt(256));

	private Set<Tile> exploredTiles = new HashSet<>();
	
	private CauseOfDeath causeOfDeath = null;
	
	
//	private SnakeVision snakeVision = new TileGridVision(this);
	private SnakeVision snakeVision = new VectorLengthVision(this);

	public Snake(Player player, SnakeGame game) {
		this.game = game;
		this.map = game.getMap();
		this.player = player;
		this.position = this.map.getRandomTile(5).getAddress().multiply(Tile.TILE_SIZE).add(Tile.TILE_SIZE / 2);
		this.init();
	}

	private void init() {
		
		// set speed to be one tile
		velocity = BasicVector.fromArray(new double[] { 0d, -1d });
		velocity = velocity.multiply(Tile.TILE_SIZE / velocity.euclideanNorm());

		// randomize direction
		int rotations = SnakeGame.R.nextInt(4);
		for (int n = 0; n < rotations; n++) {
			this.velocity = velocity.multiply(ROT_MATRIX_CW_90);
		}

		foodTile = map.getRandomTile();
	}

	public synchronized void update() {
		if (gameOver) {
			return;
		}

		energy--;
		if (energy == 0) {
			gameOver(CauseOfDeath.STARVATION);
			return;
		}

		// recalculate velocity
		if (player.isTurnLeft()) {
			velocity = velocity.multiply(ROT_MATRIX_CCW_90);
		} else if (player.isTurnRight()) {
			velocity = velocity.multiply(ROT_MATRIX_CW_90);
		}

		this.position = position.add(velocity);
		headTile = map.getTileUnderPosition(position);

		exploredTiles.add(headTile);

		player.setExploration((double) exploredTiles.size() / map.getFloorTileCount());

		if (headTile.equals(foodTile)) {
			foodTileFound();
		}

		tail.add(headTile);

		List<Tile> headInTailTiles = tail.stream().filter(t -> t.equals(headTile)).collect(Collectors.toList());
		if (headInTailTiles.size() > 1) {
			gameOver(CauseOfDeath.SUICIDE);
			return;
		}

		this.collisionCheck();
		if (gameOver) {
			return;
		}

		snakeVision.updateObservations();
		player.setInputs(snakeVision.getObservations().stream().mapToDouble(d -> d).toArray());
		player.setSteps(player.getSteps() + 1);	
	}



	private void foodTileFound() {
		bodyLength += 1;
//		energy += size*energySizeMultiplzer;
		energy += 100;
		foodTile = map.getRandomTile();

		player.setScore(player.getScore() + 1);
//		System.out.println("found food. score: " + player.getScore());

		// extent snake length to new size
		grow(bodyLength);
	}

	private void grow(int newSize) {
		EvictingQueue<Tile> newTail = EvictingQueue.create(newSize);
		newTail.addAll(tail);
		tail = newTail;
	}

	private void gameOver(CauseOfDeath cod) {
		causeOfDeath = cod;
		gameOver = true;
		player.setLives(player.getLives()-1);
	}



	public synchronized void render(Graphics2D g) {

		if (gameOver) {
			return;
		}

		g.setColor(snakeColor);
		renderFood(g);

		// draw snake tail
		tail.stream().skip(0).forEach(t -> {
			g.setColor(snakeColor);
			t.fillTile(g);

			g.setColor(Color.white);
			t.drawTileBorder(g);
		});

		// highlight the snake head
		Tile tile = map.getTileUnderPosition(getPosition());
		g.setColor(Color.GREEN);
		tile.drawTileBorder(g);
		
		snakeVision.render(g);

//		g.setColor(Color.RED);
//		viewportIntersections.forEach(v -> {
//			g.fillRect((int) (v.get(0) - 2), (int) (v.get(1) - 2), 4, 4);
//			g.drawLine((int)position.get(0), (int)position.get(1), (int)v.get(0), (int)v.get(1));
//		});
	}

	private void renderFood(Graphics2D g) {
		foodTile.fillTile(g);
	}

	
	private void collisionCheck() {
		Tile tile = map.getTileUnderPosition(position);
		if (tile.getValue() == 1) {
			gameOver(CauseOfDeath.COLLISION);
		}
	}
}
