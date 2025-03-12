import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;
import processors.FourierBlind;
import processors.FourierNew;
import processors.FourierNonBlind;

public class Main {
    public static void main(String[] args) {
        // Image encoded = FourierBlind.encode(FileReader.readImage(ImageType.Store,
        // "store.png"),
        // FileReader.readImage(ImageType.Encode, "encode.png"), 0.25);

        // FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output,
        // "encoded.png");

        // Image decoded = FourierBlind.decode(encoded, (int) (encoded.width * 0.25),
        // (int) (encoded.height * 0.25));

        // FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output,
        // "decoded.png");

        // Image encoded = FourierNonBlind.encode(FileReader.readImage(ImageType.Store,
        // "store.png"),
        // FileReader.readImage(ImageType.Encode, "encode.png"), 0.05);

        // FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output,
        // "encoded.png");

        // Image decoded = FourierNonBlind.decode(encoded, 0.05);

        // FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output,
        // "decoded.png");

        Image encoded = FourierNew.encode(FileReader.readImage(ImageType.Store, "store.png"),
                FileReader.readImage(ImageType.Encode, "encode.png"));
        FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output,
                "encoded.png");

        Image decoded = FourierNew.decode(encoded);
        FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png");

    }
}