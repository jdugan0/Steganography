import java.util.Scanner;

import cli.ParsedCommand;
import processors.ImageProcessor;

public class Main {
  /** Runs CLI. */
  public static void main(String... args) {
    Scanner scanner = new Scanner(System.in);
    String line;
    line = scanner.nextLine();
    while (!line.equals("exit")) {
      ImageProcessor.execute(ParsedCommand.parse(line));
      line = scanner.nextLine();
    }
    scanner.close();
  }
}