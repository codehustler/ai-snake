package codehustler.ml.snake.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;

public class GameScreen extends JPanel {

	private static final long serialVersionUID = 1L;

	private final SnakeGame game;

	public GameScreen(SnakeGame game) throws Exception {
		this.game = game;
		this.setBackground(Color.BLACK);
	}

	@Override
	public void paint(Graphics g_) {
		Graphics2D g = (Graphics2D) g_;
		clear(g);
		renderMap(g);
		renderRunners(g);
		renderStats(g);
	}

	private void renderRunners(Graphics2D g) {
		game.getRunners().forEach(m -> m.render(g));
	}

	private void renderMap(Graphics2D g) {
		game.getMap().render(g);
	}
	
	private void renderStats(Graphics2D g) {
		g.setColor(Color.BLACK);
		int x = game.getMap().getTiles().size() * Tile.TILE_SIZE + 10;
		
		AtomicInteger y = new AtomicInteger(15);
		
		List<Snake> snakes = new ArrayList<>(game.getRunners());
		for ( int n = 0; n < snakes.size(); n++ ) {
			if ( n % 10 == 0 ) {
				x += 140;
				y.set(15);
			}
			
			Snake s = snakes.get(n);
			if ( s.isGameOver() ) {
				g.setColor(Color.GRAY);
			} else {
				g.setColor(s.getSnakeColor());
			}
			
			g.drawString("Exploration: " + (int)(s.getPlayer().getExploration()*100), x, y.getAndAdd(20));
			g.drawString("Score          : " + s.getPlayer().getScore() + "_" + s.getPlayer().getSteps(), x, y.getAndAdd(20));
			g.drawString("Energy         : " + s.getEnergy(), x, y.getAndAdd(20));
			
		}
		
	}

	private void clear(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
}
