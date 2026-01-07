import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

  static ArrayList<String> buildInCommands =
      new ArrayList<>(Arrays.asList("exit", "echo", "type", "pwd", "cd"));

  public static void main(String[] args) throws Exception {
    // Implement REPL

    try ( // Captures the user's command in the "command" variable
    Scanner scanner = new Scanner(System.in)) {
      File currentDirectory = new File(System.getProperty("user.dir"));
      while (true) {
        System.out.print("$ ");

        String command = scanner.nextLine();
        if (command.equals("exit")) break;
        else if (command.startsWith("echo ")) {
          String argument = command.substring(5);
          Sign greaterThanSign = new Sign();
          if (argument.contains(">") || argument.contains("1>"))
            greaterThanSign = IsSignInQuotes(argument);

          if (!greaterThanSign.isInQuotes) {
            // Redirect the command's output to a file
            // split the argument and seperate the paramters from the output destination file
            String commandParameters = argument.substring(0, greaterThanSign.getIndex());
            String fileName = argument.substring(greaterThanSign.getFileIndex());
            fileName = fileName.trim();

            // evaluate the parameter and get the output
            List<String> resultList = parseArguments(commandParameters);
            String results = String.join(" ", resultList.subList(0, resultList.size()));

            // Create the file if it doesn't exists
            Path filePath = currentDirectory.toPath().resolve(fileName);

            if (Files.notExists(filePath)) {
              Files.createFile(filePath);
            }
            // write the output into the file

            Files.writeString(filePath, results);

          } else {
            List<String> resultList = parseArguments(argument);
            String results = String.join(" ", resultList.subList(0, resultList.size()));
            System.out.println(results);
          }

        } else if (command.startsWith("type ")) {
          String argument = command.substring(5);
          typeArgumentInfo(argument);
        } else if (command.startsWith("pwd")) {
          System.out.println(currentDirectory.getAbsolutePath());
        } else if (command.startsWith("cd")) {
          // Handling absolute paths
          // Get the directory
          String directory = command.substring(2).trim();

          if (directory.equals("~")) {
            currentDirectory = new File(System.getenv("HOME"));
            continue;
          }

          File newDir = null;

          if (!directory.startsWith("/")) {
            newDir = new File(currentDirectory.getAbsolutePath(), directory);
          } else {
            newDir = new File(directory);
          }

          // Normalize path
          newDir = newDir.getCanonicalFile();

          // Check if the directory exists
          if (newDir.exists() && newDir.isDirectory()) {
            // Change to the given directory
            currentDirectory = newDir.getCanonicalFile();
          } else {
            System.out.println("cd: " + directory + ": No such file or directory");
          }
        } else {
          // the command isn't build in
          // Search for the command

          Sign greaterThanSign = new Sign();

          if (command.contains(">") || command.contains("1>"))
            greaterThanSign = IsSignInQuotes(command);

          List<String> arguments = parseArguments(command);

          File file = isFileExist(arguments.get(0));

          if (file != null) {

            if (!greaterThanSign.isInQuotes) {

              // Redirect the command's output to a file
              // split the argument and seperate the paramters from the output destination file
              String commandRework = command.substring(0, greaterThanSign.getIndex());
              String fileName = command.substring(greaterThanSign.getFileIndex());
              fileName = fileName.trim();
              // Create the file if it doesn't exists
              Path filePath = currentDirectory.toPath().resolve(fileName);

              if (Files.notExists(filePath)) {
                Files.createFile(filePath);
              }

              // evaluate the parameter and get the output
              List<String> argumentsRework = parseArguments(commandRework);
              ProcessBuilder pb = new ProcessBuilder(argumentsRework);
              pb.directory(currentDirectory);
              Process process = pb.start();
              // Read the output from the process
              try (BufferedReader stdout =
                  new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = stdout.readLine()) != null) {
                  Files.writeString(
                      filePath,
                      line + System.lineSeparator(),
                      StandardOpenOption.CREATE,
                      StandardOpenOption.APPEND);
                }
              }

              try (BufferedReader stderr =
                  new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = stderr.readLine()) != null) {
                  System.err.println(line);
                }
              }

              process.waitFor();

            } else {
              ProcessBuilder pb = new ProcessBuilder(arguments);
              pb.directory(currentDirectory);
              pb.redirectErrorStream(true); // Merges stderr into stdout
              Process process = pb.start();
              // Read the output from the process
              BufferedReader reader =
                  new BufferedReader(new InputStreamReader(process.getInputStream()));
              String line;
              while ((line = reader.readLine()) != null) {
                System.out.println(line);
              }
              process.waitFor(); // Wait for the process to finish
            }

          } else
            System.out.println(
                command + ": command not found"); // Prints the "<command>: command not found"
        }
      }
    }
  }

  public static void typeArgumentInfo(String argument) {
    if (buildInCommands.contains(argument)) System.out.println(argument + " is a shell builtin");
    else {
      // Check whether the file exists
      File file = isFileExist(argument);
      if (file != null) System.out.println(argument + " is " + file.getAbsolutePath());
      else System.out.println(argument + ": not found"); // If no file found
    }
  }

  public static File isFileExist(String argument) {
    // Get the PATH environment variable
    String pathVariable = System.getenv("PATH");
    // Parse the system path
    String[] pathDirs = pathVariable.split(File.pathSeparator);
    // Search through each directory
    File file = null;
    for (String dir : pathDirs) {
      file = new File(dir, argument);
      if (file.exists() && file.isFile() && file.canExecute()) {
        return file;
      }
    }
    return null;
  }

  public static List<String> parseArguments(String argument) {

    List<String> args = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingleQuotes = false;
    boolean inDoubleQuotes = false;

    char character = ' ';

    for (int i = 0; i < argument.length(); i++) {

      character = argument.charAt(i);

      // Escape : take the next character literally
      // invariant : You can only escape one character
      if (character == '\\' && inDoubleQuotes) {
        if (i + 1 < argument.length()
            && (argument.charAt(i + 1) == '"' || argument.charAt(i + 1) == '\\')) {
          current.append(argument.charAt(i + 1));
          i++; // skip next character
        } else {
          current.append(character);
        }
        continue;
      }

      if (character == '\\' && !inSingleQuotes && !inDoubleQuotes) {
        if (i + 1 < argument.length()) {
          current.append(argument.charAt(i + 1));
          i++; // skip next character
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

  public static Sign IsSignInQuotes(String argument) {

    boolean inSingleQuotes = false;

    boolean inDoubleQuotes = false;

    Sign sign = new Sign();

    for (int i = 0; i < argument.length(); i++) {
      if (argument.charAt(i) == '\'' && !inDoubleQuotes) {
        inSingleQuotes = !inSingleQuotes;
        continue;
      }

      if (argument.charAt(i) == '"' && !inSingleQuotes) {
        inDoubleQuotes = !inDoubleQuotes;
        continue;
      }

      if (argument.charAt(i) == '>' && !inDoubleQuotes && !inSingleQuotes) {
        sign.setIsInQuotes(false);
        sign.setIndex(i);
        sign.setFileIndex(i + 2);
        break;
      }

      if (argument.charAt(i) == '1'
          && i + 1 < argument.length()
          && argument.charAt(i + 1) == '>'
          && !inDoubleQuotes
          && !inSingleQuotes) {
        sign.setIsInQuotes(false);
        sign.setIndex(i);
        sign.setFileIndex(i + 3);
        break;
      }
    }

    return sign;
  }
}

class Sign {

  int index;

  boolean isInQuotes;

  int fileIndex;

  public Sign() {
    this.index = 0;
    this.isInQuotes = true;
    this.fileIndex = 0;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public boolean isIsInQuotes() {
    return isInQuotes;
  }

  public void setIsInQuotes(boolean isInQuotes) {
    this.isInQuotes = isInQuotes;
  }

  public int getFileIndex() {
    return fileIndex;
  }

  public void setFileIndex(int fileIndex) {
    this.fileIndex = fileIndex;
  }
}
