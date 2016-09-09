package graphics2016.demo;

import graphics2016.Color4;
import graphics2016.CubeMapTexture;
import graphics2016.Object3d;
import graphics2016.Vector3;
import graphics2016.geometry.Geometry;
import graphics2016.geometry.SkyboxGeometry;
import graphics2016.geometry.VariableGeometry;
import graphics2016.light.AmbientLight;
import graphics2016.light.DirectionalLight;
import graphics2016.light.PointLight;
import graphics2016.material.Material;
import graphics2016.material.PhongMaterial;

public class TeapotScene extends ParentScene {

	public TeapotScene() {
		super("Skybox and cube map reflections");

		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

		Color4 transparent = new Color4(0, 0, 0, 0);
		Color4 white = new Color4(255, 255, 255, 255);

		PhongMaterial mirrorMat = new PhongMaterial(white, white, white);
		mirrorMat.setEnvMap(envMap, 1f);

		VariableGeometry skyboxGeometry = new SkyboxGeometry(300);
		Material skyboxMaterial = new Material();
		skyboxMaterial.setEnvMap(envMap, 0);
		skyboxMaterial.setUseEnvMapAsMap(true);
		Object3d skybox = new Object3d(skyboxGeometry, skyboxMaterial);
		addChild(skybox);

		Geometry teapotGeometry = new TeapotGeometry();
		Object3d teapotO3d = new Object3d(teapotGeometry, mirrorMat);
		teapotO3d.setPosition(0, 0, -0.5f);
		addChild(teapotO3d);

		addLight(new AmbientLight(new Color4(255, 255, 255), 0.1f));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 1));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0)));
	}
}
