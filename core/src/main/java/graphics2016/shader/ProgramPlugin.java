package graphics2016.shader;

import graphics2016.Object3d;
import graphics2016.Scene;
import graphics2016.material.Material;

public class ProgramPlugin {

	Program program;

	public ProgramPlugin(Program program) {
		this.program = program;
	}

	/**
	 * Prepare shaders
	 *
	 * @defines to be included in both fragment and vertex shader
	 */
	public void prepareShader(Scene scene, Material material) {
	}

	/**
	 * Initialize uniforms, etc
	 */
	public void onShaderLoaded() {
	}

	/**
	 * Initialize the scene uniforms before each frame
	 */
	public void onSetSceneUniforms(Scene scene) {
	}

	/**
	 * Called before each object is drawn
	 */
	public void onDrawObject(Object3d o3d) {
	}
}
