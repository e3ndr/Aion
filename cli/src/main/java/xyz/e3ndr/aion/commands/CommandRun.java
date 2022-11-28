package xyz.e3ndr.aion.commands;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.commons.platform.OSFamily;
import co.casterlabs.commons.platform.Platform;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@NoArgsConstructor
@AllArgsConstructor
@Command(name = "run", description = "Runs the specified command using the specified packages.")
public class CommandRun implements Runnable {

    @Option(names = {
            "-ai",
            "--auto-install"
    }, description = "Automatically installs the specified packages.")
    private boolean autoInstall = false;

    @Option(names = {
            "-p",
            "--package"
    }, description = "Specifies a package to be placed on the command's path during execution. Ommiting version will use the latest available.", paramLabel = "PACKAGE[:VERSION]")
    private String[] interim_packagesToUse = {};

    @Parameters(arity = "1..*", description = "The command to run.", paramLabel = "COMMAND")
    private String[] command;

    @SneakyThrows
    @Override
    public void run() {
        // Only allow logging if DEBUG or TRACE or ALL.
        if (!LogLevel.DEBUG.canLog(Aion.LOGGER.getCurrentLevel())) {
            Aion.LOGGER.setCurrentLevel(LogLevel.NONE);
        }

        AionCommands.install(false, this.autoInstall, false, false, this.interim_packagesToUse);

        // Resolve the packages.
        List<AionPackage.Version> packages = AionCommands.searchPackages(AionCommands.parseAllVersions(this.interim_packagesToUse), Aion.installCache());
        if (packages == null) return; // The error message will already be printed.

        // TODO A better way of doing this.
        String[] args;
        String pathKey;

        if (Platform.osFamily == OSFamily.WINDOWS) {
            pathKey = "Path";
            args = new String[2 + this.command.length];
            args[0] = "cmd";
            args[1] = "/c";
            System.arraycopy(this.command, 0, args, 2, this.command.length);
        } else {
            pathKey = "PATH";
            args = new String[2 + this.command.length];
            args[0] = "/bin/sh";
            args[1] = "-c";
            System.arraycopy(this.command, 0, args, 2, this.command.length);
        }

        Aion.LOGGER.debug("Using command: %s", Arrays.toString(args));

        // Add the packages to the path.
        String path = System.getenv(pathKey);

        for (AionPackage.Version version : packages) {
            File commandsDir = new File(
                Aion.PACKAGES_DIR,
                String.format("%s/%s/commands", version.getPkg().getSlug(), version.getVersion())
            );

            // Prepend.
            path = commandsDir.getCanonicalPath() + File.pathSeparator + path;
        }

        Aion.LOGGER.debug("Using path: %s", path);

        // Create & Start the process.
        ProcessBuilder pb = new ProcessBuilder()
            .command(args)
            .inheritIO();

        pb
            .environment()
            .put(pathKey, path);

        path = null;
        packages = null;
        args = null;
        Aion.teardown();

        pb.start();
    }

}
