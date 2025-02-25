package processors;

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
}
