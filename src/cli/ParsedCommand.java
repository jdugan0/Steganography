package cli;

public class ParsedCommand {
    private boolean fourier;
    private boolean encode;

    private ParsedCommand(boolean fourier, boolean encode) {
        this.fourier = fourier;
        this.encode = encode;
    }

    public static ParsedCommand parse(String command) throws IllegalArgumentException {
        String[] segments = command.split(" ");
        boolean fourier;
        boolean encode;
        if (segments[0].equals("fourier")) {
            fourier = true;
        } else if (segments[0].equals("bitnoise")) {
            fourier = false;
        } else {
            throw new IllegalArgumentException();
        }
        if (segments[1].equals("encode")) {
            encode = true;
        } else if (segments[1].equals("decode")) {
            encode = false;
        } else {
            throw new IllegalArgumentException();
        }
        return new ParsedCommand(fourier, encode);
    }

    public boolean isFourier() {
        return fourier;
    }

    public boolean isEncode() {
        return encode;
    }
}
