package processors;

import filereader.Image;

public class FourierProcessor {
    public static Image encode(Image source, Image encode) {
        return source;
    }

    public static Image decode(Image decode) {
        return decode;
    }

    public static Complex[] oneDimensionalDFT(Complex[] source) {
        int N = source.length;
        Complex[] output = new Complex[N];

        // Initialize output array
        for (int k = 0; k < N; k++) {
            output[k] = new Complex(0, 0);
        }

        for (int k = 0; k < N; k++) {
            for (int n = 0; n < N; n++) {
                double angle = -2 * Math.PI * k * n / N;
                double cosA = Math.cos(angle);
                double sinA = Math.sin(angle);
                double realPart = source[n].real * cosA - source[n].imag * sinA;
                double imagPart = source[n].real * sinA + source[n].imag * cosA;

                // Accumulate in the output
                output[k].real += realPart;
                output[k].imag += imagPart;
            }
        }

        return output;
    }

    public static Complex[][] twoDimensionalDFT(double[][] image) {
        int rows = image.length;
        int cols = image[0].length;

        Complex[][] complexImage = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                complexImage[i][j] = new Complex(image[i][j], 0);
            }
        }

        // compute 1d fft on each row
        Complex[][] rowTransformed = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            rowTransformed[i] = oneDimensionalDFT(complexImage[i]);
        }

        // compute on each col
        Complex[][] finalTransform = new Complex[rows][cols];

        for (int j = 0; j < cols; j++) {

            Complex[] column = new Complex[rows];
            for (int i = 0; i < rows; i++) {
                column[i] = rowTransformed[i][j];
            }

            Complex[] columnDFT = oneDimensionalDFT(column);

            // Store results in the final transformation matrix
            for (int i = 0; i < rows; i++) {
                finalTransform[i][j] = columnDFT[i];
            }
        }

        return finalTransform;
    }
}
