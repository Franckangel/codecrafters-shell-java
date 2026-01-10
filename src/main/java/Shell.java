import java.io.File;
import java.util.Scanner;

public class Shell {

    private File currentDirectory = new File(System.getProperty("user.dir"));
    private final CommandDispatcher dispatcher = new CommandDispatcher();

    public void run() throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("$ ");
                String input = scanner.nextLine().trim();
                if (input.equals("exit")) return;
                dispatcher.dispatch(input, this);
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
