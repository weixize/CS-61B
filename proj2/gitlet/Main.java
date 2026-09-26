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
                checkOperandsNumbers(args, 1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                checkOperandsNumbers(args, 2);
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                checkOperandsNumbers(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                checkOperandsNumbers(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                checkOperandsNumbers(args, 1);
                Repository.log();
                break;
            case "global-log":
                checkOperandsNumbers(args, 1);
                Repository.globalLog();
                break;
            case "find":
                checkOperandsNumbers(args, 2);
                Repository.find(args[1]);
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * Check numbers of operands
     * @param args arguments
     * @param n valid number
     */
    private static void checkOperandsNumbers(String[] args, int n) {
        if (args.length != n) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }
}
