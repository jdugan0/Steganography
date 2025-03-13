import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;
import processors.FourierDownsample;
import processors.PhaseEncode;

public class Main {
    public static void main(String[] args) {

        Image encoded = FourierDownsample.encode(FileReader.readImage(ImageType.Store, "store.png"),
                FileReader.readImage(ImageType.Encode, "encode.png"));
        FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output,
                "encoded.png");

        Image decoded = FourierDownsample.decode(encoded);
        FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png");

    }
}