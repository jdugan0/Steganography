import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;
import processors.FourierSteganographyColor;

public class Main {
    public static void main(String[] args) {
        Image encoded = FourierSteganographyColor.encode(FileReader.readImage(ImageType.Store, "store.png"),
                FileReader.readImage(ImageType.Encode, "encode.png"), 0.2);

        FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output, "encoded.png");

        Image decoded = FourierSteganographyColor.decode(encoded, (int) (encoded.width * 0.2),
                (int) (encoded.height * 0.2));

        FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png");
    }
}