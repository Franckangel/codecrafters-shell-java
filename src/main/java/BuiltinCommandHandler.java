import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jline.terminal.Terminal;

public class BuiltinCommandHandler {

  public static final Set<String> BUILT_INS =
      Set.of("exit", "echo", "cd", "pwd", "type", "history");

  private static List<String> cmdList = new ArrayList<>();

  // NON-PIPELINE ENTRY (unchanged behavior)
  public boolean handle(String input, Shell shell) throws Exception {
    return handle(input, shell, System.in, System.out, System.err);
  }

  // PIPELINE-AWARE ENTRY
  public boolean handle(
      String input, Shell shell, InputStream in, OutputStream out, OutputStream err)
      throws Exception {

    String[] parts = input.split("\\s+", 2);
    String cmd = parts[0];
    cmdList.add(input);

    if (!BUILT_INS.contains(cmd)) return false;

    switch (cmd) {
      case "pwd" -> write(out, shell.getCurrentDirectory().getAbsolutePath());
      case "cd" -> handleCd(parts.length > 1 ? parts[1] : "", shell, err);
      case "type" -> handleType(parts.length > 1 ? parts[1] : "", out);
      case "echo" -> handleEcho(parts.length > 1 ? parts[1] : "", shell, out);
      case "exit" -> System.exit(0);
      case "history" -> handleHistory(shell, input);
    }
    return true;
  }

  // HELPERS

  private void write(OutputStream out, String s) throws IOException {
    out.write((s + System.lineSeparator()).getBytes());
    out.flush();
  }

  private void handleCd(String arg, Shell shell, OutputStream err) throws Exception {
    if (arg.equals("~")) {
      shell.setCurrentDirectory(new File(System.getenv("HOME")));
      return;
    }

    File target = arg.startsWith("/") ? new File(arg) : new File(shell.getCurrentDirectory(), arg);

    target = target.getCanonicalFile();

    if (!target.exists() || !target.isDirectory()) {
      write(err, "cd: " + arg + ": No such file or directory");
      return;
    }

    shell.setCurrentDirectory(target);
  }

  private void handleType(String arg, OutputStream out) throws IOException {
    if (BUILT_INS.contains(arg)) {
      write(out, arg + " is a shell builtin");
    } else {
      File f = ExecutableResolver.find(arg);
      if (f != null) {
        write(out, arg + " is " + f.getAbsolutePath());
      } else {
        write(out, arg + ": not found");
      }
    }
  }

  private void handleEcho(String arg, Shell shell, OutputStream out) throws Exception {
    Redirection redirection = RedirectionParser.parse(arg);
    List<String> tokens = ArgumentParser.parse(redirection.commandPart());
    String output = String.join(" ", tokens);

    if (redirection.hasRedirection()) {
      Path filePath = shell.getCurrentDirectory().toPath().resolve(redirection.target());
      Files.createDirectories(filePath.getParent());

      if (Files.notExists(filePath)) Files.createFile(filePath);

      if (redirection.isStdout()) {
        Files.writeString(filePath, output + System.lineSeparator());

      } else if (redirection.isStderr() || redirection.isAppendError()) {
        write(out, output);
      } else if (redirection.isAppendOutput()) {
        Files.writeString(filePath, output + System.lineSeparator(), StandardOpenOption.APPEND);
      }
    } else {
      write(out, output);
    }
  }

  private void handleHistory(Shell shell, String input) {
    Terminal terminal = shell.getTerminal();
    int count = cmdList.size();
    int total = count;

    if (input.matches("^history -w .+$") || input.matches("^history -a .+$")) {
      // write command list into the file
      String[] parts = input.contains("-w") ? input.split("-w") : input.split("-a") ;
      String filePathString = parts[1].trim();
      Path filePath = shell.getCurrentDirectory().toPath().resolve(filePathString);

      saveCommandList(filePath);
    } else if (input.matches("^history -r .+$")) {
      String[] parts = input.split("-r");
      String filePathString = parts[1].trim();

      Path filePath = shell.getCurrentDirectory().toPath().resolve(filePathString);

      readFromFile(filePath);

    } else {

      if (input.matches("^history \\d$")) {
        String[] parts = input.split(" ");
        count = Integer.parseInt(parts[1]);
      }

      int start = total - count;
      for (int i = start; i < total; i++) {
        terminal.writer().println("    " + (i + 1) + "  " + cmdList.get(i));
      }
    }
  }

  public void saveCommandList(Path filePath) {

    try {

      Files.write(filePath, cmdList, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException ex) {
      System.getLogger(BuiltinCommandHandler.class.getName())
          .log(System.Logger.Level.ERROR, (String) null, ex);
    }

    cmdList = new ArrayList<>();
  }

  public void readFromFile(Path filePath) {
    List<String> lines = new ArrayList<>();

    try {
      if (Files.exists(filePath)) lines = Files.readAllLines(filePath);
    } catch (IOException ex) {
      System.getLogger(BuiltinCommandHandler.class.getName())
          .log(System.Logger.Level.ERROR, (String) null, ex);
    }

    for (String line : lines) {
      cmdList.add(line);
    }
  }
}
