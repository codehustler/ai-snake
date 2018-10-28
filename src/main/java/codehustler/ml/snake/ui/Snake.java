package codehustler.ml.snake.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

import com.google.common.collect.EvictingQueue;

import codehustler.ml.snake.Player;
import codehustler.ml.snake.ai.AIPlayer;
import codehustler.ml.snake.util.MapOptimizer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "player")
public strictfp class Snake {

	public static int[] FIELD_OF_VIEW_ANGLES = new int[] { 45, 90, 135 };
	public static final int runnerRadius = 10;

	private static final Matrix rotationMatrixCW = Matrix.zero(2, 2);
	private static final Matrix rotationMatrixCCW = Matrix.zero(2, 2);

	private static final Matrix rotationMatrixCW_45 = Matrix.zero(2, 2);
	private static final Matrix rotationMatrix_180 = Matrix.zero(2, 2);

	private Vector position = BasicVector.fromArray(new double[] { 0d, 0d });
	private Vector velocity;

	private GameMap map;

	private int size = 20;
	private EvictingQueue<Tile> tail = EvictingQueue.create(size);

	private Player player;

	private final SnakeGame game;

	private List<Vector> fieldOfView = new ArrayList<>();
	private Set<Vector> viewportIntersections = Collections.synchronizedSet(new HashSet<>());

	boolean gameOver = false;

	private double[] observations;

	private Tile occupiedTile;

	private Vector foodPosition = null;

	private final int energySizeMultiplzer = 10;
	private int energy = energySizeMultiplzer * size;
//	private int energy = 100;

	private Tile foodTile;

	private Color snakeColor = new Color(SnakeGame.R.nextInt(256), SnakeGame.R.nextInt(256), SnakeGame.R.nextInt(256));

	private double foodDistance = -1;

	private boolean lastMoveGood;

	private List<Tile> observedTiles = new ArrayList<>();

	private Set<Tile> exploredTiles = new HashSet<>();

	public Snake(Player player, SnakeGame game) {
		this.game = game;
		this.map = game.getMap();
		this.player = player;
		this.position = this.map.getRandomTile(5).getAddress().multiply(Tile.TILE_SIZE).add(Tile.TILE_SIZE / 2);
		this.init();
//		System.out.println("energy: " + energy);
	}

	private void init() {
		double angleDeg = 90;
		double angle = Math.toRadians(-angleDeg);

		rotationMatrixCW.set(0, 0, Math.cos(angle));
		rotationMatrixCW.set(1, 0, Math.sin(angle));
		rotationMatrixCW.set(0, 1, -Math.sin(angle));
		rotationMatrixCW.set(1, 1, Math.cos(angle));

		angle = Math.toRadians(angleDeg);
		rotationMatrixCCW.set(0, 0, Math.cos(angle));
		rotationMatrixCCW.set(1, 0, Math.sin(angle));
		rotationMatrixCCW.set(0, 1, -Math.sin(angle));
		rotationMatrixCCW.set(1, 1, Math.cos(angle));

		angle = Math.toRadians(-45);
		rotationMatrixCW_45.set(0, 0, Math.cos(angle));
		rotationMatrixCW_45.set(1, 0, Math.sin(angle));
		rotationMatrixCW_45.set(0, 1, -Math.sin(angle));
		rotationMatrixCW_45.set(1, 1, Math.cos(angle));

		angle = Math.toRadians(180);
		rotationMatrix_180.set(0, 0, Math.cos(angle));
		rotationMatrix_180.set(1, 0, Math.sin(angle));
		rotationMatrix_180.set(0, 1, -Math.sin(angle));
		rotationMatrix_180.set(1, 1, Math.cos(angle));
		
		// set speed to be one tile
		velocity = BasicVector.fromArray(new double[] { 0d, -1d });
		velocity = velocity.multiply(Tile.TILE_SIZE / velocity.euclideanNorm());

		// randomize direction
		int rotations = SnakeGame.R.nextInt(4);
		for (int n = 0; n < rotations; n++) {
			this.velocity = velocity.multiply(rotationMatrixCW);
		}

		foodTile = map.getRandomTile();
		foodDistance = foodTile.getCenter().subtract(position).euclideanNorm();

//		updateFieldOfView();
		observations = new double[24 + 3];
//		updateObservedTiles();
//		System.out.println(observations.length);
	}

//	private void updateFieldOfView() {
//		// field of view
//		fieldOfView.clear();
//		
//		Vector front = velocity.multiply(Tile.TILE_SIZE / velocity.euclideanNorm()); // was 1000
////		Vector back = rotateVector(front, 180);
//		fieldOfView.add(front);
////		fieldOfView.add(back);
//		IntStream.of(FIELD_OF_VIEW_ANGLES).forEach(n ->{
//			fieldOfView.add(rotateVector(front, n));
//			fieldOfView.add(rotateVector(front, -n));
//		});
//	}

	public synchronized void update() {
		if (gameOver) {
			return;
		}

//		lifetime ++;		
//		if ( lifetime % 100 == 0 ) {
//			grow(size+4);
//		}

		energy--;
		if (energy == 0) {
			gameOver();
			return;
		}

		// recalculate velocity
		if (player.isTurnLeft()) {
			velocity = velocity.multiply(rotationMatrixCCW);
		} else if (player.isTurnRight()) {
			velocity = velocity.multiply(rotationMatrixCW);
		}

		this.position = position.add(velocity);
		occupiedTile = map.getTileUnderPosition(position);

		exploredTiles.add(occupiedTile);

		player.setExploration((double) exploredTiles.size() / map.getFloorTileCount());

//		double newFoodDistance = foodTile.getCenter().subtract(position).euclideanNorm();
//		if ( newFoodDistance < foodDistance ) {
//			//good!
//			lastMoveGood = true;
//			player.givePositiveFeedBack();
//		} else {
//			lastMoveGood = false;
//			player.giveNegativeFeedBack();
//		}
//		foodDistance = newFoodDistance;

		if (occupiedTile.equals(foodTile)) {
			foodTileFound();
		}

		tail.add(occupiedTile);

		List<Tile> headInTailTiles = tail.stream().filter(t -> t.equals(occupiedTile)).collect(Collectors.toList());
		if (headInTailTiles.size() > 1) {
			gameOver();
			return;
		}

		this.collisionCheck();
		if (gameOver) {
			return;
		}

//		this.updateFieldOfView();
//		this.calculateViewport();
		this.updateObservedTiles();

		player.setSteps(player.getSteps() + 1);

//		Vector delta = foodTile.getAddress().subtract(occupiedTile.getAddress());
//		System.out.println(delta);
//		delta.update((i,v)-> 0.5+1/(2*v));

		double[] distances = new double[3];

		distances[0] = foodTile.getCenter().subtract(position.add(velocity.multiply(rotationMatrixCCW)))
				.euclideanNorm();
		distances[1] = foodTile.getCenter().subtract(position.add(velocity)).euclideanNorm();
		distances[2] = foodTile.getCenter().subtract(position.add(velocity.multiply(rotationMatrixCW))).euclideanNorm();
		distances = AIPlayer.maxNormalize(distances);

		observations[observations.length-1] = distances[0] > Math.max(distances[1], distances[2]) ? 10 : 0;
		observations[observations.length-2] = distances[1] > Math.max(distances[0], distances[2]) ? 10 : 0;
		observations[observations.length-3] = distances[2] > Math.max(distances[0], distances[1]) ? 10 : 0;

		occupiedTile.addOccupant(this);

		player.setInputs(observations);
	}

	private void updateObservedTiles() {
		
		observedTiles.clear();

		/* 
		 * 3: 8  | 4*2
		 * 5: 16 | 4*4
		 * 7: 24 | 4*6
		 * 9: 32 | 4*8
		 */
		
		// place pos at 0,0 of first shell
		Vector dir = velocity.copy();
		Vector pos = position.add(dir); // 1,0
		dir = dir.multiply(rotationMatrixCCW);
		pos = pos.add(dir); // 0,0
		
		// rotate dir to look CW
		dir = dir.multiply(rotationMatrix_180);
		
		int distance = 1;
		int tileIndex = 0;
		for ( int size = 3; size <= 5; size += 2) {
			for ( int n = 1; n <= 4*(size-1); n++ ) {
				pos = pos.add(dir);
				if ( n % (size-1) == 0 ) {
					dir = dir.multiply(rotationMatrixCW);		
				}
				observedTiles.add(map.getTileUnderPosition(pos));
				Tile t = map.getTileUnderPosition(pos);
				double value = 1/distance;
				observations[tileIndex] = t.getValue() == 1 ? value : tail.contains(t) ? 1 : value;
				tileIndex++;
			}
			dir = dir.multiply(rotationMatrix_180);
			pos = pos.add(dir); // one "left"
			dir = dir.multiply(rotationMatrixCW);
			pos = pos.add(dir); // one "up"
			dir = dir.multiply(rotationMatrixCW); // "look" "right"
			distance++;
		}
		
		//System.out.println(observedTiles.size());
	}

	private void foodTileFound() {
		size += 2;
//		energy += size*energySizeMultiplzer;
		energy += 100;
		foodTile = map.getRandomTile();
		this.foodPosition = null;

		player.setScore(player.getScore() + 1);
//		System.out.println("found food. score: " + player.getScore());

		// extent snake length to new size
		grow(size);
	}

	private void grow(int newSize) {
		this.size = newSize;

		EvictingQueue<Tile> newTail = EvictingQueue.create(size);
		newTail.addAll(tail);
		tail = newTail;
	}

	private void gameOver() {
		this.gameOver = true;
	}

//	private void calculateViewport() {
////		AtomicInteger index = new AtomicInteger();
////		viewportIntersections.clear();
////		fieldOfView.forEach(v->{
////			nearestIntersection(combine(position, position.add(v)), index.getAndIncrement()).ifPresent(viewportIntersections::add);	
////		});
//		
////		leftTile = map.getTileUnderPosition(position.add(velocity.multiply(rotationMatrixCCW)));
////		frontTile = map.getTileUnderPosition(position.add(velocity));
////		rightTile = map.getTileUnderPosition(position.add(velocity.multiply(rotationMatrixCW)));
//	}

//	private Vector rotateVector(Vector v, double angleDeg) {
//		double angle = Math.toRadians(-angleDeg);
//
//		Matrix rotationMatrix = Matrix.zero(2, 2);
//		rotationMatrix.set(0, 0, Math.cos(angle));
//		rotationMatrix.set(1, 0, Math.sin(angle));
//		rotationMatrix.set(0, 1, -Math.sin(angle));
//		rotationMatrix.set(1, 1, Math.cos(angle));
//
//		return v.multiply(rotationMatrix);
//	}

	public synchronized void render(Graphics2D g) {

		if (gameOver) {
			return;
		}

		g.setColor(snakeColor);
		renderFood(g);

		// draw snake tail
		tail.stream().skip(0).forEach(t -> {
			g.setColor(snakeColor);
			fillTile(t, g);

			g.setColor(Color.white);
			drawTileBorder(t, g);
		});

		// highlight the snake head
		Tile tile = map.getTileUnderPosition(getPosition());
		g.setColor(Color.GREEN);
		drawTileBorder(tile, g);

//		//render food vector
//		if ( foodPosition != null ) {
//			g.setColor(Color.YELLOW);
//			NeedfulThings.drawVector(position, foodPosition.subtract(position), 1, g);
//		}

		// draw field of view
//		g.setColor(Color.MAGENTA);
//		fieldOfView.forEach(v->{
//			drawVector(position, v, 1, g);	
//		});

//		g.setColor(Color.RED);
//		viewportIntersections.forEach(v -> {
//			g.fillRect((int) (v.get(0) - 2), (int) (v.get(1) - 2), 4, 4);
//			g.drawLine((int)position.get(0), (int)position.get(1), (int)v.get(0), (int)v.get(1));
//		});

//		observedTiles.stream().forEach(t->{			
//			int x = t.getValue() == 1 ? 1 : tail.contains(t) ? 1 : 0;
//			if ( x == 0 ) {
//				drawTileBorder(t, g);
//			} else {
//				fillTile(t, g);
//			}
//		});
	}

	private void renderFood(Graphics2D g) {
		fillTile(foodTile, g);
	}

	private void fillTile(Tile tile, Graphics2D g) {
		g.fillRect((int) tile.getAddress().get(0) * Tile.TILE_SIZE, (int) tile.getAddress().get(1) * Tile.TILE_SIZE,
				Tile.TILE_SIZE, Tile.TILE_SIZE);
	}

	private void drawTileBorder(Tile tile, Graphics2D g) {
		g.drawRect((int) tile.getAddress().get(0) * Tile.TILE_SIZE, (int) tile.getAddress().get(1) * Tile.TILE_SIZE,
				Tile.TILE_SIZE, Tile.TILE_SIZE);
	}

	private Optional<Vector> nearestIntersection(Vector edge, int observationIndex) {
		AtomicReference<Vector> nearest = new AtomicReference<>();
		AtomicReference<Double> nearestDistance = new AtomicReference<>();

		List<Tile> forbiddenTiles = new ArrayList<>(tail);
		forbiddenTiles.remove(occupiedTile);
		if (forbiddenTiles.size() > 2) {
			forbiddenTiles = forbiddenTiles.subList(2, forbiddenTiles.size() - 1);
		}

		Set<Wall> optimizedMap = MapOptimizer.optimizeMap(map.getTiles(), forbiddenTiles);
		optimizedMap.addAll(map.getWalls());

		// add the foofTile as a wall
//		Wall foodWall = new Wall(Vector.fromArray(new double[] { foodTile.getA().get(0), foodTile.getA().get(1), foodTile.getC().get(0), foodTile.getC().get(1) }));
//		optimizedMap.add(foodWall);

		optimizedMap.stream().flatMap(t -> t.getEdges().stream()).forEach(tileEdge -> {
			Optional<Vector> intersection = lineIntersect(edge, tileEdge);
			intersection.ifPresent(v -> {
				double distance = position.subtract(v).euclideanNorm();

				if (nearest.get() == null || distance < nearestDistance.get()) {
					nearest.set(v);
					nearestDistance.set(distance);
					observations[observationIndex] = distance;
				}
			});
		});

//		foodWall.getEdges().forEach(wallEdge ->{
//			Optional<Vector> intersection = lineIntersect(edge, wallEdge);
//			intersection.ifPresent(v -> {
//				//food is always closer than wall !!?
//				foodPosition = foodTile.getCenter();
////				System.out.println("food found: " + foodPosition);
//				double distance = position.subtract(v).euclideanNorm();
//				nearest.set(v);
//				nearestDistance.set(distance);
//				observations[observationIndex] = 1/distance;						
//			});
//		});

		return Optional.ofNullable(nearest.get());
	}

	private static Optional<Vector> lineIntersect(Vector a, Vector b) {
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

	private void collisionCheck() {
		Tile tile = map.getTileUnderPosition(position);
		if (tile.getValue() == 1) {
			gameOver();
		}
	}
}
