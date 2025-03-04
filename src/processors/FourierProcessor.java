package processors;

import org.jtransforms.fft.DoubleFFT_2D;

import filereader.FileReader;
import filereader.Image;
import filereader.FileReader.ImageType;

public class FourierProcessor implements ImageProcessor {
    private static FourierProcessor instance = new FourierProcessor();

    public FourierProcessor() {
        FourierProcessor.instance = this;
    }

    public FourierProcessor instance() {
        return FourierProcessor.instance;
    }

    
    @Override
    public Image encode(Image source, Image encode) {
        return encode(source, encode, 5, 4); // using defaults from main
    }

    public Image encode(Image source, Image encode, int scale, double factor) {
        Complex[][] sourceFFTR = applyFFT(source.r);
        Complex[][] sourceFFTG = applyFFT(source.g);
        Complex[][] sourceFFTB = applyFFT(source.b);

        Image debug1 = new Image(Image.fftToImage(sourceFFTR), Image.fftToImage(sourceFFTG),
                Image.fftToImage(sourceFFTB));

        FileReader.writeImage(Image.toBufferedImage(debug1), ImageType.Debug, "sourceFFT.png");

        Complex[][] encodeFFTR = applyFFT(encode.r);
        Complex[][] encodeFFTG = applyFFT(encode.g);
        Complex[][] encodeFFTB = applyFFT(encode.b);

        int width = source.width;
        int height = source.height;

        Complex[][] fftGScaled = new Complex[width / scale][height / scale];
        Complex[][] fftRScaled = new Complex[width / scale][height / scale];
        Complex[][] fftBScaled = new Complex[width / scale][height / scale];

        for (int i = 0; i < width; i += scale) {
            for (int j = 0; j < height; j += scale) {
                fftRScaled[i / scale][j / scale] = encodeFFTR[i][j];
                fftGScaled[i / scale][j / scale] = encodeFFTG[i][j];
                fftBScaled[i / scale][j / scale] = encodeFFTB[i][j];
            }
        }

        for (int i = 0; i < width / scale; i++) {
            for (int j = 0; j < height / scale; j++) {
                sourceFFTR[i + width / 4 - width / scale / 2][j + height / 4 - height / scale / 2] = fftRScaled[i][j]
                        .divide(factor);
                sourceFFTG[i + width / 4 - width / scale / 2][j + height / 4 - height / scale / 2] = fftGScaled[i][j]
                        .divide(factor);
                sourceFFTB[i + width / 4 - width / scale / 2][j + height / 4 - height / scale / 2] = fftBScaled[i][j]
                        .divide(factor);

                sourceFFTR[i + 3 * width / 4 - width / scale / 2][j + 3 * height / 4
                        - height / scale / 2] = fftRScaled[i][j]
                                .divide(factor).conjugate();
                sourceFFTG[i + 3 * width / 4 - width / scale / 2][j + 3 * height / 4
                        - height / scale / 2] = fftGScaled[i][j]
                                .divide(factor).conjugate();
                sourceFFTB[i + 3 * width / 4 - width / scale / 2][j + 3 * height / 4
                        - height / scale / 2] = fftBScaled[i][j]
                                .divide(factor).conjugate();
            }
        }

        Image debug2 = new Image(Image.fftToImage(sourceFFTR), Image.fftToImage(sourceFFTG),
                Image.fftToImage(sourceFFTB));

        FileReader.writeImage(Image.toBufferedImage(debug2), ImageType.Debug, "encodedFFT.png");

        Image redNew = Image.complexToImage(applyIFFT(sourceFFTR));
        Image greenNew = Image.complexToImage(applyIFFT(sourceFFTG));
        Image blueNew = Image.complexToImage(applyIFFT(sourceFFTB));

        return new Image(redNew, greenNew, blueNew);
    }

    @Override
    public Image decode(Image decode) {
        return decode(decode, 5, 4); // using defaults from main
    }

    public Image decode(Image encoded, int scale, double factor) {

        int width = encoded.width;
        int height = encoded.height;

        Complex[][] decodeFFTR = applyFFT(encoded.r);
        Complex[][] decodeFFTG = applyFFT(encoded.g);
        Complex[][] decodeFFTB = applyFFT(encoded.b);

        Image debug2 = new Image(Image.fftToImage(decodeFFTR), Image.fftToImage(decodeFFTG),
                Image.fftToImage(decodeFFTB));
        FileReader.writeImage(Image.toBufferedImage(debug2), ImageType.Debug, "decodedFFT.png");

        Complex[][] fftRScaled = new Complex[width / scale][height / scale];
        Complex[][] fftGScaled = new Complex[width / scale][height / scale];
        Complex[][] fftBScaled = new Complex[width / scale][height / scale];

        int offsetX = width / 4 - (width / scale) / 2;
        int offsetY = height / 4 - (height / scale) / 2;
        for (int i = 0; i < width / scale; i++) {
            for (int j = 0; j < height / scale; j++) {
                fftRScaled[i][j] = decodeFFTR[i + offsetX][j + offsetY].times(factor);
                fftGScaled[i][j] = decodeFFTG[i + offsetX][j + offsetY].times(factor);
                fftBScaled[i][j] = decodeFFTB[i + offsetX][j + offsetY].times(factor);
            }
        }

        Image debug1 = new Image(Image.fftToImage(fftRScaled), Image.fftToImage(fftGScaled),
                Image.fftToImage(fftBScaled));
        FileReader.writeImage(Image.toBufferedImage(debug1), ImageType.Debug, "hiddenFFT.png");

        Image redNew = Image.complexToImage(applyIFFT(fftRScaled));
        Image greenNew = Image.complexToImage(applyIFFT(fftGScaled));
        Image blueNew = Image.complexToImage(applyIFFT(fftBScaled));

        return new Image(redNew, greenNew, blueNew);
    }

    public static Complex[][] applyFFT(int[][] channel) {
        int width = channel.length;
        int height = channel[0].length;

        // 1) Prepare double[][] array of size [width][2*height]
        double[][] data = new double[width][2 * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][2 * j] = channel[i][j]; // real
                data[i][2 * j + 1] = 0.0; // imaginary
            }
        }

        // 2) Perform the FFT
        DoubleFFT_2D fft = new DoubleFFT_2D(width, height);
        fft.complexForward(data);

        // 3) Convert to Complex[][]
        Complex[][] spectrum = new Complex[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double re = data[i][2 * j];
                double im = data[i][2 * j + 1];
                spectrum[i][j] = new Complex(re, im);
            }
        }

        // 4) Shift the spectrum
        Complex[][] shifted = fftShift(spectrum);

        return shifted;
    }

    public static Complex[][] fftShift(Complex[][] spectrum) {
        int w = spectrum.length;
        int h = spectrum[0].length;

        Complex[][] shifted = new Complex[w][h];

        // For each row, we shift up or down by w/2
        // For each column, we shift left or right by h/2
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                // Wrap index (i + w/2) mod w
                int newI = (i + w / 2) % w;
                // Wrap index (j + h/2) mod h
                int newJ = (j + h / 2) % h;

                shifted[newI][newJ] = spectrum[i][j];
            }
        }
        return shifted;
    }

    public static Complex[][] applyIFFT(Complex[][] spectrum) {
        Complex[][] shifted = fftShift(spectrum);
        int width = spectrum.length;
        int height = spectrum[0].length;

        // 1) Prepare double[][] array of size [width][2*height]
        double[][] data = new double[width][2 * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][2 * j] = shifted[i][j].real;
                data[i][2 * j + 1] = shifted[i][j].imag;
            }
        }

        // 2) Perform the IFFT
        DoubleFFT_2D ifft = new DoubleFFT_2D(width, height);
        ifft.complexInverse(data, true);

        Complex[][] channel = new Complex[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                channel[i][j] = new Complex(data[i][j * 2], data[i][j * 2 + 1]);
            }
        }

        return channel;
    }
}