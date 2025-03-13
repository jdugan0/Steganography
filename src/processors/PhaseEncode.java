package processors;

import org.jtransforms.fft.DoubleFFT_2D;
import filereader.Image;

public class PhaseEncode {

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

        double[][] rEncode = toDoubleArray(toEncode.r);
        double[][] gEncode = toDoubleArray(toEncode.g);
        double[][] bEncode = toDoubleArray(toEncode.b);

        // Perform forward FFT on each channel
        DoubleFFT_2D fft2D = new DoubleFFT_2D(h, w);
        fft2D.complexForward(r);
        fft2D.complexForward(g);
        fft2D.complexForward(b);

        fft2D.complexForward(rEncode);
        fft2D.complexForward(gEncode);
        fft2D.complexForward(bEncode);

        // Replace magnitude with the pixel value from 'toEncode',
        // preserving the phase from the original 'storage' image
        for (int y = h/8; y < h; y++) {
            for (int x = w/8; x < w; x++) {
                int realIndex = 2 * (x);
                int imagIndex = 2 * (x) + 1;

                // Current phase angles from 'storage'
                double angleR = Math.atan2(r[y][imagIndex], r[y][realIndex]);
                double angleG = Math.atan2(g[y][imagIndex], g[y][realIndex]);
                double angleB = Math.atan2(b[y][imagIndex], b[y][realIndex]);

                double angleREncode = Math.atan2(rEncode[y - h/8][imagIndex - w/4], rEncode[y - h/8][realIndex - w/4]);
                double angleGEncode = Math.atan2(gEncode[y - h/8][imagIndex - w/4], gEncode[y - h/8][realIndex - w/4]);
                double angleBEncode = Math.atan2(bEncode[y - h/8][imagIndex - w/4], bEncode[y - h/8][realIndex - w/4]);

                double magRStorage = Math.sqrt(r[y][realIndex] * r[y][realIndex] + r[y][imagIndex] * r[y][imagIndex]);
                double magGStorage = Math.sqrt(g[y][realIndex] * g[y][realIndex] + g[y][imagIndex] * g[y][imagIndex]);
                double magBStorage = Math.sqrt(b[y][realIndex] * b[y][realIndex] + b[y][imagIndex] * b[y][imagIndex]);

                double scale = 10000;

                // System.out.println(magRStorage/(angleREncode * scale));

                
                r[y][realIndex] = angleREncode * Math.cos(angleR) * scale;
                r[y][imagIndex] = angleREncode * Math.sin(angleR) * scale;

                g[y][realIndex] = angleGEncode * Math.cos(angleG) * scale;
                g[y][imagIndex] = angleGEncode * Math.sin(angleG) * scale;

                b[y][realIndex] = angleBEncode * Math.cos(angleB) * scale;
                b[y][imagIndex] = angleBEncode * Math.sin(angleB) * scale;
            }
        }

        // enforceConjugateSymmetry(r, h, w);
        // enforceConjugateSymmetry(g, h, w);
        // enforceConjugateSymmetry(b, h, w);

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

        double[][] rNew = new double[h][w*2];
        double[][] gNew = new double[h][w*2];
        double[][] bNew = new double[h][w*2];

        // Perform forward FFT on each channel
        DoubleFFT_2D fft2D = new DoubleFFT_2D(h, w);
        fft2D.complexForward(r);
        fft2D.complexForward(g);
        fft2D.complexForward(b);

        for (int y = h/8; y < h / 2; y++) {
            for (int x = w/8; x < w / 2; x++) {
                int realIndex = 2 * x;
                int imagIndex = 2 * x + 1;

                double scale = 10000;

                double magRStorage = (Math
                        .sqrt(r[y][realIndex] * r[y][realIndex] + r[y][imagIndex] * r[y][imagIndex]) / scale);
                double magGStorage = (Math
                        .sqrt(g[y][realIndex] * g[y][realIndex] + g[y][imagIndex] * g[y][imagIndex]) / scale);
                double magBStorage = (Math
                        .sqrt(b[y][realIndex] * b[y][realIndex] + b[y][imagIndex] * b[y][imagIndex]) / scale);

                rNew[y - h/8][realIndex - x/4] = Math.cos(magRStorage) * magRStorage * scale;
                rNew[y - h/8][imagIndex - x/4] = Math.sin(magRStorage) * magRStorage * scale;

                gNew[y - h/8][realIndex - x/4] = Math.cos(magGStorage) * magGStorage * scale;
                gNew[y - h/8][imagIndex - x/4] = Math.sin(magGStorage) * magGStorage * scale;

                bNew[y - h/8][realIndex - x/4] = Math.cos(magBStorage) * magBStorage * scale;
                bNew[y - h/8][imagIndex - x/4] = Math.sin(magBStorage) * magBStorage * scale;
            }
        }

        fft2D.complexInverse(rNew, true);
        fft2D.complexInverse(bNew, true);
        fft2D.complexInverse(gNew, true);

        return new Image(toIntArray(rNew), toIntArray(gNew), toIntArray(bNew));
    }
}
