package processors;

import filereader.Image;

public interface ImageProcessor {
  public abstract Image encode(Image source, Image encode);
  public abstract Image decode(Image decode);
}