package codehustler.ml.snake;

import java.awt.event.KeyEvent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @EqualsAndHashCode(of= {"keyCodeLeft", "keyCodeRight"}, callSuper = false)
public class HumanPlayer extends AbstractPlayer {

	@Getter
	private final int keyCodeLeft;
	
	@Getter
	private final int keyCodeRight;

	public HumanPlayer(char left, char right) {
		this(KeyEvent.getExtendedKeyCodeForChar(left), KeyEvent.getExtendedKeyCodeForChar(right));
	}
	

	@Override
	public void save(String name) {
		throw new UnsupportedOperationException("Cannot save human players to disk yet!");
	}

	@Override
	public void restore(String name) {
		throw new UnsupportedOperationException("Cannot load human players from disk yet!");
	}
}
