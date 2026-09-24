package gitlet;

import java.io.IOException;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author WEI Xize
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if (args.length != 1) {
                    message("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length != 2) {
                    message("Incorrect operands.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }
}
