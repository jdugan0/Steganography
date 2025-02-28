import filereader.FileReader;
import filereader.Image;
import filereader.FileReader.ImageType;
import processors.BitNoiseProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String... args) {
        // testing bit noise processor
        Image source = FileReader.readImage(ImageType.Source, "source.png");
        Image encode = FileReader.readImage(ImageType.Source, "encode.png");
        try {
            Path output = Files.createFile(Paths.get(
                new File("").getAbsolutePath()
                + File.separator + "Images" + File.separator + "Output/output.png"));
            Image outputImage = BitNoiseProcessor.instance().encode(source, encode);
            FileReader.writeImage(Image.toBufferedImage(outputImage), ImageType.Output, "output.png");
        } catch (IOException e) {
            System.out.println("Failure");
        }
    }
}