import java.io.File;

public class ExecutableResolver {

  public static File find(String name) {
    for (String dir : System.getenv("PATH").split(File.pathSeparator)) {
      File f = new File(dir, name);
      if (f.exists() && f.canExecute()) return f;
    }
    return null;
  }
}
