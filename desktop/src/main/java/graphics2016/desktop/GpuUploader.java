package graphics2016.desktop;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.sun.prism.impl.BufferUtil;
import graphics2016.*;
import graphics2016.geometry.Geometry;
import graphics2016.material.Material;
import graphics2016.shader.ShaderKey;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class GpuUploader {
	static final String TAG = "GpuUploader";

	ResourceLoader resourceLoader;

	HashMap<Geometry, GeometryBuffers> geometryBuffers = new HashMap<>();
	HashMap<VertexColors, Integer> vertexColorsBuffers = new HashMap<>();
	HashMap<Texture, Integer> textures = new HashMap<>();
	HashMap<CubeMapTexture, Integer> cubeMapTextures = new HashMap<>();
	ArrayList<Program> shaderPrograms = new ArrayList<>();

	public GpuUploader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Program getProgram(Scene scene, Material material) {
		if (scene.shaderKey == -1) {
			scene.shaderKey = ShaderKey.getSceneKey(scene);
		}
		if (material.shaderKey == -1) {
			material.shaderKey = ShaderKey.getMaterialKey(material);
		}
		int key = scene.shaderKey & material.shaderKey;
		Program program = null;
		// Use ArrayList instead HashMap to avoid Integer creation
		for (int i = 0; i < shaderPrograms.size(); i++) {
			if (key == shaderPrograms.get(i).shaderKey) {
				program = shaderPrograms.get(i);
			}
		}
		if (program == null) {
			program = new Program();
			program.shaderKey = key;
			program.init(scene, material, resourceLoader);
			shaderPrograms.add(program);
		}
		return program;
	}

	public GeometryBuffers upload(Geometry geometry3d) {
		GeometryBuffers buffers = geometryBuffers.get(geometry3d);
		if (buffers == null) {
			buffers = new GeometryBuffers();
			geometryBuffers.put(geometry3d, buffers);
		}

		if ((geometry3d.status & GpuObjectStatus.VERTICES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.VERTICES_UPLOADED;
			float[] vertex = geometry3d.vertex();
			if (vertex != null) {
				if (buffers.vertexBufferId == null) {
					int[] vboId = new int[1];
					GLHolder.gl2.glGenBuffers(1, vboId, 0);
					buffers.vertexBufferId = vboId[0];
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.vertexBufferId);
				GLHolder.gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertex.length * 4, FloatBuffer.wrap(vertex), GL2.GL_STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
			float[] normals = geometry3d.normals();
			if (normals != null) {
				if (buffers.normalsBufferId == null) {
					int[] vboId = new int[1];
					GLHolder.gl2.glGenBuffers(1, vboId, 0);
					buffers.normalsBufferId = vboId[0];
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.normalsBufferId);
				GLHolder.gl2.glBufferData(GL2.GL_ARRAY_BUFFER, normals.length * 4, FloatBuffer.wrap(normals), GL2.GL_STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
			float[] uvs = geometry3d.uvs();
			if (uvs != null) {
				if (buffers.uvsBufferId == null) {
					int[] vboId = new int[1];
					GLHolder.gl2.glGenBuffers(1, vboId, 0);
					buffers.uvsBufferId = vboId[0];
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.uvsBufferId);
				GLHolder.gl2.glBufferData(GL2.GL_ARRAY_BUFFER, uvs.length * 4, FloatBuffer.wrap(uvs), GL2.GL_STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
			short[] faces = geometry3d.faces();
			if (faces != null) {
				geometry3d.facesLength = faces.length;
				if (buffers.facesBufferId == null) {
					int[] vboId = new int[1];
					GLHolder.gl2.glGenBuffers(1, vboId, 0);
					buffers.facesBufferId = vboId[0];
				}

				GLHolder.gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
				GLHolder.gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, faces.length * 2, ShortBuffer.wrap(faces), GL2.GL_STATIC_DRAW);
			}
		}

		return buffers;
	}

	public Integer upload(VertexColors vertexColors) {
		if (vertexColors == null) {
			return null;
		}

		Integer bufferId = vertexColorsBuffers.get(vertexColors);
		if ((vertexColors.status & GpuObjectStatus.VERTEX_COLORS_UPLOADED) == 0) {
			vertexColors.status |= GpuObjectStatus.VERTEX_COLORS_UPLOADED;

			float[] colors = vertexColors.getVertexColors();
			if (colors != null) {
				if (bufferId == null) {
					int[] vboId = new int[1];
					GLHolder.gl2.glGenBuffers(1, vboId, 0);
					bufferId = vboId[0];
					vertexColorsBuffers.put(vertexColors, bufferId);
				}
				GLHolder.gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferId);
				GLHolder.gl2.glBufferData(GL2.GL_ARRAY_BUFFER, colors.length * 4, FloatBuffer.wrap(colors), GL2.GL_STATIC_DRAW);
			}
		}

		return bufferId;
	}

	public void upload(Renderer3d renderer3d, Texture texture, int activeTexture) {
		if ((texture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
			texture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

			ByteBuffer pixels = null;
			final int width;
			final int height;

			try {
				BufferedImage image = resourceLoader.getImage(texture.image);
				width = image.getWidth();
				height = image.getHeight();
				DataBufferByte dbb = (DataBufferByte)image.getRaster().getDataBuffer();
				byte[] data = dbb.getData();
				pixels = BufferUtil.newByteBuffer(data.length);
				pixels.put(data);
				pixels.flip();
			} catch (Exception e) {
//					(TAG, "Texture image not found in resources: " + cubeMapTexture.images[i]);
				return;
			}

			Integer textureId = textures.get(texture);
			if (textureId == null) {
				int[] texturesIds = new int[1];
				GLHolder.gl2.glGenTextures(1, texturesIds, 0);
				textureId = texturesIds[0];
				textures.put(texture, textureId);
			}

			if (renderer3d.activeTexture != activeTexture) {
				GLHolder.gl2.glActiveTexture(activeTexture);
				renderer3d.activeTexture = activeTexture;
			}
			GLHolder.gl2.glBindTexture(GL2.GL_TEXTURE_2D, textureId);
			renderer3d.mapTextureId = textureId;

			GLHolder.gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL.GL_RGB, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixels);

			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		}
	}

	public void upload(Renderer3d renderer3d, CubeMapTexture cubeMapTexture, int activeTexture) {
		if ((cubeMapTexture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

			Integer textureId = textures.get(cubeMapTexture);
			if (textureId == null) {
				int[] texturesIds = new int[1];
				GLHolder.gl2.glGenTextures(1, texturesIds, 0);
				textureId = texturesIds[0];
				cubeMapTextures.put(cubeMapTexture, textureId);
			}
			cubeMapTextures.put(cubeMapTexture, textureId);

			if (renderer3d.activeTexture != activeTexture) {
				GLHolder.gl2.glActiveTexture(activeTexture);
				renderer3d.activeTexture = activeTexture;
			}
			GLHolder.gl2.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, textureId);
			renderer3d.envMapTextureId = textureId;

			for (int i = 0; i < 6; i++) {
				ByteBuffer pixels = null;
				final int width;
				final int height;
				try {
					BufferedImage image = resourceLoader.getImage(cubeMapTexture.images[i]);
					width = image.getWidth();
					height = image.getHeight();
					DataBufferByte dbb = (DataBufferByte)image.getRaster().getDataBuffer();
					byte[] data = dbb.getData();
					pixels = BufferUtil.newByteBuffer(data.length);
					pixels.put(data);
					pixels.flip();
				} catch (Exception e) {
//					(TAG, "Texture image not found in resources: " + cubeMapTexture.images[i]);
					return;
				}

				GLHolder.gl2.glTexImage2D(Program.CUBE_MAP_SIDES[i], 0, GL.GL_RGB, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixels);
			}
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			GLHolder.gl2.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		}
	}

	public void unload(Object o) {
		if (o instanceof Geometry) {
			if (geometryBuffers.containsKey(o)) {
				((Geometry) o).status = 0;
				GeometryBuffers buffers = geometryBuffers.remove(o);
				if (buffers != null) {
					if (buffers.vertexBufferId != null) {
						GLHolder.gl2.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.vertexBufferId}));
					}
					if (buffers.normalsBufferId != null) {
						GLHolder.gl2.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.normalsBufferId}));
					}
					if (buffers.uvsBufferId != null) {
						GLHolder.gl2.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.uvsBufferId}));
					}
					if (buffers.facesBufferId != null) {
						GLHolder.gl2.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.facesBufferId}));
					}
				}
			}
		} else if (o instanceof Texture) {
			Integer texture = textures.remove(o);
			if (texture != null) {
				((Texture) o).status = 0;
				GLHolder.gl2.glDeleteTextures(1, IntBuffer.wrap(new int[]{texture}));
			}
		} else if (o instanceof CubeMapTexture) {
			Integer texture = cubeMapTextures.remove(o);
			if (texture != null) {
				((CubeMapTexture) o).status = 0;
				GLHolder.gl2.glDeleteTextures(1, IntBuffer.wrap(new int[]{texture}));
			}
		} else if (o instanceof VertexColors) {
			Integer bufferId = vertexColorsBuffers.remove(o);
			if (bufferId != null) {
				GLHolder.gl2.glDeleteBuffers(1, IntBuffer.wrap(new int[]{bufferId}));
			}
		}
	}

	public void reset() {
		// Now force re-upload of all objects
		for (Geometry geometry : geometryBuffers.keySet()) {
			geometry.status = 0;
		}
		for (Texture texture : textures.keySet()) {
			texture.status = 0;
		}
		for (CubeMapTexture texture : cubeMapTextures.keySet()) {
			texture.status = 0;
		}
		for (VertexColors vertexColors : vertexColorsBuffers.keySet()) {
			vertexColors.status = 0;
		}
		geometryBuffers.clear();
		vertexColorsBuffers.clear();
		textures.clear();
		cubeMapTextures.clear();
		shaderPrograms.clear();
	}
}