import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public class ExecutableResolver {

  public static String[] directories = System.getenv("PATH").split(File.pathSeparator);

  public static File find(String name) {
    for (String dir : directories) {
      File f = new File(dir, name);
      if (f.exists() && f.canExecute()) return f;
    }
    return null;
  }

  public static Set<String> getExecutables(){

    Set<String> executables = new TreeSet<>();
    File[] files = null;
    if(directories == null)
      return null;

    for(String dir : directories){
      File fileDirectory = new File(dir);

      if(fileDirectory.isDirectory())
        files = fileDirectory.listFiles();

      if(files != null){
        for(File file : files){
          if(file.isFile() && file.canExecute())
            executables.add(file.getName());
        }
      }
    }

    return executables;

  }
}
