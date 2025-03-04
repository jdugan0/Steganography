package filereader;

import java.awt.image.BufferedImage;

import processors.Complex;

public class Image {
    public final int[][] r;
    public final int[][] g;
    public final int[][] b;
    public final int width;
    public final int height;

    public Image(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        r = new int[width][height];
        g = new int[width][height];
        b = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                r[x][y] = (rgb >> 16) & 0xFF;
                g[x][y] = (rgb >> 8) & 0xFF;
                b[x][y] = rgb & 0xFF;
            }
        }
    }

    public Image(Image r, Image g, Image b) {
        this.r = r.r;
        this.g = g.g;
        this.b = b.b;
        this.width = r.width;
        this.height = r.height;
    }

    public Image(int r[][], int g[][], int b[][]) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.width = r.length;
        this.height = r[0].length;
    }

    public static BufferedImage toBufferedImage(Image i) {
        BufferedImage image = new BufferedImage(i.width, i.height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < i.width; x++) {
            for (int y = 0; y < i.height; y++) {
                int r = i.r[x][y] & 0xFF;
                int g = i.g[x][y] & 0xFF;
                int b = i.b[x][y] & 0xFF;

                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    public static Image fftToImage(Complex[][] data) {
        int width = data.length;
        int height = data[0].length;
        double[][] magnitude = new double[width][height];

        // Compute magnitude and find max value
        double maxMagnitude = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                magnitude[x][y] = Math.log(1 + data[x][y].magnitude()); // Log scale
                if (magnitude[x][y] > maxMagnitude) {
                    maxMagnitude = magnitude[x][y]; // Track max value for normalization
                }
            }
        }

        // Normalize and convert to 8-bit grayscale
        int[][] grayscale = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grayscale[x][y] = (int) ((magnitude[x][y] / maxMagnitude) * 255); // Normalize to [0,255]
            }
        }

        // Return as grayscale image (same values in R, G, B)
        return new Image(grayscale, grayscale, grayscale);
    }

    public static Image complexToImage(Complex[][] image) {
        int[][] newI = new int[image.length][image[0].length];
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                int c = (int) Math.round(Math.max(0, Math.min(255, image[x][y].magnitude())));
                newI[x][y] = c;
            }
        }
        return new Image(newI, newI, newI);
    }
}
