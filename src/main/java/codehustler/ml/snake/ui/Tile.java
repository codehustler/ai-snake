package codehustler.ml.snake.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.la4j.Vector;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * 4 Vectors
 * 
 * a ------ b
 * |		|
 * |		|
 * |		|
 * d--------c
 *  
  */
@Data 
@ToString(of= {"address", "value"}) 
@EqualsAndHashCode(of="address")
public class Tile {

	public static final int TILE_SIZE = 10;
	
	private Vector address;
	private Vector a, b, c, d, center;
	private Vector edgeAB, edgeBC, edgeCD, edgeDA;
	private List<Vector> edges = new ArrayList<>();
	private int value;
	private Properties properties = new Properties();

	private Map<Snake, Long> visitors = Collections.synchronizedMap(new HashMap<>());
	
	private final SnakeGame game;
	
	public Tile(Vector address, int value, SnakeGame game) {
		this.game = game;
		this.init(address, value);
	}
	
	private void init(Vector address, int value) {
		this.value = value;
		this.address = address;
		this.a = vector(address.get(0)*TILE_SIZE, address.get(1)*TILE_SIZE);
		this.b = vector(address.get(0)*TILE_SIZE+TILE_SIZE, address.get(1)*TILE_SIZE);
		this.c = vector(address.get(0)*TILE_SIZE+TILE_SIZE, address.get(1)*TILE_SIZE+TILE_SIZE);
		this.d = vector(address.get(0)*TILE_SIZE, address.get(1)*TILE_SIZE+TILE_SIZE);
		this.center = a.add(TILE_SIZE/2);
		
		this.edgeAB = vector(a.get(0), a.get(1), b.get(0), b.get(1));
		this.edgeBC = vector(b.get(0), b.get(1), c.get(0), c.get(1));
		this.edgeCD = vector(c.get(0), c.get(1), d.get(0), d.get(1));
		this.edgeDA = vector(d.get(0), d.get(1), a.get(0), a.get(1));
		
		this.edges.add(edgeAB);
		this.edges.add(edgeBC);
		this.edges.add(edgeCD);
		this.edges.add(edgeDA);
	}
	
	private static Vector vector(double... d) {
		return Vector.fromArray(d);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key, Class<T> type, T defaultValue) {
		return (T) properties.getOrDefault(key, defaultValue);
	}
	
	public void render(Graphics2D g) {
		cleanVisitorMap();
		if (getProperty("START_FINISH", Boolean.class, false)) {
			g.setColor(Color.WHITE); 
		} else if ( value == 0 ) {
			g.setColor(Color.LIGHT_GRAY);
		} else g.setColor(Color.BLACK);
		
		g.fillRect((int)address.get(0)*TILE_SIZE, (int)address.get(1)*TILE_SIZE, TILE_SIZE , TILE_SIZE );
	
		//g.setColor(Color.LIGHT_GRAY);
//		g.drawRect((int)address.get(0)*TILE_SIZE, (int)address.get(1)*TILE_SIZE, TILE_SIZE , TILE_SIZE );
		
//		edges.forEach(e->drawLine(e, g));
		
//		g.setColor(Color.RED);
//		renderBreadcrumbTrails(g);
	}
	
	private void cleanVisitorMap() {
		long maxAge = 20_000;
		visitors.entrySet().removeIf(e -> game.getTimecode()- e.getValue() > maxAge );
	}
	
//	private void renderBreadcrumbTrails(Graphics2D g) {
////		long maxAge = 2000;
////		long currentTimecode = RunnerGame.getTimecode();
//		
//		Optional<Long> mostRecentTimecode = visitors.entrySet().stream().filter(e->!e.getKey().isGameOver()).map(e->e.getValue()).max(Comparator.naturalOrder());
////		Optional<Long> mostRecentTimecode = visitors.values().stream().max(Comparator.naturalOrder())
//		
//		if ( mostRecentTimecode.isPresent() ) {
//			//long recentAge = currentTimecode - mostRecentTimecode.get();
////			if ( recentAge < maxAge ) {
//				g.fillOval((int) address.get(0) * TILE_SIZE + TILE_SIZE/2 - 2, (int) address.get(1) * TILE_SIZE + TILE_SIZE/2 - 2, 4, 4);
////			}
//		}
//	}
	
	@SuppressWarnings("unused")
	private void drawLine(Vector edge, Graphics2D g) {
		g.drawLine((int)edge.get(0), (int)edge.get(1), (int)edge.get(2), (int)edge.get(3));
	}

	public void addOccupant(Snake mazeRunner) {

		//MazeRunner previousVisit = ma
		
		this.visitors.put(mazeRunner, game.getTimecode());
	}
	
	public Optional<Long> lastVisited(Snake runner) {
		
		return Optional.ofNullable(visitors.get(runner));
	}
	
	public void clearVisitorCache() {
		this.visitors.clear();
	}
	
	public void clearVisitorCache(Snake runner) {
		this.visitors.remove(runner);
	}

	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	
}

