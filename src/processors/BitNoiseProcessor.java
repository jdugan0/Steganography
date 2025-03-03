package processors;

import filereader.Image;

public class BitNoiseProcessor implements ImageProcessor {
  private static BitNoiseProcessor instance = new BitNoiseProcessor();

  private BitNoiseProcessor() {
    instance = this;
  }

  public static BitNoiseProcessor instance() {
    return instance;
  }

  @Override
  public Image encode(Image source, Image encode) {
    // storage for output pixels
    int[][] r = new int[source.r.length][source.r[0].length];
    int[][] g = new int[source.g.length][source.g[0].length];
    int[][] b = new int[source.b.length][source.b[0].length];
    // iterate through source image
    for (int y = 0; y < source.r.length; y++) {
      for (int x = 0; x < source.r[y].length; x++) {
        /*
         * String rCompressed = ParseHelpers.toBinaryString(encode.r[y][x])
         * .substring(0, 2);
         * String gCompressed = ParseHelpers.toBinaryString(encode.g[y][x])
         * .substring(0, 2);
         * String bCompressed = ParseHelpers.toBinaryString(encode.b[y][x])
         * .substring(0, 2);
         * String rEncoded = ParseHelpers.toBinaryString(source.r[y][x])
         * .substring(0, 6) + rCompressed;
         * String gEncoded = ParseHelpers.toBinaryString(source.g[y][x])
         * .substring(0, 6) + gCompressed;
         * String bEncoded = ParseHelpers.toBinaryString(source.b[y][x])
         * .substring(0, 6) + bCompressed;
         */
        // truncate two least significant bits of source
        int rTruncated = 2 >> source.r[y][x];
        int gTruncated = 2 >> source.g[y][x];
        int bTruncated = 2 >> source.b[y][x];
        // take two most significant bits of encode
        int rSignificant = source.r[y][x] << 6;
        int gSignificant = source.g[y][x] << 6;
        int bSignificant = source.b[y][x] << 6;
        // concatenate bits
        int rEncoded = rTruncated * 4 + rSignificant;
        int gEncoded = gTruncated * 4 + gSignificant;
        int bEncoded = bTruncated * 4 + bSignificant;
        // store new pixel
        r[y][x] = rEncoded;
        g[y][x] = gEncoded;
        b[y][x] = bEncoded;
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
