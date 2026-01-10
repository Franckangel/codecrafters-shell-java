import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {

  public static List<String> parse(String input) {
    List<String> args = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingleQuotes = false;
    boolean inDoubleQuotes = false;

    for (int i = 0; i < input.length(); i++) {
      char character = input.charAt(i);

      // In double quotes only double quotes(") and escape can be escaped(\)
      if (character == '\\' && inDoubleQuotes) {
        if (i + 1 < input.length() && (input.charAt(i + 1) == '"' || input.charAt(i + 1) == '\\')) {
          current.append(input.charAt(++i));
        } else {
          current.append(character);
        }
        continue;
      }

      // Every characters outside single or double quotes can be escaped

      if (character == '\\' && !inSingleQuotes && !inDoubleQuotes) {
        if (i + 1 < input.length()) {
          current.append(input.charAt(++i));
        }
        continue;
      }

      // Single quotes (only if not in double quotes)
      // invariant : A single quote is never printed unless escaped or in a double quote.
      if (character == '\'' && !inDoubleQuotes) {
        inSingleQuotes = !inSingleQuotes;
        continue;
      }
      // double quotes (only if not in single quotes)
      if (character == '"' && !inSingleQuotes) {
        inDoubleQuotes = !inDoubleQuotes;
        continue;
      }

      // space handling
      // any space outside single or double quotes is collapsed

      if (character == ' ' && !inSingleQuotes && !inDoubleQuotes) {
        if (current.length() > 0) {
          args.add(current.toString());
          current.setLength(0);
        }
        continue;
      }
      // add any other character
      current.append(character);
    }

    if (current.length() > 0) {
      args.add(current.toString());
    }

    return args;
  }
}
