package processors;

import filereader.Image;

public class BitNoiseProcessor implements ImageProcessor {
  /** static instance */
  private static BitNoiseProcessor instance = new BitNoiseProcessor(4);

  /** noise threshold for encode/decode (see constructor note) */
  private int threshold;

  /**
   * Constructs a new {@code BitNoiseProcessor}
   * with the specified noise threshold.
   * @param threshold Noise threshold for image
   * encode/decode. This number represents the
   * number of bits in a source image that will
   * be replaced by those of the encode image.
   */
  private BitNoiseProcessor(int threshold) {
    BitNoiseProcessor.instance = this;
    this.threshold = threshold;
  }

  /**
   * Gets the static instance of this class for method calls.
   * @return {@code BitNoiseProcessor} instance
   */
  public static BitNoiseProcessor instance() {
    return BitNoiseProcessor.instance;
  }

  /**
   * Encodes the {@code encode} image into the data of the {@code source}
   * image, replacing the {@link BitNoiseProcessor#getThreshold()} least significant (rightmost)
   * bits of {@code source} with the most significant (leftmost) bits of
   * {@code encode}. The output image will appear of lower quality than either
   * input image.
   * @param source {@link Image} to conceal the encoded data. The output image of this
   * method will resemble this image.
   * @param encode {@link Image} whose data to encode. If {@link BitNoiseProcessor#decode(Image)}
   * is called on the output of this method, its output will resemble this image.
   * @return new {@link Image} with the data of {@code source} and {@code encode}.
   */
  @Override
  public Image encode(Image source, Image encode) {
    return encode(source, encode, threshold);
  }

  /**
   * Decodes an image encoded into {@code decode} by
   * {@link BitNoiseProcessor#encode(Image, Image)}, taking the
   * {@link BitNoiseProcessor#getThreshold()} rightmost bits of
   * the image as the leftmost bits of the output image. All
   * remaining bits will be 0. Consequently, the output of this
   * method will be of lower quality than the {@code encode}
   * argument passed to {@link BitNoiseProcessor#encode(Image, Image)}.
   * @param decode {@link Image} with image encoded.
   */
  @Override
  public Image decode(Image decode) {
    return decode(decode, threshold);
  }

  /**
   * See {@link BitNoiseProcessor#encode(Image, Image)}.
   * @param noiseThreshold Number of rightmost bits of
   * {@code source} to replace.
   */
  private Image encode(Image source, Image encode, int noiseThreshold) {
    // scale images
    if (source.width != encode.width || source.height != encode.height) {
      encode = Image.scale(encode, source.width, source.height);
    }
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
        int rEncoded = rTruncated * (int) (Math.pow(2, noiseThreshold)) + rSignificant;
        int gEncoded = gTruncated * (int) (Math.pow(2, noiseThreshold)) + gSignificant;
        int bEncoded = bTruncated * (int) (Math.pow(2, noiseThreshold)) + bSignificant;
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

  /**
   * See {@link BitNoiseProcessor#decode(Image)}.
   * @param noiseThreshold Number of rightmost bits
   * to take as encoded data.
   */
  private Image decode(Image decode, int noiseThreshold) {
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

  /**
   * Sets this processor's noise threshold.
   * @param threshold New threshold.
   */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
    System.out.println("Threshold set: " + threshold);
  }

  /**
   * Get default noise threshold.
   * @return Threshold.
   */
  public int getThreshold() {
    return threshold;
  }
}
