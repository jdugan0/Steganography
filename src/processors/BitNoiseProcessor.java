package processors;

import filereader.Image;

public class BitNoiseProcessor implements ImageProcessor {
  private static BitNoiseProcessor instance = new BitNoiseProcessor();  
  
  private BitNoiseProcessor() { instance = this; }

  public static BitNoiseProcessor instance() { return instance; }
  
  @Override
  public Image encode(Image source, Image encode) {
    // storage for output pixels
    int[][] r = new int[source.r.length][source.r[0].length];
    int[][] g = new int[source.g.length][source.g[0].length];
    int[][] b = new int[source.b.length][source.b[0].length];
    // iterate through source image
    for (int y = 0; y < source.r.length; y++) {
      for (int x = 0; x < source.r[y].length; x++) {
        // take 2 most significant bits of encode
        String rCompressed = Integer.toBinaryString((byte)encode.r[y][x])
          .substring(0, 2);
        String gCompressed = Integer.toBinaryString((byte)encode.g[y][x])
          .substring(0, 2);
        String bCompressed = Integer.toBinaryString((byte)encode.b[y][x])
          .substring(0, 2);
        // replace 2 least significant bits of source
        String rEncoded = Integer.toBinaryString((byte)source.r[y][x])
          .substring(0, 6) + rCompressed;
        String gEncoded = Integer.toBinaryString((byte)source.g[y][x])
          .substring(0, 6) + gCompressed;
        String bEncoded = Integer.toBinaryString((byte)source.b[y][x])
          .substring(0, 6) + bCompressed;
        // store new pixel
        r[y][x] = (int)Byte.parseByte(rEncoded);
        g[y][x] = (int)Byte.parseByte(gEncoded);
        b[y][x] = (int)Byte.parseByte(bEncoded);
      }
    }
    // create output
    Image output = new Image(r, g, b);
    return output;
  }

  @Override
  public Image decode(Image decode) {
    return decode;
  }
}
