package graphics2016.demo;

import graphics2016.CubeMapTexture;
import graphics2016.Object3d;
import graphics2016.geometry.BoxGeometry;
import graphics2016.geometry.Geometry;
import graphics2016.material.Material;

public class EnvMapCubeScene extends ParentScene {

	public EnvMapCubeScene() {
		super("Cube with a cube map texture");

		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

		Material material1 = new Material();
		material1.color.setAll(0, 0, 0, 0);
		material1.setEnvMap(envMap, 0);
		material1.useEnvMapAsMap = true;

		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);

		addChild(o3d);
	}
}
