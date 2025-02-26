package cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cli {
  private Path encode;
  private Path source;
  private Path decode;
  private Path residuals;
  private Path output;

  public Cli(String encode, String source, String decode, String residuals, String output) {
    this.encode = Paths.get(encode);
    this.source = Paths.get(source);
    this.decode = Paths.get(decode);
    this.residuals = Paths.get(residuals);
    this.output = Paths.get(output);
  }

  public void start() {
    try {
      EncodeMonitor monitor = new EncodeMonitor(encode, source);
      new Thread(monitor).start();
    } catch (IOException e) {
      System.out.println("i/o exception");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}