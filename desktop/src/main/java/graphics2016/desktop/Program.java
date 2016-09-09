package graphics2016.desktop;

import com.jogamp.opengl.GL2;
import graphics2016.Color4;
import graphics2016.GpuObjectStatus;
import graphics2016.Object3d;
import graphics2016.Scene;
import graphics2016.material.Material;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

public class Program extends graphics2016.shader.Program {
	public static final String TAG = "Program";

	int webGLProgram;

	public void log(String message) {
		System.out.println(TAG + ": " + message);
	}
	
	public void loge(String message) {
		System.err.println(TAG + ": " + message);
	}

	public void init(Scene scene, Material material, ResourceLoader resourceLoader) {

		prepareShader(scene, material);

		StringBuffer vertexShaderStringBuffer = new StringBuffer();
		StringBuffer fragmentShaderStringBuffer = new StringBuffer();

		// TODO precision
		for (String k : shaderDefines.keySet()) {
			if (shaderDefines.get(k) == null) {
				vertexShaderStringBuffer.append("#define " + k + "\n");
				fragmentShaderStringBuffer.append("#define " + k + "\n");
			} else {
				vertexShaderStringBuffer.append("#define " + k + " " + shaderDefines.get(k) + "\n");
				fragmentShaderStringBuffer.append("#define " + k + " " + shaderDefines.get(k) + "\n");
			}
		}

		vertexShaderStringBuffer.append(resourceLoader.loadRawString(vertexShaderName));
		fragmentShaderStringBuffer.append(resourceLoader.loadRawString(fragmentShaderName));

		String vertexShaderString = vertexShaderStringBuffer.toString();
		String fragmentShaderString = fragmentShaderStringBuffer.toString();

		int vertexShader = getShader(GL2.GL_VERTEX_SHADER, vertexShaderString);
		int fragmentShader = getShader(GL2.GL_FRAGMENT_SHADER, fragmentShaderString);

		webGLProgram = GLHolder.gl2.glCreateProgram();
		GLHolder.gl2.glAttachShader(webGLProgram, vertexShader);
		GLHolder.gl2.glAttachShader(webGLProgram, fragmentShader);
		GLHolder.gl2.glLinkProgram(webGLProgram);

		IntBuffer intBuffer = IntBuffer.allocate(1);
		GLHolder.gl2.glGetProgramiv(webGLProgram, GL2.GL_LINK_STATUS, intBuffer);
		if (intBuffer.get(0) != 1) {
			loge("Could not link program: ");

			int size = intBuffer.get(0);
			System.err.println("Program link error: ");
			if (size > 0) {
				ByteBuffer byteBuffer = ByteBuffer.allocate(size);
				GLHolder.gl2.glGetProgramInfoLog(webGLProgram, size, intBuffer, byteBuffer);
				for (byte b: byteBuffer.array()) {
					System.err.print((char) b);
				}
			} else {
				System.out.println("Unknown");
			}
			GLHolder.gl2.glDeleteProgram(webGLProgram);
			throw new RuntimeException("Could not initialize shaders");
		}
		GLHolder.gl2.glUseProgram(webGLProgram);

		onShaderLoaded();

		GLHolder.gl2.glDeleteShader(vertexShader);
		GLHolder.gl2.glDeleteShader(fragmentShader);
	}

	public void setSceneUniforms(Scene scene) {
		// Reset cached attribs at the beginning of each frame
		activeVertexPosition = null;
		activeVertexNormal = null;
		activeTextureCoord = null;
		activeVertexColor = null;
		activeFacesBuffer = null;

		if (useMap && mapLast != 0) {
			GLHolder.gl2.glUniform1i(mapUniform, 0);
			mapLast = 0;
		}
		if (useEnvMap && envMapLast != 1) {
			GLHolder.gl2.glUniform1i(envMapUniform, 1);
			envMapLast = 1;
		}
		if (useCameraPosition && !cameraPositionLast.equals(scene.camera.position)) {
			GLHolder.gl2.glUniform3f(cameraPositionUniform, scene.camera.position.x, scene.camera.position.y, scene.camera.position.z);
			cameraPositionLast.setAllFrom(scene.camera.position);
		}
		if (useNormalMap && normalMapLast != 2) {
			GLHolder.gl2.glUniform1i(normalMapUniform, 2);
			normalMapLast = 2;
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onSetSceneUniforms(scene);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onSetSceneUniforms(scene);
		}
	}

	public void drawObject(Renderer3d renderer3d, GpuUploader gpuUploader, Object3d o3d, float[] projectionMatrix, float viewMatrix[]) {
		setMatrix4UniformIfChanged(projectionMatrixUniform, projectionMatrix, this.projectionMatrixLast);
		setMatrix4UniformIfChanged(viewMatrixUniform, viewMatrix, this.viewMatrixLast);
		setMatrix4UniformIfChanged(modelMatrixUniform, o3d.modelMatrix, this.modelMatrixLast);
		if ((useNormals || useNormalMap) && o3d.normalMatrix != null) {
			setMatrix3UniformIfChanged(normalMatrixUniform, o3d.normalMatrix, this.normalMatrixLast);
		}

		GeometryBuffers buffers = gpuUploader.upload(o3d.geometry3d);
		Integer vertexColorsBufferId = null;
		if (useVertexColors) {
			vertexColorsBufferId = gpuUploader.upload(o3d.vertexColors);
		}

		if (useMap) {
			gpuUploader.upload(renderer3d, o3d.material.map, GL2.GL_TEXTURE0);
			if ((o3d.material.map.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useEnvMap) {
			gpuUploader.upload(renderer3d, o3d.material.envMap, GL2.GL_TEXTURE1);
			if ((o3d.material.envMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useNormalMap) {
			gpuUploader.upload(renderer3d, o3d.material.normalMap, GL2.GL_TEXTURE2);
			if ((o3d.material.normalMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}

		if (activeVertexPosition == null || !Objects.equals(activeVertexPosition, buffers.vertexBufferId)) {
			activeVertexPosition = buffers.vertexBufferId;
			GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.vertexBufferId);
			GLHolder.gl2.glVertexAttribPointer(vertexPositionAttribLocation, 3, GL2.GL_FLOAT, false, 0, 0);
		}

		if (useNormals) {
			if (activeVertexNormal == null || !Objects.equals(activeVertexNormal, buffers.normalsBufferId)) {
				activeVertexNormal = buffers.normalsBufferId;
				if (vertexNormalAttribLocation == -1) {
					vertexNormalAttribLocation = getAndEnableAttribLocation("vertexNormal");
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.normalsBufferId);
				GLHolder.gl2.glVertexAttribPointer(vertexNormalAttribLocation, 3, GL2.GL_FLOAT, false, 0, 0);
			}
		}

		if (useMap) {
			if (activeTextureCoord == null || !Objects.equals(activeTextureCoord, buffers.uvsBufferId)) {
				activeTextureCoord = buffers.uvsBufferId;
				if (textureCoordAttribLocation == -1) {
					textureCoordAttribLocation = getAndEnableAttribLocation("textureCoord");
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.uvsBufferId);
				GLHolder.gl2.glVertexAttribPointer(textureCoordAttribLocation, 2, GL2.GL_FLOAT, false, 0, 0);
			}
		}

		if (useVertexColors && (vertexColorsBufferId != null)) {
			if (activeVertexColor == null || !Objects.equals(activeVertexColor, vertexColorsBufferId)) {
				activeVertexColor = vertexColorsBufferId;
				if (vertexColorAttribLocation == -1) {
					vertexColorAttribLocation = getAndEnableAttribLocation("vertexColor");
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexColorsBufferId);
				GLHolder.gl2.glVertexAttribPointer(vertexColorAttribLocation, 4, GL2.GL_FLOAT, false, 0, 0);
			}
		}

		if (activeFacesBuffer == null || !Objects.equals(activeFacesBuffer, buffers.facesBufferId)) {
			GLHolder.gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
			activeFacesBuffer = buffers.facesBufferId;
		}

		setColorUniformIfChanged(objectColorUniform, o3d.material.color, objectColorLast);

		if (useMap) {
			Integer mapTextureId = gpuUploader.textures.get(o3d.material.map);
			if (!Objects.equals(renderer3d.mapTextureId, mapTextureId)) {
				if (renderer3d.activeTexture != GL2.GL_TEXTURE0) {
					GLHolder.gl2.glActiveTexture(GL2.GL_TEXTURE0);
					renderer3d.activeTexture = GL2.GL_TEXTURE0;
				}
				GLHolder.gl2.glBindTexture(GL2.GL_TEXTURE_2D, gpuUploader.textures.get(o3d.material.map));
				renderer3d.mapTextureId = mapTextureId;
			}
		}
		if (useEnvMap) {
			if (reflectivityLast != o3d.material.reflectivity) {
				GLHolder.gl2.glUniform1f(reflectivityUniform, o3d.material.reflectivity);
				reflectivityLast = o3d.material.reflectivity;
			}
			Integer envMapTextureId = gpuUploader.cubeMapTextures.get(o3d.material.envMap);
			if (!Objects.equals(renderer3d.envMapTextureId, envMapTextureId)) {
				if (renderer3d.activeTexture != GL2.GL_TEXTURE1) {
					GLHolder.gl2.glActiveTexture(GL2.GL_TEXTURE1);
					renderer3d.activeTexture = GL2.GL_TEXTURE1;
				}
				GLHolder.gl2.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, envMapTextureId);
				renderer3d.envMapTextureId = envMapTextureId;
			}
		}
		if (useNormalMap) {
			Integer normalMapTextureId = gpuUploader.textures.get(o3d.material.normalMap);
			if (!Objects.equals(renderer3d.normalMapTextureId, normalMapTextureId)) {
				if (renderer3d.activeTexture != GL2.GL_TEXTURE2) {
					GLHolder.gl2.glActiveTexture(GL2.GL_TEXTURE2);
					renderer3d.activeTexture = GL2.GL_TEXTURE2;
				}
				GLHolder.gl2.glBindTexture(GL2.GL_TEXTURE_2D, gpuUploader.textures.get(o3d.material.normalMap));
				renderer3d.normalMapTextureId = normalMapTextureId;
			}
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onDrawObject(o3d);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onDrawObject(o3d);
		}
		GLHolder.gl2.glDrawElements(GL2.GL_TRIANGLES, o3d.geometry3d.facesLength, GL2.GL_UNSIGNED_SHORT, 0);
	}

	@Override
	public int getAndEnableAttribLocation(String attribName) {
		int attribLocation = GLHolder.gl2.glGetAttribLocation(webGLProgram, attribName);
		GLHolder.gl2.glEnableVertexAttribArray(attribLocation);
		return attribLocation;
	}

	@Override
	public int getUniformLocation(String uniformName) {
		return GLHolder.gl2.glGetUniformLocation(webGLProgram, uniformName);
	}

	@Override
	public float setFloatUniformIfValueChanged(int uniform, float uniformValue, float lastValue) {
		if (lastValue != uniformValue) {
			GLHolder.gl2.glUniform1f(uniform, uniformValue);
		}
		return uniformValue;
	}

	private void setMatrix3UniformIfChanged(int matrixUniform, float newMatrix[], float lastMatrix[]) {
		if (lastMatrix[0] != newMatrix[0]
				|| lastMatrix[1] != newMatrix[1]
				|| lastMatrix[2] != newMatrix[2]
				|| lastMatrix[3] != newMatrix[3]
				|| lastMatrix[4] != newMatrix[4]
				|| lastMatrix[5] != newMatrix[5]
				|| lastMatrix[6] != newMatrix[6]
				|| lastMatrix[7] != newMatrix[7]
				|| lastMatrix[8] != newMatrix[8]) {
			GLHolder.gl2.glUniformMatrix3fv(matrixUniform, 1, false, newMatrix, 0);
			lastMatrix[0] = newMatrix[0];
			lastMatrix[1] = newMatrix[1];
			lastMatrix[2] = newMatrix[2];
			lastMatrix[3] = newMatrix[3];
			lastMatrix[4] = newMatrix[4];
			lastMatrix[5] = newMatrix[5];
			lastMatrix[6] = newMatrix[6];
			lastMatrix[7] = newMatrix[7];
			lastMatrix[8] = newMatrix[8];
		}
	}

	private void setMatrix4UniformIfChanged(int matrixUniform, float newMatrix[], float lastMatrix[]) {
		if (lastMatrix[0] != newMatrix[0]
				|| lastMatrix[1] != newMatrix[1]
				|| lastMatrix[2] != newMatrix[2]
				|| lastMatrix[3] != newMatrix[3]
				|| lastMatrix[4] != newMatrix[4]
				|| lastMatrix[5] != newMatrix[5]
				|| lastMatrix[6] != newMatrix[6]
				|| lastMatrix[7] != newMatrix[7]
				|| lastMatrix[8] != newMatrix[8]
				|| lastMatrix[9] != newMatrix[9]
				|| lastMatrix[10] != newMatrix[10]
				|| lastMatrix[11] != newMatrix[11]
				|| lastMatrix[12] != newMatrix[12]
				|| lastMatrix[13] != newMatrix[13]
				|| lastMatrix[14] != newMatrix[14]
				|| lastMatrix[15] != newMatrix[15]) {
			GLHolder.gl2.glUniformMatrix4fv(matrixUniform, 1, false, newMatrix, 0);
			lastMatrix[0] = newMatrix[0];
			lastMatrix[1] = newMatrix[1];
			lastMatrix[2] = newMatrix[2];
			lastMatrix[3] = newMatrix[3];
			lastMatrix[4] = newMatrix[4];
			lastMatrix[5] = newMatrix[5];
			lastMatrix[6] = newMatrix[6];
			lastMatrix[7] = newMatrix[7];
			lastMatrix[8] = newMatrix[8];
			lastMatrix[9] = newMatrix[9];
			lastMatrix[10] = newMatrix[10];
			lastMatrix[11] = newMatrix[11];
			lastMatrix[12] = newMatrix[12];
			lastMatrix[13] = newMatrix[13];
			lastMatrix[14] = newMatrix[14];
			lastMatrix[15] = newMatrix[15];
		}
	}

	@Override
	public void setColorUniformIfChanged(int colorUniform, Color4 newColor, Color4 lastColor) {
		if (newColor.r != lastColor.r || newColor.g != lastColor.g || newColor.b != lastColor.b || newColor.a != lastColor.a) {
			GLHolder.gl2.glUniform4f(colorUniform, newColor.r, newColor.g, newColor.b, newColor.a);
			lastColor.r = newColor.r;
			lastColor.g = newColor.g;
			lastColor.b = newColor.b;
			lastColor.a = newColor.a;
		}
	}

	@Override
	public void setUniform3fv(int location, int count, float[] v) {
		GLHolder.gl2.glUniform3fv(location, count, v, 0);
	}

	@Override
	public void setUniform4fv(int location, int count, float[] v) {
		GLHolder.gl2.glUniform4fv(location, count, v, 0);
	}

	private int getShader(int type, String source) {
		int vertexShaderId = GLHolder.gl2.glCreateShader(type);

		GLHolder.gl2.glShaderSource(vertexShaderId, 1, new String[] {source}, new int[] {source.length()}, 0);
		GLHolder.gl2.glCompileShader(vertexShaderId);

		return vertexShaderId;
	}
}