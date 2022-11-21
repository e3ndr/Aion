package xyz.e3ndr.aion.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.functional.tuples.Pair;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.Resolver;
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
                "-s",
                "--soft"
        }, description = "If soft, then don't update the path IF another package already handles that command.")
        private boolean soft = false;

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
    @Command(name = "rebuild", description = "Rebuilds your path using the existing configuration, useful if you nuked the path somehow. Note that this will automatically purge missing packages from the path.")
    public static class CommandPathRebuild implements Runnable {

        @Override
        public void run() {
            if (Aion.config().getPathConfiguration().isEmpty()) {
                Aion.LOGGER.info("Path configuration is empty, did you mean `path update`?");
                return;
            }

            Aion.LOGGER.info("Rebuilding path...");

            boolean didModify = false;

            Iterator<Map.Entry<String, Pair<String, String>>> it = Aion.config().getPathConfiguration().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Pair<String, String>> entry = it.next();

                String command = entry.getKey();

                boolean exists = AionCommands.findPackage(entry.getValue(), Aion.installCache()) != null;
                if (!exists) {
                    Aion.LOGGER.warn("%s:%s no longer exists, removing command `%s`.", entry.getValue().a(), entry.getValue().b(), command);
                    it.remove();
                    didModify = true;
                    continue;
                }

                updateLocalPath(entry.getValue().a(), entry.getValue().b(), command);
                Aion.LOGGER.info("Created command `%s` with package %s:%s.", command, entry.getValue().a(), entry.getValue().b());
            }

            addAionToLocalPath();

            if (didModify) {
                Aion.config().save(); // Save the modified configuration.
            }
        }

    }

    private static void addAionToLocalPath() {
        try {
            File unixExecutableFile = new File(Aion.PATH_DIR, "aion");
            File windowsExecutableFile = new File(Aion.PATH_DIR, "aion.bat");

            Files.write(
                unixExecutableFile.toPath(),
                Resolver
                    .getString("resource:///path/aion")
                    .getBytes()
            );

            Files.write(
                windowsExecutableFile.toPath(),
                Resolver
                    .getString("resource:///path/aion.bat")
                    .getBytes()
            );

            unixExecutableFile.setExecutable(true);
        } catch (IOException e) {
            Aion.LOGGER.warn("Unable to write the `aion` command to path. Things may break.\n%s", e.getMessage());
        }
    }

    private static void updateLocalPath(String pkg, String version, String commandName) {
        AsyncTask.createNonDaemon(() -> {
            try {
                String unixExecutable = Resolver.getString("resource:///path/path_format");
                String windowsExecutable = Resolver.getString("resource:///path/path_format.bat");

                unixExecutable = unixExecutable
                    .replace("{package}", pkg)
                    .replace("{version}", version)
                    .replace("{command}", commandName);
                windowsExecutable = windowsExecutable
                    .replace("{package}", pkg)
                    .replace("{version}", version)
                    .replace("{command}", commandName + ".bat");

                File unixExecutableFile = new File(Aion.PATH_DIR, commandName);
                File windowsExecutableFile = new File(Aion.PATH_DIR, commandName + ".bat");

                Files.write(unixExecutableFile.toPath(), unixExecutable.getBytes());
                Files.write(windowsExecutableFile.toPath(), windowsExecutable.getBytes());

                unixExecutableFile.setExecutable(true);
            } catch (IOException e) {
                Aion.LOGGER.warn("Unable to write the `%s` command to path. Things may break.\n%s", commandName, e.getMessage());
            }
        });
    }

}
