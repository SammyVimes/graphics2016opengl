package graphics2016.demo;

import java.util.Random;

import graphics2016.Color4;
import graphics2016.Object3d;
import graphics2016.Vector3;
import graphics2016.geometry.BoxGeometry;
import graphics2016.geometry.Geometry;
import graphics2016.light.AmbientLight;
import graphics2016.light.PointLight;
import graphics2016.material.PhongMaterial;

public class CubesScene extends ParentScene {

	public CubesScene() {
		super("Multiple cubes with a light");

		Random r = new Random();

		addLight(new AmbientLight(new Color4(255, 255, 255), 0f));
		addLight(new PointLight(new Vector3(0, 0, 0), new Color4(255, 255, 255), 1.1f));

		Color4 ambient = new Color4(255, 255, 255, 255);
		Color4 red = new Color4(255, 0, 0, 255);
		Color4 green = new Color4(0, 255, 0, 255);
		Color4 blue = new Color4(0, 0, 255, 255);
		PhongMaterial material1 = new PhongMaterial(ambient, red, red);
		PhongMaterial material2 = new PhongMaterial(ambient, green, green);
		PhongMaterial material3 = new PhongMaterial(ambient, blue, blue);

		for (int i = 0; i < 200; i++) {
			float x = r.nextFloat() * 50 - 25;
			float y = r.nextFloat() * 50 - 25;
			float z = r.nextFloat() * 50 - 25;
			Geometry geometry = new BoxGeometry(1);
			Object3d o3d;
			if (i % 3 == 0) {
				o3d = new Object3d(geometry, material1);
			} else if (i % 3 == 1) {
				o3d = new Object3d(geometry, material2);
			} else {
				o3d = new Object3d(geometry, material3);
			}
			o3d.setPosition(x, y, z);
			addChild(o3d);
		}
	}
}
