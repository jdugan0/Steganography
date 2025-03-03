package processors;

public class Complex {
    public double real;
    public double imag;

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public double magnitude() {
        return Math.sqrt(real * real + imag * imag);
    }

    public Complex divide(double d) {
        return new Complex(real / d, imag / d);
    }

    public Complex times(double x) {
        return new Complex(real * x, imag * x);
    }
}
