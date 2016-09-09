package graphics2016.demo;

import graphics2016.Color4;
import graphics2016.Object3d;
import graphics2016.Texture;
import graphics2016.Vector3;
import graphics2016.geometry.BoxGeometry;
import graphics2016.geometry.Geometry;
import graphics2016.light.AmbientLight;
import graphics2016.light.DirectionalLight;
import graphics2016.light.PointLight;
import graphics2016.material.Material;
import graphics2016.material.PhongMaterial;

public class CubeScene extends ParentScene {

	public CubeScene() {
		super("A simple cube with different lights");

		Texture map = new Texture("texture.png");
		Color4 white = new Color4(255, 255, 255, 255);
		Material material1 = new PhongMaterial(map, white, white, white);
		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);

		addChild(o3d);

		addLight(new AmbientLight(new Color4(100, 100, 100), 0.5f));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 1.1f));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0)));
	}
}
