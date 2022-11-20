package xyz.e3ndr.aion.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathUpdate;

//@formatter:off
@Command(
 name = "path", 
 description = "Lists all commands added to your path by Aion, run `aion path --help` to see subcommands.", 
 mixinStandardHelpOptions = true, 
 subcommands = {
         CommandPathUpdate.class
 }
)
//@formatter:on
public class CommandPath implements Runnable {

    @Override
    public void run() {
        // TODO
    }

    @Command(name = "update", description = "Configures your path to use a specific package.")
    public static class CommandPathUpdate implements Runnable {

        @Parameters(arity = "1", description = "The package to use for the commands.", paramLabel = "PACKAGE:VERSION")
        private String packageToUse;

        @Parameters(arity = "2..*", description = "The list of commands to update.", paramLabel = "commands")
        private String[] commandsToUpdate;

        @Override
        public void run() {
            // TODO
        }

    }

}
