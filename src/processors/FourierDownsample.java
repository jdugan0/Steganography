package processors;

import org.jtransforms.fft.DoubleFFT_2D;
import filereader.Image;

public class FourierDownsample {

    private static double[][] toDoubleArray(int[][] channel) {
        // channel[x][y]: x goes up to channel.length, y up to channel[0].length
        int w = channel.length;
        int h = channel[0].length;

        double[][] result = new double[h][2 * w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // Real part at index 2*x, imaginary at 2*x+1
                result[y][2 * x] = channel[x][y];
                result[y][2 * x + 1] = 0.0;
            }
        }
        return result;
    }

    private static int[][] toIntArray(double[][] complexData) {
        int h = complexData.length;
        int w = complexData[0].length / 2;
        int[][] result = new int[w][h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double realVal = complexData[y][2 * x];
                result[x][y] = (int) Math.round(realVal);
            }
        }
        return result;
    }

    public static Image encode(Image storage, Image toEncode) {
        int h = storage.height; // number of rows
        int w = storage.width; // number of columns

        // Convert each RGB channel to double arrays for FFT
        double[][] r = toDoubleArray(storage.r);
        double[][] g = toDoubleArray(storage.g);
        double[][] b = toDoubleArray(storage.b);

        // Perform forward FFT on each channel
        DoubleFFT_2D fft2D = new DoubleFFT_2D(h, w);
        fft2D.complexForward(r);
        fft2D.complexForward(g);
        fft2D.complexForward(b);

        // Replace magnitude with the pixel value from 'toEncode',
        // preserving the phase from the original 'storage' image
        for (int y = 0; y < h / 2; y++) {
            for (int x = 0; x < w / 2; x++) {
                int realIndex = 2 * x;
                int imagIndex = 2 * x + 1;

                // Current phase angles from 'storage'
                double angleR = Math.atan2(r[y][imagIndex], r[y][realIndex]);
                double angleG = Math.atan2(g[y][imagIndex], g[y][realIndex]);
                double angleB = Math.atan2(b[y][imagIndex], b[y][realIndex]);

                // Use the toEncode pixel magnitude
                double magR = toEncode.r[2 * x][2 * y] * 40;
                double magG = toEncode.g[2 * x][2 * y] * 40;
                double magB = toEncode.b[2 * x][2 * y] * 40;

                double magRStorage = Math.sqrt(r[y][realIndex] * r[y][realIndex] + r[y][imagIndex] * r[y][imagIndex]);
                double magGStorage = Math.sqrt(g[y][realIndex] * g[y][realIndex] + g[y][imagIndex] * g[y][imagIndex]);
                double magBStorage = Math.sqrt(b[y][realIndex] * b[y][realIndex] + b[y][imagIndex] * b[y][imagIndex]);

                // System.out.println(magRStorage/magR);

                // Rebuild real + imag using that magnitude and the original phase
                r[y][realIndex] = magR * Math.cos(angleR);
                r[y][imagIndex] = magR * Math.sin(angleR);

                g[y][realIndex] = magG * Math.cos(angleG);
                g[y][imagIndex] = magG * Math.sin(angleG);

                b[y][realIndex] = magB * Math.cos(angleB);
                b[y][imagIndex] = magB * Math.sin(angleB);
            }
        }

        enforceConjugateSymmetry(r, h, w);
        enforceConjugateSymmetry(g, h, w);
        enforceConjugateSymmetry(b, h, w);

        // Inverse FFT to get back to spatial domain
        fft2D.complexInverse(r, true);
        fft2D.complexInverse(g, true);
        fft2D.complexInverse(b, true);

        // Convert the result to integer image channels
        int[][] newR = toIntArray(r);
        int[][] newG = toIntArray(g);
        int[][] newB = toIntArray(b);

        return new Image(newR, newG, newB);
    }

    private static void enforceConjugateSymmetry(double[][] data, int h, int w) {
        // Loop over every frequency coordinate
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mirrorX = (w - x) % w;
                int mirrorY = (h - y) % h;

                // Use lexicographical ordering to update only one of each conjugate pair.
                // (If the mirror index is the same as the current index, we have a
                // self-conjugate bin.)
                if (y < mirrorY || (y == mirrorY && x <= mirrorX)) {
                    if (y == mirrorY && x == mirrorX) {
                        // Self-conjugate bin: force the imaginary part to 0.
                        data[y][2 * x + 1] = 0.0;
                    } else {
                        // Get the current bin’s real and imaginary parts.
                        double real = data[y][2 * x];
                        double imag = data[y][2 * x + 1];
                        // Set the mirror bin to be the complex conjugate.
                        data[mirrorY][2 * mirrorX] = real;
                        data[mirrorY][2 * mirrorX + 1] = -imag;
                    }
                }
            }
        }
    }

    public static Image decode(Image encoded) {
        int h = encoded.height;
        int w = encoded.width;

        // Convert each RGB channel to double arrays for FFT
        double[][] r = toDoubleArray(encoded.r);
        double[][] g = toDoubleArray(encoded.g);
        double[][] b = toDoubleArray(encoded.b);

        // Perform forward FFT on each channel
        DoubleFFT_2D fft2D = new DoubleFFT_2D(h, w);
        fft2D.complexForward(r);
        fft2D.complexForward(g);
        fft2D.complexForward(b);

        int[][] newR = new int[w / 2][h / 2];
        int[][] newG = new int[w / 2][h / 2];
        int[][] newB = new int[w / 2][h / 2];

        for (int y = 0; y < h / 2; y++) {
            for (int x = 0; x < w / 2; x++) {
                int realIndex = 2 * x;
                int imagIndex = 2 * x + 1;

                int magRStorage = (int) (Math
                        .sqrt(r[y][realIndex] * r[y][realIndex] + r[y][imagIndex] * r[y][imagIndex]) / 40.0);
                int magGStorage = (int) (Math
                        .sqrt(g[y][realIndex] * g[y][realIndex] + g[y][imagIndex] * g[y][imagIndex]) / 40.0);
                int magBStorage = (int) (Math
                        .sqrt(b[y][realIndex] * b[y][realIndex] + b[y][imagIndex] * b[y][imagIndex]) / 40.0);

                newR[x][y] = magRStorage;
                newG[x][y] = magGStorage;
                newB[x][y] = magBStorage;
            }
        }

        return new Image(newR, newG, newB);
    }
}
