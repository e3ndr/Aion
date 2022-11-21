package xyz.e3ndr.aion.commands;

import lombok.AllArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathRebuild;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathUpdate;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathWhat;

@AllArgsConstructor
//@formatter:off
@Command(
   name = "path", 
   description = "Lists all commands added to your path by Aion, run `aion path --help` to see subcommands.", 
   mixinStandardHelpOptions = true, 
   subcommands = {
       CommandPathWhat.class,
       CommandPathUpdate.class,
       CommandPathRebuild.class,
   }
)
//@formatter:on
public class CommandPath implements Runnable {

    @Override
    public void run() {
        // TODO
        Aion.LOGGER.fatal("TODO");
    }

    @AllArgsConstructor
    @Command(name = "what", description = "Lists all commands handled by the specified list of packages.")
    public static class CommandPathWhat implements Runnable {

        @Parameters(arity = "1..*", description = "The list of packages to list commands for.", paramLabel = "PACKAGE[:VERSION]")
        private String[] interim_packagesToInstall;

        @Override
        public void run() {
            // TODO
            Aion.LOGGER.fatal("TODO");
        }

    }

    @AllArgsConstructor
    @Command(name = "update", description = "Configures your path to use a specific package.")
    public static class CommandPathUpdate implements Runnable {

        @Option(names = {
                "-e",
                "--easy"
        }, description = "If easy, then don't update the path IF another package already handles that command.")
        private boolean easy = false;

        @Parameters(arity = "1", description = "The package to use for the commands.", paramLabel = "PACKAGE:VERSION")
        private String packageToUse;

        @Parameters(arity = "2..*", description = "The list of commands to update.", paramLabel = "commands")
        private String[] commandsToUpdate;

        @Override
        public void run() {
            // TODO
            Aion.LOGGER.fatal("TODO");
        }

    }

    @AllArgsConstructor
    @Command(name = "rebuild", description = "Rebuilds your path using the existing configuration, useful if you nuked the path somehow.")
    public static class CommandPathRebuild implements Runnable {

        @Override
        public void run() {
            // TODO
            Aion.LOGGER.fatal("TODO");
        }

    }

}
