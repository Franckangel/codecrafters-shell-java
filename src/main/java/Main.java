import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

  static ArrayList<String> buildInCommands =
      new ArrayList<>(Arrays.asList("exit", "echo", "type", "pwd", "cd"));

  public static void main(String[] args) throws Exception {
    // Implement REPL

    // Captures the user's command in the "command" variable
    Scanner scanner = new Scanner(System.in);

    File currentDirectory = new File(System.getProperty("user.dir"));
    while (true) {
      System.out.print("$ ");

      String command = scanner.nextLine();
      if (command.equals("exit")) break;
      else if (command.startsWith("echo ")) {
        System.out.println(command.substring(5));
      } else if (command.startsWith("type ")) {
        String argument = command.substring(5);
        typeArgumentInfo(argument);
      } else if (command.startsWith("pwd")) {
        System.out.println(currentDirectory.getAbsolutePath());
      } else if (command.startsWith("cd")) {
        // Handling absolute paths
        // Get the directory
        String directory = command.substring(2).trim();

        File newDir = null;

        if (!directory.startsWith("/")) {
          newDir = new File(currentDirectory.getAbsolutePath(), directory);
        } else {
          newDir = new File(directory);
        }

        //Normalize path
        newDir = newDir.getCanonicalFile();

        // Check if the directory exists
        if (newDir.exists() && newDir.isDirectory()) {
          // Change to the given directory
          currentDirectory = newDir.getCanonicalFile();
        } else {
          System.out.println("cd: " + directory + ": No such file or directory");
        }
      } else {
        // the command isn't build inU
        // Search for the command
        String[] argumentList = command.split(" ");
        File file = isFileExist(argumentList[0]);

        if (file != null) {
          ProcessBuilder pb = new ProcessBuilder(argumentList);
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
        } else
          System.out.println(
              command + ": command not found"); // Prints the "<command>: command not found"
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
}
