package filereader;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class FileReader {
    public enum ImageType {
        Encode,
        Decode,
        Storage,
        Source,
        Output,
        Store,
        Debug
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

    public static boolean writeImage(BufferedImage image, ImageType type, String relativeFilePath) {
        try {
            String basePath = new File("").getAbsolutePath() + File.separator + "Images" + File.separator + type;
            String fullPath = basePath + File.separator + relativeFilePath;
            System.out.println("Writing image to: " + fullPath);
            File outputFile = new File(fullPath);
            outputFile.getParentFile().mkdirs();
            return ImageIO.write(image, "png", outputFile);
        } catch (Exception e) {
            System.out.println("Image write failed: " + e);
        }
        return false;
    }

    public static boolean writeImage(BufferedImage image, String relativeFilePath) {
        try {
            String path;
            if (relativeFilePath.substring(0, 1).equals(".")) {
                path = new File("").getAbsolutePath() + relativeFilePath.substring(1);
            } else {
                path = relativeFilePath;
            }
            System.out.println("Writing image to: " + path);
            File outputFile = new File(path);
            outputFile.getParentFile().mkdirs();
            return ImageIO.write(image, "png", outputFile);
        } catch (Exception e) {
            System.out.println("Image write failed: " + e);
        }
        return false;
    }
}
