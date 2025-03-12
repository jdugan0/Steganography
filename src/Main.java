import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;
import processors.FourierNew;

public class Main {
    public static void main(String[] args) {

        Image encoded = FourierNew.encode(FileReader.readImage(ImageType.Store, "store.png"),
                FileReader.readImage(ImageType.Encode, "encode.png"));
        FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output,
                "encoded.png");

        Image decoded = FourierNew.decode(encoded);
        FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png");

    }
}