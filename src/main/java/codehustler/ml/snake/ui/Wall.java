package codehustler.ml.snake.ui;

import java.util.ArrayList;
import java.util.List;

import org.la4j.Vector;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of="box")
public class Wall {
	
	private Vector box;
	private Vector size;
	private Vector a, b, c, d;
	private Vector edgeAB, edgeBC, edgeCD, edgeDA;
	private List<Vector> edges = new ArrayList<>();
	
	public Wall(Vector box) {
		this.box = box;
		this.init();
	}
	
	private void init() {
		this.size = vector(box.get(2)-box.get(0), box.get(3)-box.get(1));
		
		this.a = vector(box.get(0), box.get(1));
		this.b = vector(box.get(0)+size.get(0), box.get(1));
		this.c = vector(box.get(0)+size.get(0), box.get(1)+size.get(1));
		this.d = vector(box.get(0), box.get(1)+size.get(1));
		
		this.edgeAB = vector(a.get(0), a.get(1), b.get(0), b.get(1));
		this.edgeBC = vector(b.get(0), b.get(1), c.get(0), c.get(1));
		this.edgeCD = vector(c.get(0), c.get(1), d.get(0), d.get(1));
		this.edgeDA = vector(d.get(0), d.get(1), a.get(0), a.get(1));
		
		this.edges.add(edgeAB);
		this.edges.add(edgeBC);
		this.edges.add(edgeCD);
		this.edges.add(edgeDA);
	}
	
	private Vector vector(double... d) {
		return Vector.fromArray(d);
	}
	
}
