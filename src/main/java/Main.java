import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        ArrayList<String> buildInCommands = new ArrayList<>(Arrays.asList("exit", "echo", "type"));
        //Implement REPL 
        while(true){
        System.out.print("$ ");
        // Captures the user's command in the "command" variable
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        if(command.equals("exit"))
            break;
        else if(command.startsWith("echo ")){
            System.out.println(command.substring(5));
        }
        else if(command.startsWith("type ")){
            String argument = command.substring(5);
            if(buildInCommands.contains(argument))
                System.out.println(argument + " is a shell builtin");
            else{
                //Get the PATH environment variable
                String pathVariable = System.getenv("PATH");

                //Parse the system path
                String[] pathDirs = pathVariable.split(File.pathSeparator);

                //Search through each directory
                boolean commandFound = false;
                File file = null;
                for(String dir : pathDirs){
                    file = new File(dir, argument);
                    if(file.exists() && file.isFile() && file.canExecute()){
                        commandFound = true;
                        break;
                    }
                }

                if(commandFound)
                    System.out.println(argument + " is " + file.getAbsolutePath());
                else
                //If no file found
                    System.out.println(argument + ": not found");
            }
                
        }
        else
            // Prints the "<command>: command not found" message
            System.out.println(command + ": command not found");
        }
    }
}
