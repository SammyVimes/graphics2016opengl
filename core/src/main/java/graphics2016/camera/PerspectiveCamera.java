package graphics2016.camera;

import graphics2016.MatrixUtils;
import graphics2016.Utils;
import graphics2016.Vector3;

public class PerspectiveCamera extends Camera {

	private Vector3 vx = new Vector3();
	private Vector3 vy = new Vector3();
	private Vector3 vz = new Vector3();

	private float fovy = 45;

	public float getFovy() {
		return fovy;
	}

	public void setFovy(float fovy) {
		if (this.fovy != fovy) {
			this.fovy = fovy;
			needsMatrixUpdate = true;
		}
	}

	public float getFovx() {
		return (float) (360 / Utils.PI * Math.atan(Math.tan(fovy * Utils.PI / 360) * width / height));
	}

	public boolean updateMatrices() {
		if (needsMatrixUpdate) {
			MatrixUtils.perspective(projectionMatrix, fovy, ((float) width) / height, near, far);
			MatrixUtils.lookAt(viewMatrix, position, target, upAxis, vx, vy, vz);

			needsMatrixUpdate = false;
			return true;
		}
		return false;
	}

	public void updateViewMatrix() {
		MatrixUtils.lookAt(viewMatrix, position, target, upAxis, vx, vy, vz);
	}
}