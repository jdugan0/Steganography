package processors;

import org.jtransforms.dct.DoubleDCT_2D;
import filereader.FileReader;
import filereader.Image;
import filereader.FileReader.ImageType;

public class DCTStego implements ImageProcessor {
    private static DCTStego instance = new DCTStego();

    private DCTStego() {
        DCTStego.instance = this;
    }

    public static DCTStego instance() {
        return DCTStego.instance;
    }

    public static int crop = 32;
    public static double alpha = 1;
    public static double scale = 1;

    private static double[][] toDoubleArray(int[][] channel) {
        int w = channel.length;
        int h = channel[0].length;

        double[][] result = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                result[x][y] = channel[x][y];
            }
        }
        return result;
    }

    private static int[][] toIntArray(double[][] data) {
        int w = data.length;
        int h = data[0].length;
        int[][] result = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                result[x][y] = (int) Math.round(data[x][y]);
            }
        }
        return result;
    }

    @Override
    public Image encode(Image storage, Image toEncode) {
        int h = storage.height;
        int w = storage.width;

        // Convert image channels to double arrays
        double[][] r = storage.labL;
        double[][] g = storage.labA;
        double[][] b = storage.labB;

        // Perform forward DCT
        DoubleDCT_2D dct2D = new DoubleDCT_2D(w, h);
        dct2D.forward(r, true);
        dct2D.forward(g, true);
        dct2D.forward(b, true);

        Image scaled = Image.scale(toEncode, w, h);

        // Modify DCT coefficients
        for (int y = h / crop; y < h; y++) {
            for (int x = w / crop; x < w; x++) {
                r[x][y] = alpha * (scaled.labL[x][y] * scale) + (1 - alpha) * r[x][y];
                g[x][y] = alpha * (scaled.labA[x][y] * scale) + (1 - alpha) * g[x][y];
                b[x][y] = alpha * (scaled.labB[x][y] * scale) + (1 - alpha) * b[x][y];
                // System.out.println(scaled.labB[x][y] / b[x][y]);
            }
        }

        // Perform inverse DCT
        dct2D.inverse(r, true);
        dct2D.inverse(g, true);
        dct2D.inverse(b, true);

        return new Image(r, g, b);
    }

    @Override
    public Image decode(Image encoded) {
        int h = encoded.height;
        int w = encoded.width;

        // Convert image channels to double arrays
        double[][] r = encoded.labL;
        double[][] g = (encoded.labA);
        double[][] b = (encoded.labB);

        // Perform forward DCT
        DoubleDCT_2D dct2D = new DoubleDCT_2D(w, h);
        dct2D.forward(r, true);
        dct2D.forward(g, true);
        dct2D.forward(b, true);

        double[][] newR = new double[w - w / crop][h - h / crop];
        double[][] newG = new double[w - w / crop][h - h / crop];
        double[][] newB = new double[w - w / crop][h - h / crop];

        for (int y = h / crop; y < h; y++) {
            for (int x = w / crop; x < w; x++) {
                newR[x - w / crop][y - h / crop] = r[x][y] / scale / alpha;
                newG[x - w / crop][y - h / crop] = g[x][y] / scale / alpha;
                newB[x - w / crop][y - h / crop] = b[x][y] / scale / alpha;
            }
        }

        return new Image(newR, newG, newB);
    }
}
