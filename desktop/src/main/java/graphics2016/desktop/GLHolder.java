package graphics2016.desktop;

import com.jogamp.opengl.GL2;

/**
 * Created by Semyon on 09.09.2016.
 */
public class GLHolder {

    public static GL2 gl2;

    public static void setGl2(final GL2 gl2) {
        GLHolder.gl2 = gl2;
    }
}
