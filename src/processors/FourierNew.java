package processors;

import org.jtransforms.fft.DoubleFFT_2D;
import filereader.Image;

public class FourierNew implements ImageProcessor {
    private static FourierNew instance = new FourierNew();

    private FourierNew() {
        FourierNew.instance = this;
    }

    public static FourierNew instance() {
        return instance;
    }

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

    @Override
    public Image encode(Image storage, Image toEncode) {
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
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                // if (x < w / 8)
                //     continue;
                // if (y < h / 8)
                //     continue;
                int realIndex = 2 * x;
                int imagIndex = 2 * x + 1;

                // Current phase angles from 'storage'
                double angleR = Math.atan2(r[y][imagIndex], r[y][realIndex]);
                double angleG = Math.atan2(g[y][imagIndex], g[y][realIndex]);
                double angleB = Math.atan2(b[y][imagIndex], b[y][realIndex]);

                // Use the toEncode pixel magnitude
                double magR = toEncode.r[x][y] * 40;
                double magG = toEncode.g[x][y] * 40;
                double magB = toEncode.b[x][y] * 40;

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

    @Override
    public Image decode(Image encoded) {
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

        int[][] newR = new int[w][h];
        int[][] newG = new int[w][h];
        int[][] newB = new int[w][h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // if (x < w / 8)
                //     continue;
                // if (y < h / 8)
                //     continue;
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
