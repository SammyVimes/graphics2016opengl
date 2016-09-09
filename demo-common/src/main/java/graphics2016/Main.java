package graphics2016;

import graphics2016.demo.DemoScreenController;
import graphics2016.desktop.Window3d;

/**
 * Created by Semyon on 09.09.2016.
 */
public class Main {

    public static void main(String[] args) {
        Window3d window3d = new Window3d(640, 480);
        window3d.setScreenController(new DemoScreenController());
        window3d.show();
    }

}
