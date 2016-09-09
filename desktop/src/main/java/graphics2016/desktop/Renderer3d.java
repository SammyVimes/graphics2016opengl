package graphics2016.desktop;

import com.jogamp.opengl.GL;
import graphics2016.Blending;
import graphics2016.Object3d;
import graphics2016.Scene;

/**
 * Created by Semyon on 09.09.2016.
 */
public class Renderer3d implements graphics2016.Renderer3d {
    
    public static final String TAG = "Renderer";


    private ResourceLoader resourceLoader;
    private GpuUploader gpuUploader;

    Blending blending = null;
    Program currentProgram = null;
    Integer mapTextureId = -1;
    Integer envMapTextureId = -1;
    Integer normalMapTextureId = -1;
    int activeTexture = -1;

    int width = -1;
    int height = -1;

    public Renderer3d(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        gpuUploader = new GpuUploader(resourceLoader);
    }

    public void reset() {
        mapTextureId = -1;
        envMapTextureId = -1;
        gpuUploader.reset();

        GLHolder.gl2.glEnable(GL.GL_DEPTH_TEST);
        GLHolder.gl2.glClearDepthf(1f);
        GLHolder.gl2.glDepthFunc(GL.GL_LEQUAL);
        GLHolder.gl2.glDepthRangef(0, 1f);
        GLHolder.gl2.glDepthMask(true);

        // For performance
        GLHolder.gl2.glDisable(GL.GL_DITHER);

        // For transparency
        setBlending(Blending.NoBlending);

        // CCW frontfaces only, by default
        GLHolder.gl2.glFrontFace(GL.GL_CCW);
        GLHolder.gl2.glCullFace(GL.GL_BACK);
        GLHolder.gl2.glEnable(GL.GL_CULL_FACE);
    }

    public void setViewPort(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            GLHolder.gl2.glViewport(0, 0, width, height);
        }
    }

    public void render(Scene scene) {
        scene.setViewPort(width, height);
        scene.camera.updateMatrices();
        render(scene, scene.camera.projectionMatrix, scene.camera.viewMatrix);
    }

    public void render(Scene scene, float[] projectionMatrix, float[] viewMatrix) {
        currentProgram = null;

        for (int i = 0; i < scene.unload.size(); i++) {
            gpuUploader.unload(scene.unload.get(i));
        }
        scene.unload.clear();

        if (scene.backgroundColor != null) {
            GLHolder.gl2.glClearColor(scene.backgroundColor.r, scene.backgroundColor.g, scene.backgroundColor.b, scene.backgroundColor.a);
            GLHolder.gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        } else {
            GLHolder.gl2.glClear(GL.GL_DEPTH_BUFFER_BIT);
        }

        for (int i = 0; i < scene.children.size(); i++) {
            Object3d o3d = scene.children.get(i);
            if (o3d.visible) {
                o3d.updateMatrices();
                drawObject(scene, o3d, projectionMatrix, viewMatrix);

                if (o3d.clearDepthAfterDraw) {
                    GLHolder.gl2.glClear(GL.GL_DEPTH_BUFFER_BIT);
                }
            }
        }
    }

    private void drawObject(Scene scene, Object3d o3d, float[] projectionMatrix, float[] viewMatrix) {
        Program program = gpuUploader.getProgram(scene, o3d.material);

        if (program != currentProgram) {
            GLHolder.gl2.glUseProgram(program.webGLProgram);
            program.setSceneUniforms(scene);
            currentProgram = program;
        }

        if (blending != o3d.material.blending) {
            setBlending(o3d.material.blending);
        }

        program.drawObject(this, gpuUploader, o3d, projectionMatrix, viewMatrix);
        for (int i = 0; i < o3d.getChildren().size(); i++) {
            drawObject(scene, o3d.getChildren().get(i), projectionMatrix, viewMatrix);
        }
    }

    private void setBlending(Blending blending) {
        if (this.blending == null || !this.blending.equals(blending)) {
            this.blending = blending;

            switch (blending) {
                case NoBlending:
                    GLHolder.gl2.glDisable(GL.GL_BLEND);
                    break;
                case NormalBlending:
                    GLHolder.gl2.glEnable(GL.GL_BLEND);
                    GLHolder.gl2.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                    break;
                case AdditiveBlending:
                    GLHolder.gl2.glEnable(GL.GL_BLEND);
                    GLHolder.gl2.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
                    break;
                case SubtractiveBlending:
                    GLHolder.gl2.glEnable(GL.GL_BLEND);
                    GLHolder.gl2.glBlendFunc(GL.GL_ZERO, GL.GL_ONE_MINUS_SRC_COLOR);
                    break;
                case MultiplyBlending:
                    GLHolder.gl2.glEnable(GL.GL_BLEND);
                    GLHolder.gl2.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
                    break;
            }
        }
    }

    public GpuUploader getGpuUploader() {
        return gpuUploader;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

}
