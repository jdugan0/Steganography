import java.util.Scanner;

import cli.ParsedCommand;
import processors.ImageProcessor;

public class Main {
  /** Runs CLI. */
  public static void main(String... args) { // prompt help
    System.out.println("Type 'help' for assistance.");
    Scanner scanner = new Scanner(System.in);
    // get input
    String line;
    line = scanner.nextLine().toLowerCase();
    // loop until exit
    while (!line.equals("exit")) {
      try {
        if (!ParsedCommand.executeSpecial(line)) {
          ImageProcessor.execute(ParsedCommand.parse(line));
        }
      } catch (IllegalArgumentException e) {
        System.out.println("Not a valid command. Type 'help' for instructions.");
      }
      catch (Exception e){ 
        System.out.println("Command failed. Please type 'help' for instructions.");
      }
      line = scanner.nextLine();
    }
    scanner.close();
  }
}