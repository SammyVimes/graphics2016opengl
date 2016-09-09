package graphics2016.desktop;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;
import graphics2016.ScreenController;


/**
 * Created by Semyon on 09.09.2016.
 */
public class Window3d {
    private int width, height;
    private Renderer3d renderer3d;
    private ScreenController screenController;
    private int forceRedraw = 0;

    private static String TITLE = "JOGL 2 with NEWT";  // window's title
    private static final int WINDOW_WIDTH = 640;  // width of the drawable
    private static final int WINDOW_HEIGHT = 480; // height of the drawable
    private static final int FPS = 60; // animator's target frames per second

    static {
        GLProfile.initSingleton();  // The method allows JOGL to prepare some Linux-specific locking optimizations
    }

    public Window3d(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void show() {
        renderer3d = new Renderer3d(new ResourceLoader());


        // Get the default OpenGL profile, reflecting the best for your running platform
        GLProfile glp = GLProfile.getDefault();
        // Specifies a set of OpenGL capabilities, based on your profile.
        GLCapabilities caps = new GLCapabilities(glp);
        // Create the OpenGL rendering canvas
        GLWindow window = GLWindow.create(caps);

        // Create a animator that drives canvas' display() at the specified FPS.
        final FPSAnimator animator = new FPSAnimator(window, FPS, true);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent arg0) {
            }
        });

        window.addGLEventListener(new GLEventListener() {
            @Override
            public void init(final GLAutoDrawable drawable) {
                final GL2 gl = (GL2) drawable.getContext().getGL();
                GLHolder.setGl2(gl);
                renderer3d.reset();
            }

            @Override
            public void dispose(final GLAutoDrawable drawable) {
                final GL2 gl = (GL2) drawable.getContext().getGL();
                GLHolder.setGl2(gl);

            }

            @Override
            public void display(final GLAutoDrawable drawable) {
                final GL2 gl = (GL2) drawable.getContext().getGL();
                GLHolder.setGl2(gl);
                if (screenController != null) {

                    if (screenController.onNewFrame(forceRedraw > 0)) {
                        // due to buffer swapping we need to redraw three frames
                        forceRedraw = 3;
                    }

                    if (forceRedraw > 0) {
                        screenController.render(renderer3d);
                        forceRedraw--;
                    }
                }
            }

            @Override
            public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
                final GL2 gl = (GL2) drawable.getContext().getGL();
                GLHolder.setGl2(gl);
                renderer3d.setViewPort(width, height);
            }
        });
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setTitle(TITLE);
        window.setVisible(true);
        animator.start();  // start the animator loop
    }

    public void setScreenController(final ScreenController screenController) {
        this.screenController = screenController;
    }
}
