package cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.stream.Stream;

import filereader.FileReader;
import filereader.FileReader.ImageType;
import processors.BitNoiseProcessor;

public class EncodeMonitor implements Runnable {
  private Path encode;
  private Path source;
  private FileSystem fileSystem;
  private WatchService watchService;

  public EncodeMonitor(Path encode, Path source) throws IOException, Exception {
    this.encode = encode;
    this.source = source;
    this.fileSystem = encode.getFileSystem();
    try {
      this.watchService = fileSystem.newWatchService();
      encode.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
      source.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
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
      // we've seen at least one creation in a directory, so check both for image combination
      if (key.isPresent()) {
        try {
          // check we have an encode and a source
          Optional<Path> encodePath = Files.list(encode).findFirst();
          Optional<Path> sourcePath = Files.list(source).findFirst();
          if (encodePath.isPresent() && sourcePath.isPresent()) { // if we have images
            String[] encodePathString = encodePath.toString().split(File.separator);
            BitNoiseProcessor.instance().encode(
              FileReader.readImage(ImageType.Encode,
                encodePathString[encodePathString.length - 1]),
              FileReader.readImage(ImageType.Encode, null));
          }
        } catch (IOException e) {
          System.out.println("i/o exception");
          e.printStackTrace();
        }
      }
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