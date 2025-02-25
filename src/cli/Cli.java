package cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cli {
  private Path encode;
  private Path decode;
  private Path residuals;
  private Path output;

  public Cli(String encode, String decode, String residuals, String output) {
    this.encode = Paths.get(encode);
    this.decode = Paths.get(decode);
    this.residuals = Paths.get(residuals);
    this.output = Paths.get(output);
  }

  public void start() {
    try {
      EncodeMonitor monitor = new EncodeMonitor(encode);
      new Thread(monitor).start();
    } catch (IOException e) {
      System.out.println("i/o exception");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}