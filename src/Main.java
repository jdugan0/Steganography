import java.util.Scanner;

import cli.ParsedCommand;
import processors.ImageProcessor;

public class Main {
  public static void main(String... args) {
    try {
      Scanner scanner = new Scanner(System.in);
      ImageProcessor.execute(ParsedCommand.parse(scanner.nextLine()));
      scanner.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    /* Image encoded = FourierProcessor.encode(FileReader.readImage(ImageType.Store, "store.png"),
        FileReader.readImage(ImageType.Encode, "encode.png"), 5, 4);
      FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output, "encoded.png");
      Image decoded = FourierProcessor.decode(encoded, 5, 4);
      FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png"); */
  }
}