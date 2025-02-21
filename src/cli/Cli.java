package cli;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

public class Cli {
  private Path encode;
  private Path decode;
  private Path residuals;
  private Path output;
  private FileSystem fileSystem;

  public Cli(String encode, String decode, String residuals, String output) {
    this.encode = Paths.get(encode);
    this.decode = Paths.get(decode);
    this.residuals = Paths.get(residuals);
    this.output = Paths.get(output);
    this.fileSystem = this.encode.getFileSystem();
  }

  public void start() {
    try (WatchService watchService = fileSystem.newWatchService()) {
        encode.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    } catch (IOException e) {
        System.out.println("i/o exception");
        return;
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }
    new Thread(new EncodeMonitor(encode)).start();
  }

  public static class EncodeMonitor implements Runnable {
    private Path encode;
    
    public EncodeMonitor(Path encode) {
        this.encode = encode;
    }

    @Override
    public void run() {

    }
  }
}