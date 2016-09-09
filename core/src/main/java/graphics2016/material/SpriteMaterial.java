package graphics2016.material;

import graphics2016.Blending;
import graphics2016.Color4;
import graphics2016.Texture;

public class SpriteMaterial extends Material {

	public SpriteMaterial() {
		super();
		setBlending(Blending.NormalBlending);
	}

	public SpriteMaterial(Color4 color) {
		super(color);
		setBlending(Blending.NormalBlending);
	}

	public SpriteMaterial(Texture texture) {
		super(texture);
		setBlending(Blending.NormalBlending);
	}
}
