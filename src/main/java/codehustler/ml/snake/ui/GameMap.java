package codehustler.ml.snake.ui;

import static codehustler.ml.snake.util.NeedfulThings.vector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.la4j.Vector;

import codehustler.ml.snake.util.MapOptimizer;
import lombok.Data;

@Data
public class GameMap {
	private List<List<Tile>> tiles = new ArrayList<>();
	private List<Tile> wallTiles = new ArrayList<>();
	private Set<Wall> walls = new HashSet<>();

	private final SnakeGame game;
	
	private int floorTileCount; 
	
	public GameMap(int size, SnakeGame game) throws Exception {
		this.game = game;
		loadMap(size);
	}

	public Tile getRandomTile() {
		return getRandomTile(1);
	}
	
	public Tile getRandomTile(int minWallDistance) {
		Tile tile;
//		do {
			int y = minWallDistance + SnakeGame.R.nextInt(tiles.size()-(2*minWallDistance));
			int x = minWallDistance + SnakeGame.R.nextInt(tiles.get(y).size()-2*(minWallDistance));
			tile = tiles.get(y).get(x);
//		} while (tile.getValue() == 1);

		return tile;
	}

	public void render(Graphics2D g) {
//		tiles.stream().flatMap(List::stream).forEach(t->t.render(g));
		renderBackground(g);
		renderWalls(g);
	}

	private void renderBackground(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, tiles.get(0).size() * Tile.TILE_SIZE, tiles.size() * Tile.TILE_SIZE);
	}

	
	private void renderWalls(Graphics2D g) {
//		System.out.println("wall count: " + walls.size());
		g.setColor(Color.BLACK);
		walls.forEach(v -> {
			g.fillRect((int) v.getA().get(0), (int) v.getA().get(1), (int) v.getSize().get(0),
					(int) v.getSize().get(1));
		});
//		g.setColor(Color.RED);
//		walls.forEach(v -> {
//			g.drawRect((int)v.getA().get(0), (int)v.getA().get(1), (int)v.getSize().get(0), (int)v.getSize().get(1));
//		});
	}

	private void loadMap(int size) throws IOException {
//		AtomicInteger x = new AtomicInteger();
//		AtomicInteger y = new AtomicInteger();

		for ( int y = 0; y < size; y++ ) { 
			List<Tile> tileRow = new ArrayList<>();
			tiles.add(tileRow);
			for ( int x = 0; x < size; x++ ) {
				Tile t;
				if ( y == 0 || x == 0 || y == size-1 || x == size-1 ) {
					t = new Tile(vector(x, y), 1, game);
					wallTiles.add(t);
				} else {
					t = new Tile(vector(x, y), 0, game);
					floorTileCount++;
				}
				tileRow.add(t);
			}	
		}
		
//		Files.lines(mapPath).forEach(l -> {
//			x.set(0);
//			List<Tile> tileRow = new ArrayList<>();
//			tiles.add(tileRow);
//
//			l.chars()//
//					.map(c -> Integer.valueOf(new String(new char[] { (char) c })))//
//					.forEach(i -> {
//						if (i == 9) {
//							startPosition = BasicVector.fromArray(new double[] { x.get(), y.get() });
//							tileRow.add(new Tile(vector(x.get(), y.get()), 0, game));
//						} else if (i == 2) {// START/FINISH
//							Tile t = new Tile(vector(x.get(), y.get()), 0, game);
//							t.setProperty("START_FINISH", true);
//							tileRow.add(t);
//						} else if (i == 3) { // CHECKPOINTS
//							tileRow.add(new Tile(vector(x.get(), y.get()), 0, game));
//						} else {
//							Tile t = new Tile(vector(x.get(), y.get()), i, game);
//							if (i == 1) {
//								wallTiles.add(t);
//							}
//							tileRow.add(t);
//						}
//						x.incrementAndGet();
//					});
//			y.incrementAndGet();
//		});
		
		this.walls = MapOptimizer.optimizeMap(tiles, wallTiles);
	}

	public Tile getTileByAddress(Vector address) {
		
		int x = (int)address.get(0);
		int y = (int)address.get(1);
		int size = tiles.size();
		
		x = Math.min(Math.max(x, 0), size-1);
		y = Math.min(Math.max(y, 0), size-1);
		
		return tiles.get(y).get(x);
	}

	public Tile getTileUnderPosition(Vector position) {
		Vector address = positionToAddress(position);		
		return getTileByAddress(address);
	}

	private Vector positionToAddress(Vector position) {
		Vector tilePosition = position.divide(Tile.TILE_SIZE);
		tilePosition.update((i, v) -> ((int) v));
		return tilePosition;
	}

	public void clearVisitorCache(Snake snake) {
		tiles.parallelStream().flatMap(List::stream).parallel().forEach(t -> t.clearVisitorCache(snake));
	}

	public void clearVisitorCache() {
		tiles.parallelStream().flatMap(List::stream).parallel().forEach(Tile::clearVisitorCache);
	}

}
