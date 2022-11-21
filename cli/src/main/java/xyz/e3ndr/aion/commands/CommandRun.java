package xyz.e3ndr.aion.commands;

import lombok.AllArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;

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

    @Override
    public void run() {
        // TODO
        Aion.LOGGER.fatal("TODO");
    }

}
