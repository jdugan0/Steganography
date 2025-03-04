import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;
import processors.BitNoiseProcessor;
import processors.FourierProcessor;

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

        /* Image encoded = FourierProcessor.encode(FileReader.readImage(ImageType.Store, "store.png"),
                FileReader.readImage(ImageType.Encode, "encode.png"), 5, 4);
        FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output, "encoded.png");
        Image decoded = FourierProcessor.decode(encoded, 5, 4);
        FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png"); */
    }
}