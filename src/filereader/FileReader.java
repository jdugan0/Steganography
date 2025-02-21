package filereader;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class FileReader {
    public enum ImageType {
        Encode,
        Decode,
        Storage
    }

    public static Image readImage(ImageType type, String relativeFilePath) {
        try {

            String basePath = new File("").getAbsolutePath() + File.separator + "Images" + File.separator + type;
            String fullPath = basePath + File.separator + relativeFilePath;
            System.out.println("Reading image from: " + fullPath);
            BufferedImage img = ImageIO.read(new File(fullPath));
            return new Image(img);
        } catch (Exception e) {
            System.out.println("Image read failed: " + e);
        }
        return null;
    }
}
