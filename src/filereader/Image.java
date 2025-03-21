package filereader;

import java.awt.image.BufferedImage;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;

public class Image {
    // RGB channels (0-255)
    public final int[][] r;
    public final int[][] g;
    public final int[][] b;
    public final int width;
    public final int height;

    // L*a*b* channels (L in [0,100] and a,b roughly in -128..127)
    public final double[][] labL;
    public final double[][] labA;
    public final double[][] labB;

    // Constructor from a BufferedImage (RGB -> Lab conversion)
    public Image(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        r = new int[width][height];
        g = new int[width][height];
        b = new int[width][height];
        labL = new double[width][height];
        labA = new double[width][height];
        labB = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                r[x][y] = red;
                g[x][y] = green;
                b[x][y] = blue;

                double[] lab = rgbToLab(red, green, blue);
                labL[x][y] = lab[0];
                labA[x][y] = lab[1];
                labB[x][y] = lab[2];
            }
        }
    }

    // Constructor from individual RGB arrays (computes Lab from RGB)
    public Image(int[][] r, int[][] g, int[][] b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.width = r.length;
        this.height = r[0].length;
        labL = new double[width][height];
        labA = new double[width][height];
        labB = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double[] lab = rgbToLab(r[x][y], g[x][y], b[x][y]);
                labL[x][y] = lab[0];
                labA[x][y] = lab[1];
                labB[x][y] = lab[2];
            }
        }
    }

    // Constructor from L*a*b* arrays (computes RGB from Lab)
    public Image(double[][] labL, double[][] labA, double[][] labB) {
        this.labL = labL;
        this.labA = labA;
        this.labB = labB;
        this.width = labL.length;
        this.height = labL[0].length;
        r = new int[width][height];
        g = new int[width][height];
        b = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] rgb = labToRgb(labL[x][y], labA[x][y], labB[x][y]);
                r[x][y] = rgb[0];
                g[x][y] = rgb[1];
                b[x][y] = rgb[2];
            }
        }
    }

    // Existing constructor (example: combining three images) – left unchanged
    public Image(Image r, Image g, Image b) {
        this.r = r.r;
        this.g = g.g;
        this.b = b.b;
        this.width = r.width;
        this.height = r.height;
        // Note: Lab channels could be computed here if desired.
        labL = new double[width][height];
        labA = new double[width][height];
        labB = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double[] lab = rgbToLab(this.r[x][y], this.g[x][y], this.b[x][y]);
                labL[x][y] = lab[0];
                labA[x][y] = lab[1];
                labB[x][y] = lab[2];
            }
        }
    }

    // Convert this image (RGB) to a BufferedImage
    public static BufferedImage toBufferedImage(Image i) {
        BufferedImage image = new BufferedImage(i.width, i.height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < i.width; x++) {
            for (int y = 0; y < i.height; y++) {
                int red = i.r[x][y] & 0xFF;
                int green = i.g[x][y] & 0xFF;
                int blue = i.b[x][y] & 0xFF;
                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    // Getter for the RGB channels: returns a 3D array: [channel][x][y]
    public int[][][] getRGB() {
        return new int[][][] { r, g, b };
    }

    // Getter for the L*a*b* channels: returns a 3D array: [channel][x][y]
    // (Channel 0 = L, 1 = a, 2 = b)
    public double[][][] getLab() {
        return new double[][][] { labL, labA, labB };
    }

    // Optionally, a single method that returns the channels based on a string
    // argument.
    // (You could also use an enum here.)
    public Object getColorSpace(String colorSpace) {
        if (colorSpace.equalsIgnoreCase("RGB")) {
            return getRGB();
        } else if (colorSpace.equalsIgnoreCase("LAB") || colorSpace.equalsIgnoreCase("L*a*b*")) {
            return getLab();
        } else {
            throw new IllegalArgumentException("Unknown color space: " + colorSpace);
        }
    }

    // --- Conversion helper methods ---
    // Convert a single RGB pixel (0-255) to L*a*b*
    private static double[] rgbToLab(int r, int g, int b) {
        // 1. Normalize and linearize sRGB
        double R = r / 255.0;
        double G = g / 255.0;
        double B = b / 255.0;

        R = (R > 0.04045) ? Math.pow((R + 0.055) / 1.055, 2.4) : (R / 12.92);
        G = (G > 0.04045) ? Math.pow((G + 0.055) / 1.055, 2.4) : (G / 12.92);
        B = (B > 0.04045) ? Math.pow((B + 0.055) / 1.055, 2.4) : (B / 12.92);

        // 2. Convert to XYZ (D65)
        double X = R * 0.4124564 + G * 0.3575761 + B * 0.1804375;
        double Y = R * 0.2126729 + G * 0.7151522 + B * 0.0721750;
        double Z = R * 0.0193339 + G * 0.1191920 + B * 0.9503041;

        // 3. Normalize for the D65 white point
        X /= 0.95047;
        // Y is already in the proper scale (Y/1.0)
        Z /= 1.08883;

        // 4. Convert XYZ to L*a*b*
        // Use the standard threshold 0.008856 (≈6/29³)
        double epsilon = 0.008856;
        double fx = (X > epsilon) ? Math.cbrt(X) : (7.787 * X + 16.0 / 116);
        double fy = (Y > epsilon) ? Math.cbrt(Y) : (7.787 * Y + 16.0 / 116);
        double fz = (Z > epsilon) ? Math.cbrt(Z) : (7.787 * Z + 16.0 / 116);

        double L = (116 * fy) - 16;
        double aVal = 500 * (fx - fy);
        double bVal = 200 * (fy - fz);

        return new double[] { L, aVal, bVal };
    }

    // Convert a single L*a*b* pixel to RGB (0-255)
    private static int[] labToRgb(double L, double a, double bVal) {
        // 1. Convert Lab to XYZ
        double fy = (L + 16) / 116.0;
        double fx = fy + a / 500.0;
        double fz = fy - bVal / 200.0;

        double epsilon = 0.008856;
        double X = (fx * fx * fx > epsilon) ? (fx * fx * fx) : ((fx - 16.0 / 116) / 7.787);
        double Y = (L > (epsilon * 903.3)) ? (fy * fy * fy) : (L / 903.3);
        double Z = (fz * fz * fz > epsilon) ? (fz * fz * fz) : ((fz - 16.0 / 116) / 7.787);

        // Denormalize for D65 white point
        X *= 0.95047;
        Z *= 1.08883;

        // 2. Convert XYZ to linear RGB
        double R_lin = X * 3.2404542 + Y * (-1.5371385) + Z * (-0.4985314);
        double G_lin = X * (-0.9692660) + Y * 1.8760108 + Z * 0.0415560;
        double B_lin = X * 0.0556434 + Y * (-0.2040259) + Z * 1.0572252;

        // 3. Apply gamma correction
        double R = (R_lin <= 0.0031308) ? 12.92 * R_lin : (1.055 * Math.pow(R_lin, 1.0 / 2.4) - 0.055);
        double G = (G_lin <= 0.0031308) ? 12.92 * G_lin : (1.055 * Math.pow(G_lin, 1.0 / 2.4) - 0.055);
        double B = (B_lin <= 0.0031308) ? 12.92 * B_lin : (1.055 * Math.pow(B_lin, 1.0 / 2.4) - 0.055);

        // 4. Clamp to [0,1] then scale to [0,255]
        int rVal = (int) Math.round(Math.max(0, Math.min(1, R)) * 255);
        int gVal = (int) Math.round(Math.max(0, Math.min(1, G)) * 255);
        int bInt = (int) Math.round(Math.max(0, Math.min(1, B)) * 255);

        return new int[] { rVal, gVal, bInt };
    }

    // Add this method to your Image class
    public static Image scale(Image image, int newWidth, int newHeight) {
        // Create new arrays for the scaled RGB channels.
        int[][] newR = new int[newWidth][newHeight];
        int[][] newG = new int[newWidth][newHeight];
        int[][] newB = new int[newWidth][newHeight];

        // When newWidth or newHeight is 1, avoid division by zero.
        double scaleX = (newWidth > 1) ? (double) (image.width - 1) / (newWidth - 1) : 0;
        double scaleY = (newHeight > 1) ? (double) (image.height - 1) / (newHeight - 1) : 0;

        for (int nx = 0; nx < newWidth; nx++) {
            // Compute the corresponding x in the source image.
            double x = (newWidth > 1) ? nx * scaleX : 0;
            int x1 = (int) Math.floor(x);
            int x2 = Math.min(x1 + 1, image.width - 1);
            double dx = x - x1;

            for (int ny = 0; ny < newHeight; ny++) {
                // Compute the corresponding y in the source image.
                double y = (newHeight > 1) ? ny * scaleY : 0;
                int y1 = (int) Math.floor(y);
                int y2 = Math.min(y1 + 1, image.height - 1);
                double dy = y - y1;

                // Bilinear interpolation for red channel
                double red = (1 - dx) * (1 - dy) * image.r[x1][y1] +
                        dx * (1 - dy) * image.r[x2][y1] +
                        (1 - dx) * dy * image.r[x1][y2] +
                        dx * dy * image.r[x2][y2];

                // Bilinear interpolation for green channel
                double green = (1 - dx) * (1 - dy) * image.g[x1][y1] +
                        dx * (1 - dy) * image.g[x2][y1] +
                        (1 - dx) * dy * image.g[x1][y2] +
                        dx * dy * image.g[x2][y2];

                // Bilinear interpolation for blue channel
                double blue = (1 - dx) * (1 - dy) * image.b[x1][y1] +
                        dx * (1 - dy) * image.b[x2][y1] +
                        (1 - dx) * dy * image.b[x1][y2] +
                        dx * dy * image.b[x2][y2];

                // Round and clamp the values to valid [0,255] range.
                newR[nx][ny] = (int) Math.round(Math.max(0, Math.min(255, red)));
                newG[nx][ny] = (int) Math.round(Math.max(0, Math.min(255, green)));
                newB[nx][ny] = (int) Math.round(Math.max(0, Math.min(255, blue)));
            }
        }
        // Use the existing constructor that computes Lab channels from RGB arrays.
        return new Image(newR, newG, newB);
    }

    public static double[][] getImageData(Image image) {
        double[][] data = new double[image.width * image.height][3];
        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                data[x * image.height + y][0] = image.r[x][y];
                data[x * image.height + y][1] = image.g[x][y];
                data[x * image.height + y][2] = image.b[x][y];
            }
        }
        return data;
    }

    public static double[][] centerData(double[][] data, double[] means) {
        int n = data.length;
        int dim = data[0].length;

        double[][] centeredData = new double[n][dim];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < dim; j++) {
                centeredData[i][j] = data[i][j] - means[j];
            }
        }
        return centeredData;
    }

    public static double[] getMeans(double[][] data) {
        int n = data.length;
        int dim = data[0].length;

        double[] means = new double[dim];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < dim; j++) {
                means[j] += data[i][j];
            }
        }
        for (int j = 0; j < dim; j++) {
            means[j] /= n;
        }
        return means;
    }

    public static RealMatrix getTransformationMatrix(Image image) {
        double[][] data = getImageData(image);
        double[][] centeredData = centerData(data, getMeans(data));
        RealMatrix matrix = new Array2DRowRealMatrix(centeredData);
        RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
        EigenDecomposition ed = new EigenDecomposition(covarianceMatrix);
        RealVector pc1 = ed.getEigenvector(0);
        RealVector pc2 = ed.getEigenvector(1);
        RealVector pc3 = ed.getEigenvector(2);

        int dim = data[0].length;

        RealMatrix transformationMatrix = new Array2DRowRealMatrix(dim, dim);
        transformationMatrix.setColumnVector(0, pc1);
        transformationMatrix.setColumnVector(1, pc2);
        transformationMatrix.setColumnVector(2, pc3);

        return transformationMatrix;
    }

    public static double[][] applyTransformationMatrix(Image image) {
        RealMatrix transformationMatrix = getTransformationMatrix(image);
        double[][] ogData = getImageData(image);
        double[][] data = centerData(ogData, getMeans(ogData));
        double[][] transformedData = new Array2DRowRealMatrix(data)
                .multiply(transformationMatrix)
                .getData();
        return transformedData;
    }

    public static Image imageFromTransform(double[][] transformedData, RealMatrix transform, double[] means, int width,
            int height) {
        // Inverse transform (note: if transform is orthogonal, its transpose is its
        // inverse)
        double[][] originalData = new Array2DRowRealMatrix(transformedData)
                .multiply(transform.transpose())
                .getData();

        // Allocate Lab channels with the original image dimensions.
        int[][] r = new int[width][height];
        int[][] g = new int[width][height];
        int[][] b = new int[width][height];

        // Reconstruct the Lab channels from the data.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x * height + y;
                r[x][y] = (int) Math.round(Math.max(0, Math.min(originalData[index][0] + means[0], 255)));
                g[x][y] = (int) Math.round(Math.max(0, Math.min(originalData[index][1] + means[1], 255)));
                b[x][y] = (int) Math.round(Math.max(0, Math.min(originalData[index][2] + means[2], 255)));

            }
        }
        return new Image(r, g, b);
    }

    public static Image applyTransformation(Image image) {
        double[][] transformedData = applyTransformationMatrix(image);
        return imageFromTransform(transformedData, getTransformationMatrix(image), getMeans(getImageData(image)),
                image.width, image.height);
    }

}
