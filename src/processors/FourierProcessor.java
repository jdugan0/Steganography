package processors;

import filereader.Image;

public class FourierProcessor {
    public static Image encode(Image source, Image encode) {
        return source;
    }

    public static Image decode(Image decode) {
        return decode;
    }

    public static double[][] applyFFT(int[][] channel) {
        int width = channel.length;
        int height = channel[0].length;
        Complex[][] fftData = new Complex[width][height];
        // Convert integer data to double for FFT
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                
            }
        }

        // Apply 2D FFT
        DoubleFFT_2D fft = new DoubleFFT_2D(width, height);
        fft.complexForward(fftData);

        return fftData;
    }
}
