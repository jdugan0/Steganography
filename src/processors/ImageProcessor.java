package processors;

import cli.ParsedCommand;
import filereader.FileReader;
import filereader.FileReader.ImageType;
import filereader.Image;

public interface ImageProcessor {
  public abstract Image encode(Image source, Image encode);
  public abstract Image decode(Image decode);

  public static void execute(ParsedCommand command) {
    ImageProcessor processor = command.getProcessor().toInstance();
    if (command.isEncode()) {
      Image source = FileReader.readImage(ImageType.Source, "source.png");
      Image encode = FileReader.readImage(ImageType.Encode, "encode.png");
      Image output = processor.encode(source, encode);
      if (command.isPrepare().isPresent() && command.isPrepare().get()) {
        FileReader.writeImage(Image.toBufferedImage(output),
          ImageType.Decode, "decode.png");
      } else if (command.isPrepare().isPresent() && command.getPath().isPresent()) {
        FileReader.writeImage(Image.toBufferedImage(output), command.getPath().get());
      } else if (command.isPrepare().isEmpty()) {
        FileReader.writeImage(Image.toBufferedImage(output), ImageType.Output, "output.png");
      }
    } else {
      Image decode = FileReader.readImage(ImageType.Decode, "decode.png");
      Image output = processor.decode(decode);
      FileReader.writeImage(Image.toBufferedImage(output), ImageType.Output, "output.png");
    }
  }
}