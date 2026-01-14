import java.io.File;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Shell {

  private File currentDirectory = new File(System.getProperty("user.dir"));
  private final CommandDispatcher dispatcher = new CommandDispatcher();
  private BuiltinCommandHandler builtinCommandHandler = new BuiltinCommandHandler();

  @SuppressWarnings("ConvertToTryWithResources")
  public void run() throws Exception {

    Terminal terminal = TerminalBuilder.builder().system(true).dumb(true).build();

    Completer completer = new StringsCompleter(builtinCommandHandler.BUILT_INS);

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
}
