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
        Image encode = FileReader.readImage(ImageType.Encode, "encode.png");
        int noiseThreshold = 4;
        try {
            Path decode = Files.createFile(Paths.get(
                new File("").getAbsolutePath()
                + File.separator + "Images" + File.separator + "Decode/decode.png"));
            Image outputImage = BitNoiseProcessor.instance().encode(source, encode, noiseThreshold);
            FileReader.writeImage(Image.toBufferedImage(outputImage), ImageType.Decode, "decode.png");

            Path output = Files.createFile(Paths.get(
                new File("").getAbsolutePath()
                + File.separator + "Images" + File.separator + "Output/output.png"));
            Image decodedImage = BitNoiseProcessor.instance().decode(outputImage, noiseThreshold);
            FileReader.writeImage(Image.toBufferedImage(decodedImage), ImageType.Output, "output.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}