package codehustler.ml.snake.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.la4j.Vector;

import codehustler.ml.snake.GameOptions;
import codehustler.ml.snake.util.Algebra;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TileGridVision implements SnakeVision {

	private final Snake snake;
	
	private Map<Integer, List<Pair<Tile, Double>>> observedTiles = new HashMap<>();
	
	@Getter
	private List<Double> observations = new ArrayList<>();
	
	private double wallValue = 0.02;
	private double selfValue = 0.01;
	private double neutralValue = 50;
	private double gotoValue = 100;
	
	@Override
	public int getInputSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void updateObservations() {
		observedTiles.clear();
		observations.clear();

		/* 
		 * 3: 8  | 4*2
		 * 5: 16 | 4*4
		 * 7: 24 | 4*6
		 * 9: 32 | 4*8
		 */
		
		// place pos at 0,0 of first shell
		Vector dir = snake.getVelocity().copy();
		Vector pos = snake.getPosition().add(dir); // 1,0
		
		dir = dir.multiply(Algebra.ROT_MATRIX_CCW_90);
		pos = pos.add(dir); // 0,0
		
		// rotate dir to look CW
		dir = dir.multiply(Algebra.ROT_MATRIX_180);
		
		

		
		Map<Integer, Tile> shortestPath = new HashMap<>();
//		Map<Tile, Double> tileObservationValues = new HashMap<>();
//		Map<Integer, Tile> indexed = new HashMap<>();
		
		
		
		//collect the viewport tiles
		for ( int radius = 3; radius <= 3; radius += 2) {
			double minFoodDistance = Double.MAX_VALUE;

			for ( int n = 1; n <= 4*(radius-1); n++ ) {
				pos = pos.add(dir);
				if ( n % (radius-1) == 0 ) {
					dir = dir.multiply(Algebra.ROT_MATRIX_CW_90);		
				}				
				Tile t = snake.getMap().getTileUnderPosition(pos);
				observedTiles.computeIfAbsent(radius, ArrayList::new).add(new MutablePair<>(t, 0d));
				
				double tileToFoodDistance = snake.getFoodTile().getCenter().subtract(t.getCenter()).euclideanNorm();
				if ( tileToFoodDistance < minFoodDistance ) {
					shortestPath.put(radius, t);
					minFoodDistance = tileToFoodDistance;					
				}
			}
			dir = dir.multiply(Algebra.ROT_MATRIX_180);
			pos = pos.add(dir); // one "left"
			dir = dir.multiply(Algebra.ROT_MATRIX_CW_90);
			pos = pos.add(dir); // one "up"
			dir = dir.multiply(Algebra.ROT_MATRIX_CW_90); // "look" "right"
		}
		
		//Optional<Tile> nogoTile = shortestPath.values().stream().filter(t-> t.isWall() || tail.contains(t)).findFirst();
		
		observedTiles.keySet().stream().sorted().flatMap(n->observedTiles.get(n).stream()).forEach(p->{
			Tile t = p.getLeft();
			
			
			double tileValue = t.isWall() ? wallValue : snake.getTail().contains(t) ? selfValue : shortestPath.containsValue(t) ? gotoValue : neutralValue;
			
//			if ( /*!nogoTile.isPresent() &&*/ shortestPath.containsValue(t) && tileValue >= neutralValue ) {
//				tileValue = gotoValue;
//			}
			
			p.setValue(tileValue);
			observations.add(tileValue);
		});
	}

	@Override
	public void render(Graphics2D g) {
		
		if ( GameOptions.isRenderFieldOfView() ) {
			observedTiles.values().stream().flatMap(List::stream).forEach(p->{			
				Tile t = p.getLeft(); 
				Double value = p.getValue();
				
				g.setColor(Color.GREEN);
				t.drawTileBorder(g);
				if (value == selfValue ) {
					g.setColor(Color.BLACK);
					t.fillTile(g);
				} else if ( value == wallValue ) {
					g.setColor(Color.RED);
					t.fillTile(g);
				} else if ( value == gotoValue ) {
					g.setColor(Color.MAGENTA);
					t.fillTile(g);
				}
				
			});
		}
	}
}
