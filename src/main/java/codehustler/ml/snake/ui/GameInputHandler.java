package codehustler.ml.snake.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import codehustler.ml.snake.HumanPlayer;
import codehustler.ml.snake.Player;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class GameInputHandler implements KeyListener {
	
	@NonNull
	private SnakeGame game;
	
	private Set<HumanPlayer> humanPlayers = new HashSet<>();
	
	public void addHumanPlayer(Player humanPlayer) {
		humanPlayers.remove(humanPlayer);
		humanPlayers.add((HumanPlayer) humanPlayer);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {		
		humanPlayers.forEach(p -> {					
			if ( e.getKeyCode() == p.getKeyCodeLeft() ) {
				p.setTurnLeft(true);
			} else if ( e.getKeyCode() == p.getKeyCodeRight()) {
				p.setTurnRight(true);
			}
		});
	}

	@Override
	public void keyReleased(KeyEvent e) {
		humanPlayers.forEach(p -> {			
			if ( e.getKeyCode() == p.getKeyCodeLeft() ) {
				p.setTurnLeft(false);
			} else if ( e.getKeyCode() == p.getKeyCodeRight()) {
				p.setTurnRight(false);
			}
		});
	}

	@Override
	public void keyTyped(KeyEvent e) {
//		System.out.println("keyTyped: " + e.getKeyChar());
		if ( e.getKeyChar() == '\n' ) {
			game.toggleAnimationSpeed();
		} else if ( e.getKeyChar() == 'k' ) {
			game.killCurrentPopulation();
		} else if ( e.getKeyChar() == 'r' ) {
			game.toggleRender();
		} else if ( e.getKeyChar() == 'v' ) {
			game.toggleRenderFieldOfView();
		} 
	}
}
