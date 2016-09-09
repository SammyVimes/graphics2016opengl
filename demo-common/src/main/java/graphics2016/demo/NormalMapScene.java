package graphics2016.demo;

import graphics2016.Color4;
import graphics2016.Object3d;
import graphics2016.Texture;
import graphics2016.Vector3;
import graphics2016.geometry.Geometry;
import graphics2016.geometry.PlaneGeometry;
import graphics2016.light.PointLight;
import graphics2016.material.PhongMaterial;

public class NormalMapScene extends ParentScene {

	PointLight pointLight;

	public NormalMapScene() {
		super("Normal map in one of the two textures");

		Texture map = new Texture("wall.png");
		Texture normalMap = new Texture("wall_normal.png");

		Color4 white = new Color4(255, 255, 255, 255);
		Color4 transparent = new Color4(0, 0, 0, 0);
		PhongMaterial material = new PhongMaterial(map, white, white, transparent);

		PhongMaterial materialMap = new PhongMaterial(map, white, white, white);
		materialMap.setNormalMap(normalMap);

		Geometry planeGeometry = new PlaneGeometry(1);

		Object3d plane1 = new Object3d(planeGeometry, material);
		plane1.setPosition(-1, 0, 0);
		addChild(plane1);

		Object3d plane2 = new Object3d(planeGeometry, materialMap);
		plane2.setPosition(1, 0, 0);
		addChild(plane2);

		pointLight = new PointLight(new Vector3(0, 0, 0.5f), new Color4(128, 128, 128), 2);
		addLight(pointLight);
	}

	@Override
	public void update() {
		pointLight.position.setAllFrom(camera.getPosition());
	}
}
