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
		game.getSnakes().forEach(m -> m.render(g));
	}

	private void renderMap(Graphics2D g) {
		game.getMap().render(g);
	}
	
	private void renderStats(Graphics2D g) {
		List<Snake> snakes = new ArrayList<>(game.getSnakes());
		int left = game.getMap().getTiles().size() * Tile.TILE_SIZE + 10;
		
		AtomicInteger x = new AtomicInteger(left);
		AtomicInteger y = new AtomicInteger(15);
		
		int colWidth = 100;
		int rowHeight = 15;
		int snakeCount = snakes.size();
		int m = 15; //margin
		
		g.setColor(Color.WHITE);
		g.drawLine(x.get(), y.get()+3, x.get()+5*colWidth, y.get()+3);
		
		g.drawString("Score", x.get(), y.get());
		
		g.drawString("Steps", x.addAndGet(colWidth), y.get());
		g.drawLine(x.get()-m, y.get()-rowHeight, x.get()-m, y.get()+snakeCount*rowHeight);
		
		g.drawString("Exploration", x.addAndGet(colWidth), y.get());
		g.drawLine(x.get()-m, y.get()-rowHeight, x.get()-m, y.get()+snakeCount*rowHeight);
		
		g.drawString("Energy", x.addAndGet(colWidth), y.get());
		g.drawLine(x.get()-m, y.get()-rowHeight, x.get()-m, y.get()+snakeCount*rowHeight);
		
		g.drawString("CoD", x.addAndGet(colWidth), y.get());
		g.drawLine(x.get()-m, y.get()-rowHeight, x.get()-m, y.get()+snakeCount*rowHeight);
		
		y.addAndGet(2);
		snakes.forEach(s->{
			if ( s.isGameOver() ) {
				g.setColor(Color.GRAY);
			} else {
				g.setColor(s.getSnakeColor());
			}
			x.set(left);
			y.addAndGet(rowHeight);
			
			g.drawString(""+s.getPlayer().getScore(), x.get(), y.get());
			g.drawString(""+s.getPlayer().getSteps(), x.addAndGet(colWidth), y.get());
			g.drawString(""+(int)(s.getPlayer().getExploration()*100), x.addAndGet(colWidth), y.get());
			g.drawString(""+s.getEnergy(), x.addAndGet(colWidth), y.get());
			g.drawString(""+(s.getCauseOfDeath() != null ? s.getCauseOfDeath() : ""), x.addAndGet(colWidth), y.get());
		});
		
		
		
	}

	private void clear(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
}
