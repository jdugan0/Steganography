package processors;

import org.jtransforms.fft.DoubleFFT_2D;

import filereader.Image;

public class FourierProcessor implements ImageProcessor {
    private static FourierProcessor instance = new FourierProcessor();

    private FourierProcessor() { instance = this; }

    public FourierProcessor instance() {
        return instance;
    }

    @Override
    public Image encode(Image source, Image encode) {
        return source;
    }
    
    @Override
    public Image decode(Image decode) {
        return decode;
    }

    public static Complex[][] applyFFT(int[][] channel) {
        int width = channel.length;
        int height = channel[0].length;
        DoubleFFT_2D fft = new DoubleFFT_2D(width, height);
        double[][] fftData = new double[width][2 * height]; // 2x height for complex numbers

        // Convert integer data to double for FFT
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                fftData[x][2 * y] = channel[x][y]; // Real part
                fftData[x][2 * y + 1] = 0.0; // Imaginary part
            }
        }

        Complex[][] complexData = new Complex[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double real = fftData[x][2 * y];
                double imag = fftData[x][2 * y + 1];
                complexData[x][y] = new Complex(real, imag);
            }
        }

        return complexData;
    }
}
