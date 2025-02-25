package cli;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.stream.Stream;

public class EncodeMonitor implements Runnable {
  private Path encode;
  private FileSystem fileSystem;
  private WatchService watchService;

  public EncodeMonitor(Path encode) throws IOException, Exception {
    this.encode = encode;
    this.fileSystem = encode.getFileSystem();
    try {
      this.watchService = fileSystem.newWatchService();
      encode.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public void run() {
    try {
      Optional<WatchKey> key = Optional.ofNullable(watchService.poll());
      if (key.isPresent() && key.get().pollEvents().size() >= 1) {
        try {
            Stream<Path> files = Files.list(encode);
            
        } catch (IOException e) {
            System.out.println("i/o exception");
            e.printStackTrace();
        }
      };
    } catch (ClosedWatchServiceException e) {
      try {
        this.watchService = fileSystem.newWatchService();
        encode.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
      } catch (IOException io) {
        System.out.println("i/o exception");
      } catch (Exception x) {
        x.printStackTrace();
      }
      e.printStackTrace();
    }
  }
}