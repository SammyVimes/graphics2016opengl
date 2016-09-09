package graphics2016.demo;

import graphics2016.Font;
import graphics2016.HudScene;
import graphics2016.Object3d;
import graphics2016.Rect;
import graphics2016.Texture;
import graphics2016.geometry.SpriteGeometry;
import graphics2016.material.SpriteMaterial;

public class ArrowsHudScene extends HudScene {

	int width;

	Font font = new ArialFont();

	String title = "xxxxxxxxx A very long string to be replaced xxxxxxxxxx";

	Object3d titleObject3d;
	Rect fm = new Rect();

	Object3d buttonRight;
	Object3d buttonLeft;

	public ArrowsHudScene() {
		super();

		titleObject3d = font.getTextLine(title, fm);
		addChild(titleObject3d);

		SpriteMaterial buttonRightMaterial = new SpriteMaterial(new Texture("arrow_right.png"));
		SpriteGeometry buttonRightGeometry = new SpriteGeometry(1);
		buttonRightGeometry.addSprite(0, 0, 0, 0);
		buttonRight = new Object3d(buttonRightGeometry, buttonRightMaterial);
		addChild(buttonRight);

		SpriteMaterial buttonLeftMaterial = new SpriteMaterial(new Texture("arrow_left.png"));
		SpriteGeometry buttonLeftGeometry = new SpriteGeometry(1);
		buttonLeftGeometry.addSprite(0, 0, 0, 0);
		buttonLeft = new Object3d(buttonLeftGeometry, buttonLeftMaterial);
		addChild(buttonLeft);
	}

	/**
	 * Hud elements need viewport size, this method is also called each time that the viewport is changed
	 */
	@Override
	public void onViewPortChanged(int width, int height) {
		this.width = width;

		setTitleObjectScale();

		float buttonWidth = Math.min(width, height) / 5;
		((SpriteGeometry) buttonLeft.getGeometry3d()).setSpritePosition(0, 0, height - buttonWidth, buttonWidth, height);
		((SpriteGeometry) buttonRight.getGeometry3d()).setSpritePosition(0, width - buttonWidth, height - buttonWidth, width, height);
	}

	public void setTitle(String title) {
		if (this.title != title) {
			font.setTextLine(titleObject3d, title, fm);
			setTitleObjectScale();
		}
	}

	private void setTitleObjectScale() {
		// Do not exceed screen width
		float titleScale = ((float) width) / ((float) fm.right);
		if (titleScale > 1) {
			titleScale = 1f;
		}
		titleObject3d.setScale(titleScale);
		// center in screen
		titleObject3d.setPosition((width - (((float) fm.right) * titleScale)) / 2, fm.bottom * titleScale * 0.5f, 0);
	}
}
