import java.util.Scanner;

import cli.ParsedCommand;
import processors.ImageProcessor;

public class Main {
  /** Runs CLI. */
  public static void main(String... args) {
    Scanner scanner = new Scanner(System.in);
    // get input
    String line;
    line = scanner.nextLine();
    // loop until exit
    while (!line.equals("exit")) {
      try {
        if (!ParsedCommand.executeSpecial(line)) {
          ImageProcessor.execute(ParsedCommand.parse(line));
        }
      } catch (IllegalArgumentException e) {
        System.out.println("Not a valid command. Type 'help' for instructions.");
      }
      line = scanner.nextLine();
    }
    scanner.close();
  }
}