import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;
import processors.FourierProcessor;

public class Main {
    public static void main(String[] args) {
        Image encoded = FourierProcessor.encode(FileReader.readImage(ImageType.Store, "store.png"),
                FileReader.readImage(ImageType.Encode, "encode.png"), 4, 2);

        FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output, "encoded.png");

        Image decoded = FourierProcessor.decode(encoded, 4, 2);

        FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png");
    }
}