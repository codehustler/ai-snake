package codehustler.ml.snake;

import lombok.Getter;

public class GameOptions {

	@Getter
	private static boolean renderFieldOfView = false;
	
	@Getter
	private static boolean rendergame = true;
	
	public static void toggleRenderFieldOfView() {
		renderFieldOfView = !renderFieldOfView;
	}
	
	public static void toggleRender() {
		rendergame = !rendergame;
	}
}
