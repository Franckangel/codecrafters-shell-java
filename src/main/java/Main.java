import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        ArrayList<String> commandList = new ArrayList<>(Arrays.asList("exit", "echo", "type"));
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
            if(commandList.contains(command.substring(5)))
                System.out.println(command.substring(5) + " is a shell builtin");
            else
                System.out.println(command.substring(5) + ": not found");
        }
        else
            // Prints the "<command>: command not found" message
            System.out.println(command + ": command not found");
        }
    }
}
