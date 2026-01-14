import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

public class BuiltinCommandHandler {

  public static final Set<String> BUILT_INS = Set.of("exit","echo", "cd", "pwd", "type");

  public boolean handle(String input, Shell shell) throws Exception {
    String[] parts = input.split("\\s+", 2);
    String cmd = parts[0];

    if (!BUILT_INS.contains(cmd)) return false;

    switch (cmd) {
      case "pwd" -> System.out.println(shell.getCurrentDirectory().getAbsolutePath());
      case "cd" -> handleCd(parts.length > 1 ? parts[1] : "", shell);
      case "type" -> handleType(parts.length > 1 ? parts[1] : "");
      case "echo" -> handleEcho(parts.length > 1 ? parts[1] : "", shell);
    }
    return true;
  }

  private void handleCd(String arg, Shell shell) throws Exception {
    if (arg.equals("~")) {
      shell.setCurrentDirectory(new File(System.getenv("HOME")));
      return;
    }

    File target = arg.startsWith("/") ? new File(arg) : new File(shell.getCurrentDirectory(), arg);

    target = target.getCanonicalFile();

    if (!target.exists() || !target.isDirectory()) {
      System.out.println("cd: " + arg + ": No such file or directory");
      return;
    }

    shell.setCurrentDirectory(target);
  }

  private void handleType(String arg) {

    if (BUILT_INS.contains(arg)) {
      System.out.println(arg + " is a shell builtin");
    } else {
      File f = ExecutableResolver.find(arg);
      if (f != null) {
        System.out.println(arg + " is " + f.getAbsolutePath());
      } else {
        System.out.println(arg + ": not found");
      }
    }
  }

  private void handleEcho(String arg, Shell shell) throws Exception {
    Redirection redirection = RedirectionParser.parse(arg);
    List<String> tokens = ArgumentParser.parse(redirection.commandPart());
    String output = String.join(" ", tokens);

    if (redirection.hasRedirection()) {
      Path filePath = shell.getCurrentDirectory().toPath().resolve(redirection.target());
      if (Files.notExists(filePath)) {
        Files.createFile(filePath);
      }
      if (redirection.isStdout()) {
        Files.writeString(filePath, output + System.lineSeparator());
      } else if (redirection.isStderr()) {
        System.out.println(output);
      } else if (redirection.isAppendOutput()){
		Files.writeString(filePath, output + System.lineSeparator(), StandardOpenOption.APPEND);
	  }
	  else if (redirection.isAppendError()){
		System.out.println(output);
	  }
    } else {
      System.out.println(output);
    }
  }
}
