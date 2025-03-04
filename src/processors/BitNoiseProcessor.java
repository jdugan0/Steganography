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
    return encode(source, encode, 2);
  }

  public Image encode(Image source, Image encode, int noiseThreshold) {
    // cap noise threshold to reasonable value
    noiseThreshold = Math.max(Math.min(noiseThreshold, 5), 1);
    // storage for output pixels
    int[][] r = new int[source.r.length][source.r[0].length];
    int[][] g = new int[source.g.length][source.g[0].length];
    int[][] b = new int[source.b.length][source.b[0].length];
    // iterate through source image
    for (int y = 0; y < source.r.length; y++) {
      for (int x = 0; x < source.r[y].length; x++) {
        // truncate least significant bits of source
        int rTruncated = source.r[y][x] >> noiseThreshold;
        int gTruncated = source.g[y][x] >> noiseThreshold;
        int bTruncated = source.b[y][x] >> noiseThreshold;
        // take most significant bits of encode
        int rSignificant = encode.r[y][x] >> (8 - noiseThreshold);
        int gSignificant = encode.g[y][x] >> (8 - noiseThreshold);
        int bSignificant = encode.b[y][x] >> (8 - noiseThreshold);
        // concatenate bits
        int rEncoded = rTruncated * (int)(Math.pow(2, noiseThreshold)) + rSignificant;
        int gEncoded = gTruncated * (int)(Math.pow(2, noiseThreshold)) + gSignificant;
        int bEncoded = bTruncated * (int)(Math.pow(2, noiseThreshold)) + bSignificant;
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
    return decode(decode, 2);
  }

  public Image decode(Image decode, int noiseThreshold) {
    // cap noise threshold
    noiseThreshold = Math.max(Math.min(noiseThreshold, 5), 1);
    // storage for decoded pixels
    int[][] r = new int[decode.r.length][decode.r[0].length];
    int[][] g = new int[decode.g.length][decode.g[0].length];
    int[][] b = new int[decode.b.length][decode.b[0].length];
    // iterate over pixels
    for (int y = 0; y < decode.r.length; y++) {
      for (int x = 0; x < decode.r[0].length; x++) {
        // take end bits of decode
        int rDecoded = decode.r[y][x] << (8 - noiseThreshold) & 255;
        int gDecoded = decode.g[y][x] << (8 - noiseThreshold) & 255;
        int bDecoded = decode.b[y][x] << (8 - noiseThreshold) & 255;
        // store new pixel
        r[y][x] = rDecoded;
        g[y][x] = gDecoded;
        b[y][x] = bDecoded;
      }
    }
    // create output
    Image decoded = new Image(r, g, b);
    return decoded;
  }
}
