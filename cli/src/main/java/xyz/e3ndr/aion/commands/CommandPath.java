package xyz.e3ndr.aion.commands;

import java.util.List;
import java.util.Map.Entry;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathRebuild;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathUpdate;
import xyz.e3ndr.aion.commands.CommandPath.CommandPathWhat;
import xyz.e3ndr.aion.types.AionPackage;

//@NoArgsConstructor
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
        if (Aion.config().getPathConfiguration().isEmpty()) {
            Aion.LOGGER.info("No packages are configured to be on the path.");
            return;
        }

        Aion.LOGGER.info("Path:");
        for (Entry<String, Pair<String, String>> entry : Aion.config().getPathConfiguration().entrySet()) {
            String command = entry.getKey();
            String version = String.format("%s:%s", entry.getValue().a(), entry.getValue().b());

            Aion.LOGGER.info("    %s=%s", command, version);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Command(name = "what", description = "Lists all commands provided by the specified list of packages.")
    public static class CommandPathWhat implements Runnable {

        @Parameters(arity = "1..*", description = "The list of packages to list commands for.", paramLabel = "PACKAGE[:VERSION]")
        private String[] interim_packagesToList;

        @Override
        public void run() {
            List<Pair<String, String>> packagesToFind = AionCommands.parseAllVersions(this.interim_packagesToList);

            Aion.LOGGER.info("Looking for packages...");
            List<AionPackage.Version> packages = AionCommands.findPackages(packagesToFind, Aion.installCache());

            Aion.LOGGER.info(""); // Newline.

            for (AionPackage.Version version : packages) {
                if (version.getCommands().isEmpty()) {
                    Aion.LOGGER.fatal("%s:%s provides no commands.", version.getPkg().getSlug(), version.getVersion());
                    continue;
                }

                Aion.LOGGER.fatal("%s:%s provides:", version.getPkg().getSlug(), version.getVersion());

                for (String command : version.getCommands().keySet()) {
                    Aion.LOGGER.fatal("    %s", command);
                }
            }
        }

    }

    @NoArgsConstructor
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

//    @NoArgsConstructor
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
