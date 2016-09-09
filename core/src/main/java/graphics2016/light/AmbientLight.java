package graphics2016.light;

import graphics2016.Color4;

public class AmbientLight extends Light {

	public AmbientLight(Color4 color) {
		this.color = color;
	}

	public AmbientLight(Color4 color, float intensity) {
		this.color = color;
		this.color.a = intensity;
	}
}
