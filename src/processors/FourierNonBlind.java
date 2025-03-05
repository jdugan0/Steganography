package processors;

import filereader.Image;
import org.jtransforms.fft.DoubleFFT_2D;

/**
 * Improved frequency-domain steganography.
 *
 * For each color channel the secret is embedded only in the high-frequency region.
 * In that region the store FFT is blended with the secret FFT:
 *
 *      storeFFT = (1 - factor) * storeFFT + factor * secretFFT.
 *
 * During decode the secret is recovered (in the high-frequency region) by:
 *
 *      secretFFT = (encodedFFT - (1 - factor) * origStoreFFT) / factor.
 *
 * Low-frequency coefficients are left untouched to preserve image quality.
 */
public class FourierNonBlind {

    // We'll store the FFT of the original "store" image so we can subtract it out in decode.
    private static double[][] storeRFFT;
    private static double[][] storeGFFT;
    private static double[][] storeBFFT;

    // Keep track of the dimensions of the store image
    private static int storeWidth;
    private static int storeHeight;

    /**
     * Encodes the secret image into the store image using JTransforms 2D FFT.
     * The embedding is performed only in the high-frequency region (based on a radial threshold)
     * for each color channel.
     *
     * @param store  The cover (store) image.
     * @param secret The secret image (must be the same dimensions as the store).
     * @param factor The embedding strength (0 to 1). At 0 no secret is embedded; at 1 the secret fully replaces the store in the high frequencies.
     * @return       The encoded (stego) image.
     */
    public static Image encode(Image store, Image secret, double factor) {
        // Save original store dimensions
        storeWidth  = store.width;
        storeHeight = store.height;
        
        if (store.width != secret.width || store.height != secret.height) {
            throw new IllegalArgumentException("Store and secret images must have the same dimensions.");
        }

        // Prepare the doubleFFT for these dimensions
        DoubleFFT_2D fft2D = new DoubleFFT_2D(storeHeight, storeWidth);

        // --- 1) Forward FFT on the store image for each channel ---
        // Convert each channel to interleaved double[][] (shape: [height][2*width])
        double[][] storeR = toDoubleArray(store.r);
        double[][] storeG = toDoubleArray(store.g);
        double[][] storeB = toDoubleArray(store.b);

        // Forward FFT (in-place)
        fft2D.complexForward(storeR);
        fft2D.complexForward(storeG);
        fft2D.complexForward(storeB);

        // Save deep copies for use in decode
        storeRFFT = deepCopy(storeR);
        storeGFFT = deepCopy(storeG);
        storeBFFT = deepCopy(storeB);

        // --- 2) Forward FFT on the secret image for each channel ---
        double[][] secretR = toDoubleArray(secret.r);
        double[][] secretG = toDoubleArray(secret.g);
        double[][] secretB = toDoubleArray(secret.b);

        fft2D.complexForward(secretR);
        fft2D.complexForward(secretG);
        fft2D.complexForward(secretB);

        // --- 3) Embed: blend storeFFT with secretFFT in high-frequency regions only ---
        blendFFT(storeR, secretR, factor);
        blendFFT(storeG, secretG, factor);
        blendFFT(storeB, secretB, factor);

        // --- 4) Inverse FFT to get the encoded image ---
        fft2D.complexInverse(storeR, true);
        fft2D.complexInverse(storeG, true);
        fft2D.complexInverse(storeB, true);

        // Convert back to int[][] and clamp to [0,255]
        int[][] encodedR = toIntArray(storeR);
        int[][] encodedG = toIntArray(storeG);
        int[][] encodedB = toIntArray(storeB);

        clampToByteRange(encodedR);
        clampToByteRange(encodedG);
        clampToByteRange(encodedB);

        // Return the final encoded image
        return new Image(encodedR, encodedG, encodedB);
    }

    /**
     * Decodes the secret image from the encoded image by subtracting out the stored FFT
     * (saved during encode) and then reversing the blending in the high-frequency regions.
     *
     * @param encoded The encoded (stego) image.
     * @param factor  The embedding strength used in encode.
     * @return        The recovered secret image.
     */
    public static Image decode(Image encoded, double factor) {
        // Ensure dimensions match
        if (encoded.width != storeWidth || encoded.height != storeHeight) {
            throw new RuntimeException("Encoded image dimensions differ from store image dimensions!");
        }

        DoubleFFT_2D fft2D = new DoubleFFT_2D(storeHeight, storeWidth);

        // --- 1) Forward FFT of the encoded image ---
        double[][] encR = toDoubleArray(encoded.r);
        double[][] encG = toDoubleArray(encoded.g);
        double[][] encB = toDoubleArray(encoded.b);

        fft2D.complexForward(encR);
        fft2D.complexForward(encG);
        fft2D.complexForward(encB);

        // --- 2) Extract secret FFT coefficients in the high-frequency region ---
        extractFFT(encR, storeRFFT, factor);
        extractFFT(encG, storeGFFT, factor);
        extractFFT(encB, storeBFFT, factor);

        // --- 3) Inverse FFT to recover secret image channels ---
        fft2D.complexInverse(encR, true);
        fft2D.complexInverse(encG, true);
        fft2D.complexInverse(encB, true);

        // Convert to int[][] and clamp
        int[][] secretR = toIntArray(encR);
        int[][] secretG = toIntArray(encG);
        int[][] secretB = toIntArray(encB);

        clampToByteRange(secretR);
        clampToByteRange(secretG);
        clampToByteRange(secretB);

        return new Image(secretR, secretG, secretB);
    }

    // -------------------------------------------------------------------------
    //                         Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Converts an int[][] (channel data) to a double[][] with interleaved complex format.
     * The resulting array has shape [height][2*width]: real parts at index 2*x, imaginary parts at 2*x+1.
     */
    private static double[][] toDoubleArray(int[][] channel) {
        int w = channel.length;
        int h = channel[0].length;
        // We create an array with shape [h][2*w] (treating x as column, y as row)
        double[][] result = new double[h][2 * w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double val = channel[x][y];
                result[y][2 * x]     = val;  // real
                result[y][2 * x + 1] = 0.0;  // imaginary
            }
        }
        return result;
    }

    /**
     * Converts the interleaved double[][] (from JTransforms) back to an int[][].
     * Only the real part is used.
     */
    private static int[][] toIntArray(double[][] complexData) {
        int h = complexData.length;
        int w = complexData[0].length / 2;
        int[][] result = new int[w][h]; // [width][height]
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double realVal = complexData[y][2 * x];
                int intVal = (int) Math.round(realVal);
                result[x][y] = intVal;
            }
        }
        return result;
    }

    /**
     * Blends the secret FFT into the store FFT for frequencies in the high-frequency region.
     * For a given frequency coefficient, if its distance from the center exceeds a threshold,
     * we replace the store coefficient with:
     *      (1 - factor)*store + factor*secret.
     *
     * This is applied in-place.
     */
    private static void blendFFT(double[][] storeFFT, double[][] secretFFT, double factor) {
        int h = storeFFT.length;
        int width = storeFFT[0].length / 2; // number of complex numbers per row
        double centerX = width / 2.0;
        double centerY = h / 2.0;
        double maxDist = Math.sqrt(centerX * centerX + centerY * centerY);
        double threshold = 0.5; // embed only if distance > 50% of max distance (i.e. high frequencies)

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < width; x++) {
                double dx = x - centerX;
                double dy = y - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > threshold * maxDist) {
                    int idx = 2 * x;
                    // Blend the real and imaginary parts
                    storeFFT[y][idx]     = (1 - factor) * storeFFT[y][idx]     + factor * secretFFT[y][idx];
                    storeFFT[y][idx + 1] = (1 - factor) * storeFFT[y][idx + 1] + factor * secretFFT[y][idx + 1];
                }
            }
        }
    }

    /**
     * Extracts the secret FFT from the encoded FFT by reversing the blending in the high-frequency region.
     * For coefficients in the high-frequency region, compute:
     *      secret = (encoded - (1 - factor) * origStore) / factor.
     *
     * Outside the embedding region, set the secret coefficient to zero.
     * This is done in-place on encodedFFT.
     */
    private static void extractFFT(double[][] encodedFFT, double[][] origStoreFFT, double factor) {
        int h = encodedFFT.length;
        int width = encodedFFT[0].length / 2;
        double centerX = width / 2.0;
        double centerY = h / 2.0;
        double maxDist = Math.sqrt(centerX * centerX + centerY * centerY);
        double threshold = 0.5;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < width; x++) {
                int idx = 2 * x;
                double dx = x - centerX;
                double dy = y - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > threshold * maxDist) {
                    encodedFFT[y][idx]     = (encodedFFT[y][idx]     - (1 - factor) * origStoreFFT[y][idx])     / factor;
                    encodedFFT[y][idx + 1] = (encodedFFT[y][idx + 1] - (1 - factor) * origStoreFFT[y][idx + 1]) / factor;
                } else {
                    // Outside the embedding region, we assume no secret was embedded.
                    encodedFFT[y][idx] = 0;
                    encodedFFT[y][idx + 1] = 0;
                }
            }
        }
    }

    /**
     * Clamps the values of a 2D int array into the range [0, 255].
     */
    private static void clampToByteRange(int[][] arr) {
        for (int x = 0; x < arr.length; x++) {
            for (int y = 0; y < arr[x].length; y++) {
                if (arr[x][y] < 0)   arr[x][y] = 0;
                if (arr[x][y] > 255) arr[x][y] = 255;
            }
        }
    }

    /**
     * Makes a deep copy of a 2D double array.
     */
    private static double[][] deepCopy(double[][] original) {
        double[][] copy = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}