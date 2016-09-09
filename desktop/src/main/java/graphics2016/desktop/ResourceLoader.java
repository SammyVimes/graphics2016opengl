package graphics2016.desktop;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ResourceLoader {
    static final String TAG = "ResourceLoader";


    HashMap<String, BufferedImage> customBitmaps = new HashMap();

    public ResourceLoader() {
    }

    public BufferedImage getImage(String image) {
        if (customBitmaps.containsKey(image)) {
            return customBitmaps.get(image);
        }

        try {
            return ImageIO.read(getClass().getClassLoader().getResource(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String loadRawString(final String file) {
        try {
            String f = getClass().getClassLoader().getResource(file).getFile();
            f = f.substring(1);
            byte[] encoded = Files.readAllBytes(Paths.get(f));
            return new String(encoded, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    private String inputStream2String(InputStream inputStream) {
        try {
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            return new String(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}