package processors;

import filereader.Image;

public class BitNoiseProcessor implements ImageProcessor {
  private static BitNoiseProcessor instance = new BitNoiseProcessor();  
  
  private BitNoiseProcessor() { instance = this; }

  public static BitNoiseProcessor instance() { return instance; }
  
  @Override
  public Image encode(Image source, Image encode) {
    return source;
  }

  @Override
  public Image decode(Image decode) {
    return decode;
  }
}
