package codehustler.ml.snake.util;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.la4j.Vector;

import codehustler.ml.snake.ui.Tile;
import codehustler.ml.snake.ui.Wall;

public class MapOptimizer {
	/**
	 * combines adjacent wall tiles to wall, this drastically improves collision
	 * detection and field of view calculations
	 */
	public static Set<Wall> optimizeMap(List<List<Tile>> tiles, List<Tile> forbiddenTiles) {
		Set<Wall> walls = new HashSet<>();
		
		int maxX = tiles.get(0).size();
		int maxY = tiles.size();

		forbiddenTiles.stream().forEach(t -> {
			int x = (int) t.getAddress().get(0);
			int y = (int) t.getAddress().get(1);

			Set<Tile> wallX = new HashSet<>();
			Set<Tile> wallY = new HashSet<>();

			Tile adjTile;

			// search left/right

			for (int x1 = x; x1 >= 0; x1--) {
				adjTile = tiles.get(y).get(x1);
//				System.out.print(adjTile + ",       ");
				if (adjTile.getValue() == 1 || forbiddenTiles.contains(adjTile)) {
					wallX.add(adjTile);
				} else
					break;
			}

			for (int x1 = x; x1 < maxX; x1++) {
				adjTile = tiles.get(y).get(x1);
//				System.out.print(adjTile + ",       ");
				if (adjTile.getValue() == 1 || forbiddenTiles.contains(adjTile)) {
					wallX.add(adjTile);
				} else
					break;
			}
//			System.out.println();

			// search up/down
			for (int y1 = y; y1 >= 0; y1--) {
				adjTile = tiles.get(y1).get(x);
				if (adjTile.getValue() == 1 || forbiddenTiles.contains(adjTile)) {
					wallY.add(adjTile);
				} else
					break;
			}

			for (int y1 = y; y1 < maxY; y1++) {
				adjTile = tiles.get(y1).get(x);
				if (adjTile.getValue() == 1 || forbiddenTiles.contains(adjTile)) {
					wallY.add(adjTile);
				} else
					break;
			}

			// System.out.println(wallX.size());
			if (wallX.size() > 1) {
				walls.add(compressWall(wallX));
			}

			if (wallY.size() > 1) {
				walls.add(compressWall(wallY));
			}

			if (wallX.size() == 1 && wallY.size() == 1) {
				walls.add(compressWall(wallX));
			}
		});
		
		
		return walls;
	}

	private static Wall compressWall(Set<Tile> wall) {
		double minX = wall.stream().min(Comparator.comparing(Tile::getA, (a, b) -> Double.compare(a.get(0), b.get(0))))
				.get().getA().get(0);
		double minY = wall.stream().min(Comparator.comparing(Tile::getA, (a, b) -> Double.compare(a.get(1), b.get(1))))
				.get().getA().get(1);

		double maxX = wall.stream().max(Comparator.comparing(Tile::getC, (a, b) -> Double.compare(a.get(0), b.get(0))))
				.get().getC().get(0);
		double maxY = wall.stream().max(Comparator.comparing(Tile::getC, (a, b) -> Double.compare(a.get(1), b.get(1))))
				.get().getC().get(1);

		return new Wall(Vector.fromArray(new double[] { minX, minY, maxX, maxY }));
	}
}
