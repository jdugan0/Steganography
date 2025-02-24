package filereader;

import java.awt.image.BufferedImage;

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

}
