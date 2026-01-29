import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Shell {

  private File currentDirectory = new File(System.getProperty("user.dir"));
  private final CommandDispatcher dispatcher = new CommandDispatcher();
  Terminal terminal;
  private final BuiltinCommandHandler builtinCommandHandler = new BuiltinCommandHandler();

  public Shell(){
    String histfile = System.getenv("HISTFILE");

    if(histfile != null){
      Path histfilePath = currentDirectory.toPath().resolve(histfile);
      builtinCommandHandler.readFromFile(histfilePath);
    }
  }

  @SuppressWarnings("ConvertToTryWithResources")
  public void run() throws Exception {

    terminal = TerminalBuilder.builder().system(true).build();

    Set<String> pathExecutables = ExecutableResolver.getExecutables();
    Set<String> allExecutables = new TreeSet<>(BuiltinCommandHandler.BUILT_INS);
    allExecutables.addAll(pathExecutables);

    Completer completer =
        new ShellCompleter(allExecutables);

    DefaultParser parser = new DefaultParser();
    parser.setEscapeChars(new char[0]);

    LineReader lineReader =
        LineReaderBuilder.builder().terminal(terminal).parser(parser).completer(completer).build();

    while (true) {

      try {
        String input = lineReader.readLine("$ ");

        if (input.equals("exit")) {
          terminal.close();
          return;
        }
        dispatcher.dispatch(input, this);
      } catch (Exception e) {

      }
    }
  }

  public File getCurrentDirectory() {
    return currentDirectory;
  }

  public void setCurrentDirectory(File dir) {
    this.currentDirectory = dir;
  }

  public Terminal getTerminal(){
    return terminal;
  }
}
