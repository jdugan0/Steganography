package filereader;

import java.awt.image.BufferedImage;

public class Image {
    public final int[][] r;
    public final int[][] g;
    public final int[][] b;
    public final int width;
    public final int height;

    public Image(BufferedImage image){
        width = image.getWidth();
        height = image.getHeight();
        r = new int[width][height];
        g = new int[width][height];
        b = new int[width][height];

        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                int rgb = image.getRGB(x, y);
                r[x][y] = (rgb >> 16) & 0xFF;
                g[x][y] = (rgb >> 8) & 0xFF;
                b[x][y] = rgb & 0xFF;
            }
        }
    }
}
