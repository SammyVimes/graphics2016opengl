package graphics2016;

import graphics2016.camera.HudCamera;

/**
 * A scene for the hud (Heads-Up-Display) with a HudCamera and a transparent background
 */
public class HudScene extends Scene {

	public HudScene() {
		super(new HudCamera());
		backgroundColor = null;
	}

}
