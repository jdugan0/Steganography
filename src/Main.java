import java.util.Scanner;

import cli.ParsedCommand;
import processors.ImageProcessor;

public class Main {
  public static void main(String... args) {
    Scanner scanner = new Scanner(System.in);
    String line;
    line = scanner.nextLine();
    while (!line.equals("exit")) {
      ImageProcessor.execute(ParsedCommand.parse(line));
      line = scanner.nextLine();
    }
    scanner.close();

    /* Image encoded = FourierProcessor.encode(FileReader.readImage(ImageType.Store, "store.png"),
        FileReader.readImage(ImageType.Encode, "encode.png"), 5, 4);
      FileReader.writeImage(Image.toBufferedImage(encoded), ImageType.Output, "encoded.png");
      Image decoded = FourierProcessor.decode(encoded, 5, 4);
      FileReader.writeImage(Image.toBufferedImage(decoded), ImageType.Output, "decoded.png"); */
  }
}