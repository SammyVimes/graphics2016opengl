package graphics2016;

public interface Renderer3d {

	public void render(Scene scene);

	public void render(Scene scene, float[] projectionMatrix, float[] viewMatrix);

}
