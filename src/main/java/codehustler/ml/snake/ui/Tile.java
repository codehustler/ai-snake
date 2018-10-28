package codehustler.ml.snake.ui;

import java.util.ArrayList;
import java.util.List;

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
	

	public boolean isWall() {
		return value == 1;
	}
	
}

