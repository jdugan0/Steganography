import java.awt.image.BufferedImage;
import java.util.Arrays;

import filereader.FileReader;
import filereader.Image;
import filereader.FileReader.ImageType;
import processors.Complex;
import processors.FourierProcessor;

public class Main {
    public static void main(String[] args) {
        Image i = FileReader.readImage(ImageType.Encode, "encode.png");

        int[][] intArray = i.r;
        double[][] doubleArray = new double[intArray.length][intArray[0].length];
        for (int x = 0; x < intArray.length; x++) {
            for (int y = 0; y < intArray[x].length; y++) {
                doubleArray[x][y] = (double) intArray[x][y];
            }
        }
        Complex[][] c = FourierProcessor.twoDimensionalDFT(doubleArray);
        Image r = Image.displayFourier(c);
        BufferedImage b = Image.toBufferedImage(r);

        FileReader.writeImage(b, ImageType.Decode, "decode.png");
    }
}