package graphics2016.geometry;

import graphics2016.Vector3;

public class SkyboxGeometry extends VariableGeometry {

	public SkyboxGeometry(float radius) {
		super(24, 12);
		addQuad(new Vector3(radius, radius, radius), new Vector3(radius, -radius, radius), new Vector3(radius, radius, -radius), new Vector3(radius, -radius, -radius));
		addQuad(new Vector3(-radius, -radius, radius), new Vector3(-radius, radius, radius), new Vector3(-radius, -radius, -radius), new Vector3(-radius, radius, -radius));
		addQuad(new Vector3(-radius, radius, radius), new Vector3(radius, radius, radius), new Vector3(-radius, radius, -radius), new Vector3(radius, radius, -radius));
		addQuad(new Vector3(radius, -radius, radius), new Vector3(-radius, -radius, radius), new Vector3(radius, -radius, -radius), new Vector3(-radius, -radius, -radius));
		addQuad(new Vector3(-radius, -radius, radius), new Vector3(radius, -radius, radius), new Vector3(-radius, radius, radius), new Vector3(radius, radius, radius));
		addQuad(new Vector3(-radius, radius, -radius), new Vector3(radius, radius, -radius), new Vector3(-radius, -radius, -radius), new Vector3(radius, -radius, -radius));
	}
}
