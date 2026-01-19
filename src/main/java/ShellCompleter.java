import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

class ShellCompleter implements Completer {

  private final List<String> executables;
  private boolean pending = false;
  private String lastWord = "";

  ShellCompleter(Set<String> executables) {
    this.executables = new ArrayList<>(executables);
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

    String word = line.word();

    List<String> matches = executables.stream().filter(e -> e.startsWith(word)).sorted().toList();

    if (matches.size() <= 1) {
      pending = false;
      matches.forEach(m -> candidates.add(new Candidate(m)));
      return;
    }

    // FIRST TAB → bell only
    if (!pending || !word.equals(lastWord)) {
      reader.getTerminal().writer().print("\u0007");
      reader.getTerminal().writer().flush();
      pending = true;
      lastWord = word;
      return;
    }

    // SECOND TAB → print matches
    var out = reader.getTerminal().writer();
    out.println();
    matches.forEach(m -> out.print(m + "  "));
    out.println();
    out.flush();

    reader.callWidget(LineReader.REDRAW_LINE);
    reader.callWidget(LineReader.REDISPLAY);

    pending = false;
  }
}
