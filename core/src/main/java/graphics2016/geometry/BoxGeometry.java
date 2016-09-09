package graphics2016.geometry;

import graphics2016.Vector3;

public class BoxGeometry extends VariableGeometry {

	public BoxGeometry(float radius) {
		super(24, 12);
		addBox(new Vector3(-radius, -radius, radius), new Vector3(radius, -radius, radius), //
				new Vector3(-radius, -radius, -radius), new Vector3(radius, -radius, -radius), //
				new Vector3(-radius, radius, radius), new Vector3(radius, radius, radius), //
				new Vector3(-radius, radius, -radius), new Vector3(radius, radius, -radius));
	}
}
