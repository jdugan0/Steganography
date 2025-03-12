package processors;

import filereader.Image;
import org.jtransforms.fft.DoubleFFT_2D;

/**
 * Frequency-domain steganography that embeds a secret image into a cover image
 * using all three channels.
 * 
 * Both images are assumed to have the same resolution initially.
 * The secret image is downsampled by a given scaling factor before its data is
 * embedded.
 * For each color channel the secret image (downsampled) is converted into a
 * byte array (row-major order)
 * and then each bit is embedded into the cover image’s corresponding channel in
 * the frequency domain.
 * Each frequency coefficient’s real part is nudged by a constant DELTA based on
 * the bit value.
 * 
 * During decode the secret image’s bits are extracted from each channel,
 * reassembled into the downsampled secret image.
 */
public class FourierBlind {

    // The amount by which to modify each coefficient’s real part per embedded bit.
    private static final double DELTA = 12500;

    /**
     * Encodes the secret image into the cover image.
     * The secret image is downsampled by the specified scale and its three color
     * channels are embedded into
     * the corresponding channels of the cover image.
     *
     * @param cover  The cover image.
     * @param secret The secret image (will be downsampled).
     * @param scale  The scaling factor for downsampling the secret image (e.g., 0.5
     *               for half resolution).
     * @return A stego image containing the embedded secret image.
     */
    public static Image encode(Image cover, Image secret, double scale) {
        int coverWidth = cover.width;
        int coverHeight = cover.height;

        // Downsample secret image channels.
        int[][] secretRDown = downsampleChannel(secret.r, scale);
        int[][] secretGDown = downsampleChannel(secret.g, scale);
        int[][] secretBDown = downsampleChannel(secret.b, scale);

        // Determine dimensions of the downsampled secret.
        int secretWidth = secretRDown.length; // x-dimension
        int secretHeight = secretRDown[0].length; // y-dimension

        // Convert each downsampled secret channel into a byte array (row-major order).
        byte[] secretRData = channelToByteArray(secretRDown);
        byte[] secretGData = channelToByteArray(secretGDown);
        byte[] secretBData = channelToByteArray(secretBDown);

        // Each cover channel (frequency domain) has capacity equal to coverWidth *
        // coverHeight coefficients.
        int capacity = coverWidth * coverHeight;
        if (secretRData.length * 8 > capacity ||
                secretGData.length * 8 > capacity ||
                secretBData.length * 8 > capacity) {
            throw new IllegalArgumentException(
                    "Secret image (after downsampling) is too large to embed in cover image.");
        }

        // Process each cover channel.
        double[][] coverRDouble = toDoubleArray(cover.r);
        double[][] coverGDouble = toDoubleArray(cover.g);
        double[][] coverBDouble = toDoubleArray(cover.b);

        // Create a 2D FFT transformer for the cover dimensions.
        DoubleFFT_2D fft2D = new DoubleFFT_2D(coverHeight, coverWidth);

        // Forward FFT on each channel.
        fft2D.complexForward(coverRDouble);
        fft2D.complexForward(coverGDouble);
        fft2D.complexForward(coverBDouble);

        // Embed the secret data bits into each channel’s frequency domain.
        embedDataInFrequencyDomain(coverRDouble, secretRData);
        embedDataInFrequencyDomain(coverGDouble, secretGData);
        embedDataInFrequencyDomain(coverBDouble, secretBData);

        // Inverse FFT to return each channel to the spatial domain.
        fft2D.complexInverse(coverRDouble, true);
        fft2D.complexInverse(coverGDouble, true);
        fft2D.complexInverse(coverBDouble, true);

        // Convert back to int[][] and clamp pixel values.
        int[][] encodedR = toIntArray(coverRDouble);
        int[][] encodedG = toIntArray(coverGDouble);
        int[][] encodedB = toIntArray(coverBDouble);
        clampToByteRange(encodedR);
        clampToByteRange(encodedG);
        clampToByteRange(encodedB);

        // Construct and return the stego image.
        return new Image(encodedR, encodedG, encodedB);
    }

    /**
     * Decodes (extracts) the secret image from a stego image.
     * The secret image is recovered from all three channels.
     * You must supply the width and height of the downsampled secret image.
     *
     * @param stego        The stego image.
     * @param secretWidth  The width of the downsampled secret image.
     * @param secretHeight The height of the downsampled secret image.
     * @return The extracted (downsampled) secret image.
     */
    public static Image decode(Image stego, int secretWidth, int secretHeight) {
        int coverWidth = stego.width;
        int coverHeight = stego.height;

        // Process each channel.
        double[][] stegoRDouble = toDoubleArray(stego.r);
        double[][] stegoGDouble = toDoubleArray(stego.g);
        double[][] stegoBDouble = toDoubleArray(stego.b);
        DoubleFFT_2D fft2D = new DoubleFFT_2D(coverHeight, coverWidth);
        fft2D.complexForward(stegoRDouble);
        fft2D.complexForward(stegoGDouble);
        fft2D.complexForward(stegoBDouble);

        // Calculate number of secret bytes per channel.
        int dataLength = secretWidth * secretHeight;

        // Extract the secret data bits from each channel.
        byte[] secretRData = extractDataFromFrequencyDomain(stegoRDouble, dataLength);
        byte[] secretGData = extractDataFromFrequencyDomain(stegoGDouble, dataLength);
        byte[] secretBData = extractDataFromFrequencyDomain(stegoBDouble, dataLength);

        // Reconstruct the secret channels from the extracted byte arrays.
        int[][] secretR = byteArrayToIntArray(secretRData, secretWidth, secretHeight);
        int[][] secretG = byteArrayToIntArray(secretGData, secretWidth, secretHeight);
        int[][] secretB = byteArrayToIntArray(secretBData, secretWidth, secretHeight);

        // Return the recovered secret image.
        return new Image(secretR, secretG, secretB);
    }

    // --------------------------------------------------
    // Helper Methods
    // --------------------------------------------------

    /**
     * Downsamples a given channel (2D int array) using nearest-neighbor sampling.
     * The input channel is assumed to have dimensions [width][height].
     *
     * @param channel The original channel.
     * @param scale   The scaling factor (e.g., 0.5 for half size).
     * @return A downsampled channel.
     */
    private static int[][] downsampleChannel(int[][] channel, double scale) {
        int origWidth = channel.length;
        int origHeight = channel[0].length;
        int newWidth = (int) (origWidth * scale);
        int newHeight = (int) (origHeight * scale);
        int[][] downsampled = new int[newWidth][newHeight];
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int origX = (int) (x / scale);
                int origY = (int) (y / scale);
                // Clamp indices to be within the original dimensions.
                if (origX >= origWidth)
                    origX = origWidth - 1;
                if (origY >= origHeight)
                    origY = origHeight - 1;
                downsampled[x][y] = channel[origX][origY];
            }
        }
        return downsampled;
    }

    /**
     * Converts a 2D int channel (dimensions [width][height]) to a byte array in
     * row-major order.
     *
     * @param channel The channel to convert.
     * @return A byte array representing the channel.
     */
    private static byte[] channelToByteArray(int[][] channel) {
        int width = channel.length;
        int height = channel[0].length;
        byte[] data = new byte[width * height];
        int index = 0;
        // Row-major: iterate over y then x.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[index++] = (byte) channel[x][y];
            }
        }
        return data;
    }

    /**
     * Embeds secret data bits into the frequency domain coefficients.
     * The frequency domain is represented as a double[][] with interleaved real and
     * imaginary parts.
     *
     * @param freqDomain The frequency domain coefficients.
     * @param data       The secret data to embed.
     */
    private static void embedDataInFrequencyDomain(double[][] freqDomain, byte[] data) {
        int height = freqDomain.length;
        int width = freqDomain[0].length / 2; // number of complex numbers per row
        int totalCoeffs = height * width;
        if (data.length * 8 > totalCoeffs) {
            throw new IllegalArgumentException("Data too large to embed into the frequency domain");
        }
        int bitIndex = 0;
        outer: for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= data.length * 8)
                    break outer;
                int byteIndex = bitIndex / 8;
                int bitPos = bitIndex % 8;
                byte bit = (byte) ((data[byteIndex] >> bitPos) & 1);
                int realIndex = 2 * x;
                double realPart = freqDomain[y][realIndex];
                // Nudge the real part by DELTA: add if bit=1, subtract if bit=0.
                if (bit == 1) {
                    realPart += DELTA;
                } else {
                    realPart -= DELTA;
                }
                freqDomain[y][realIndex] = realPart;
                bitIndex++;
            }
        }
    }

    /**
     * Extracts secret data bits from the frequency domain coefficients.
     *
     * @param freqDomain The frequency domain (interleaved) coefficients.
     * @param dataLength The number of bytes to extract.
     * @return A byte array containing the extracted secret data.
     */
    private static byte[] extractDataFromFrequencyDomain(double[][] freqDomain, int dataLength) {
        int height = freqDomain.length;
        int width = freqDomain[0].length / 2;
        int totalCoeffs = height * width;
        if (dataLength * 8 > totalCoeffs) {
            throw new IllegalArgumentException("Data length too large to extract from the frequency domain");
        }
        byte[] extractedData = new byte[dataLength];
        int bitIndex = 0;
        byte currentByte = 0;
        outer: for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= dataLength * 8)
                    break outer;
                int realIndex = 2 * x;
                double realPart = freqDomain[y][realIndex];
                byte bit = (realPart > 0) ? (byte) 1 : (byte) 0;
                currentByte |= (bit << (bitIndex % 8));
                if (bitIndex % 8 == 7) {
                    extractedData[bitIndex / 8] = currentByte;
                    currentByte = 0;
                }
                bitIndex++;
            }
        }
        return extractedData;
    }

    /**
     * Converts an int[][] channel (dimensions [width][height]) to a double[][] in
     * interleaved complex format.
     * The resulting array has dimensions [height][2*width] with real parts at even
     * indices.
     */
    private static double[][] toDoubleArray(int[][] channel) {
        int w = channel.length;
        int h = channel[0].length;
        double[][] result = new double[h][2 * w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double val = channel[x][y];
                result[y][2 * x] = val;
                result[y][2 * x + 1] = 0.0;
            }
        }
        return result;
    }

    /**
     * Converts an interleaved double[][] (from JTransforms) back to an int[][]
     * channel.
     * Only the real part of each complex number is used.
     */
    private static int[][] toIntArray(double[][] complexData) {
        int h = complexData.length;
        int w = complexData[0].length / 2;
        int[][] result = new int[w][h]; // dimensions: [width][height]
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double realVal = complexData[y][2 * x];
                result[x][y] = (int) Math.round(realVal);
            }
        }
        return result;
    }

    /**
     * Clamps all values in a 2D int array to the range [0, 255].
     */
    private static void clampToByteRange(int[][] arr) {
        for (int x = 0; x < arr.length; x++) {
            for (int y = 0; y < arr[x].length; y++) {
                if (arr[x][y] < 0)
                    arr[x][y] = 0;
                if (arr[x][y] > 255)
                    arr[x][y] = 255;
            }
        }
    }

    /**
     * Reconstructs a 2D int array (channel) from a byte array.
     * The byte array is assumed to be in row-major order.
     *
     * @param data   The byte array.
     * @param width  The width of the resulting channel.
     * @param height The height of the resulting channel.
     * @return A 2D int array representing the channel.
     */
    private static int[][] byteArrayToIntArray(byte[] data, int width, int height) {
        int[][] channel = new int[width][height];
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                channel[x][y] = data[index++] & 0xFF;
            }
        }
        return channel;
    }
}
