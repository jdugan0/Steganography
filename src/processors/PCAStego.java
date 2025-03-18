package processors;

import java.io.FileWriter;

import filereader.FileReader;
import filereader.Image;
import filereader.FileReader.ImageType;

public class PCAStego implements ImageProcessor {

    private static PCAStego instance = new PCAStego();
    private static double scale = 0.01;

    private PCAStego() {
        PCAStego.instance = this;
    }

    public static PCAStego instance() {
        return PCAStego.instance;
    }

    @Override
    public Image decode(Image decode) {
        double[][] pcaDecode = Image.applyTransformationMatrix(decode);
        double[] means = Image.getMeans(Image.getImageData(decode));

        for (int x = 0; x < pcaDecode.length; x++) {
            pcaDecode[x][0] = pcaDecode[x][2] / scale;
            pcaDecode[x][1] = 0;
            pcaDecode[x][2] = 0;
        }

        return Image.imageFromTransform(pcaDecode,
                Image.getTransformationMatrix(decode), means, decode.width,
                decode.height);
    }

    @Override
    public Image encode(Image source, Image encode) {
        // return Image.applyTransformation(source);
        double[][] pcaSource = Image.applyTransformationMatrix(source);
        double[][] pcaEncode = Image.applyTransformationMatrix(encode);

        double[] means = Image.getMeans(Image.getImageData(source));

        double[][] pcaHigh = new double[pcaSource.length][3];
        double[][] pcaMid = new double[pcaSource.length][3];
        double[][] pcaLow = new double[pcaSource.length][3];

        for (int x = 0; x < pcaSource.length; x++) {
            pcaHigh[x][0] = pcaSource[x][0];
            pcaMid[x][1] = pcaSource[x][1];
            pcaLow[x][2] = pcaSource[x][2];
            pcaSource[x][2] = pcaEncode[x][0] * scale;
        }

        FileReader.writeImage(Image.toBufferedImage(Image.imageFromTransform(pcaHigh,
                Image.getTransformationMatrix(source), means, source.width,
                source.height)), ImageType.Debug, "pca/pcaHigh.png");

        FileReader.writeImage(Image.toBufferedImage(Image.imageFromTransform(pcaMid,
                Image.getTransformationMatrix(source), means, source.width,
                source.height)), ImageType.Debug, "pca/pcaMid.png");

        FileReader.writeImage(Image.toBufferedImage(Image.imageFromTransform(pcaLow,
                Image.getTransformationMatrix(source), means, source.width,
                source.height)), ImageType.Debug, "pca/pcaLow.png");

        return Image.imageFromTransform(pcaSource,
                Image.getTransformationMatrix(source), means, source.width,
                source.height);
    }

}
