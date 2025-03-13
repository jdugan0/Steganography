package cli;

import java.util.ArrayList;
import java.util.Optional;

import processors.BitNoiseProcessor;
import processors.FourierDownsample;
import processors.ImageProcessor;
import util.ParseHelpers;

/**
 * Represents a set of command parameters
 * parsed from the user's terminal input.
 */
public class ParsedCommand {
  /**
   * Allowable {@code ImageProcessor} types.
   */
  public enum ProcessorType {
    kBitnoise(BitNoiseProcessor.instance()),
    kFourier(FourierDownsample.instance());

    /** static {@code ImageProcessor} instance associated with type */
    private ImageProcessor instance;

    private ProcessorType(ImageProcessor instance) {
      this.instance = instance;
    }

    /**
     * Gets associated {@code ImageProcessor} instance.
     * @return Instance.
     */
    public ImageProcessor toInstance() {
      return instance;
    }
  }

  /** pattern of allowable input strings */
  private static final String[][] pattern = {
    { "encode", "decode" },
    { "--prepare", "--to:" }
  };

  /** processor to execute command */
  private ProcessorType processor;
  /** whether this command encodes */
  private boolean encode;
  /** saves to decode if encoding and {@code true} */
  private Optional<Boolean> prepare;
  /** path to save if {@code encode --to:} */
  private Optional<String> path;

  /**
   * Constructs a new {@code ParsedCommand}
   * representing the input parameters.
   * 
   * @param processor Which image processor with which to run the command.
   * @param encode If this command encodes. If {@code false}, this command decodes.
   * @param prepare For encoding only. This optional instructs that the encoded
   * image be saved to the Images/Decode folder if {@code true} and to Images/Output
   * if {@code false}. Empty if this command decodes.
   * @param path Present if {@code encode --to:}, specifying the path to save the
   * encoded image to. Empty otherwise.
   */
  private ParsedCommand(ProcessorType processor, boolean encode,
      Optional<Boolean> prepare, Optional<String> path) {
    this.processor = processor;
    this.encode = encode;
    this.prepare = prepare;
    this.path = path;
  }

  /**
   * Parses an input string representing a user command for its constituent
   * parameters.
   * 
   * @param command User's command.
   * @return {@code new ParsedCommand} containing the parsed parameters.
   * @throws IllegalArgumentException if the input command does not match
   * {@code pattern}.
   */
  public static ParsedCommand parse(String command)
      throws IllegalArgumentException {
    // separate command tokens
    String[] tokens = command.split(" ");
    // storage for parameters
    ArrayList<Boolean> params = new ArrayList<>();
    // iterate through tokens to match with pattern
    for (int i = 1; i < tokens.length && i <= pattern.length; i++) {
      // find token
      int index = ParseHelpers.search(pattern[i - 1], tokens[i]);
      // throw exception if not found
      if (index == -1) {
        throw new IllegalArgumentException();
      }
      // save token
      String token = pattern[i - 1][index];
      params.add(token.equals(pattern[i - 1][0]));
    }
    ProcessorType type = null;
    // determine processor
    for (ProcessorType processor : ProcessorType.values()) {
      // convert constant to string
      String constant = processor.toString().substring(1).toLowerCase();
      // check against first token
      if (tokens[0].equals(constant)) {
        type = processor;
        break;
      }
    }
    // validity checks
    if (type == null) {
      throw new IllegalArgumentException();
    }
    Optional<Boolean> prepare = Optional.empty();
    Optional<String> path = Optional.empty();
    if (params.size() >= 2) {
      // additional parameters invalid if decoding
      if (!params.get(0)) {
        throw new IllegalArgumentException();
      } else { // encoding
        // "--to:" requires path
        if (!params.get(1) && tokens.length != 4) {
          throw new IllegalArgumentException();
        }
        prepare = Optional.of(params.get(1));
        // checking path
        if (tokens.length == 4) {
          path = Optional.of(tokens[3]);
          // path invalid if preparing
          if (params.get(1)) {
            throw new IllegalArgumentException();
          }
        }
      }
    }
    if (tokens.length > 4) { // 4 possible tokens
      throw new IllegalArgumentException();
    }
    // return parsed command
    return new ParsedCommand(type, params.get(0), prepare, path);
  }

  /**
   * Get the processor with which
   * to execute the command.
   * 
   * @return Processor type.
   */
  public ProcessorType getProcessor() {
    return processor;
  }

  /**
   * Get whether this command encodes.
   * 
   * @return {@code true} if encoding,
   * {@code false} if decoding.
   */
  public boolean isEncode() {
    return encode;
  }

  /**
   * Get preparation status.
   * 
   * @return Empty {@code Optional} if this command decodes.
   * Otherwise, {@code true} if this command encodes to the
   * decode folder and {@code false} if encoding to output.
   */
  public Optional<Boolean> isPrepare() {
    return prepare;
  }

  /**
   * Get path for {@code encode --to:}.
   * 
   * @return Empty {@code Optional} if
   * representing a different command pattern.
   * If present, represents a relative file path.
   */
  public Optional<String> getPath() {
    return path;
  }
}
