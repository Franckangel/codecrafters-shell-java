import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ExternalCommandExecutor {

    public void execute(String input, Shell shell) throws Exception {
        Redirection redirection = RedirectionParser.parse(input);
        List<String> args = ArgumentParser.parse(redirection.commandPart());

        if (ExecutableResolver.find(args.get(0)) == null) {
            System.out.println(args.get(0) + ": command not found");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(shell.getCurrentDirectory());
        Process process = pb.start();

        if (redirection.isStdout()) {
            writeStream(process.getInputStream(),
                shell.getCurrentDirectory().toPath().resolve(redirection.target()));
            printStream(process.getErrorStream());
        } else if (redirection.isStderr()) {
            writeStream(process.getErrorStream(),
                shell.getCurrentDirectory().toPath().resolve(redirection.target()));
            printStream(process.getInputStream());
        } else {
            printStream(process.getInputStream());
            printStream(process.getErrorStream());
        }

        process.waitFor();
    }

    private void printStream(java.io.InputStream stream) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private void writeStream(java.io.InputStream stream, Path file) throws Exception {
        Files.createDirectories(file.getParent());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = br.readLine()) != null) {
                Files.writeString(
                    file,
                    line + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                );
            }
        }
    }
}
