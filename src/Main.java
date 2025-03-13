import java.util.Scanner;

import cli.ParsedCommand;
import processors.ImageProcessor;

public class Main {
  /** terminal help message */
  private static final String help = "Type the processor and then the operation. " +
      "Processor can be bitnoise or fourier; operation can be encode or decode. " +
      "If you are encoding, you can optionally add \"--prepare\" to send the " +
      "encoded image to the decode folder or \"--to: \" plus a path to select a " +
      "custom folder. \"./\" also works when doing --to:. Type 'exit' to exit.";

  /** Runs CLI. */
  public static void main(String... args) {
    Scanner scanner = new Scanner(System.in);
    String line;
    line = scanner.nextLine();
    while (!line.equals("exit")) {
      if (line.equals("help")) {
        printHelpMessage();
        line = scanner.nextLine();
        continue;
      }
      try {
        ImageProcessor.execute(ParsedCommand.parse(line));
      } catch (IllegalArgumentException e) {
        System.out.println("Not a valid command. Type 'help' for instructions.");
      }
      line = scanner.nextLine();
    }
    scanner.close();
  }

  public static void printHelpMessage() {
    System.out.println(help);
  }
}