package xyz.e3ndr.aion.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update", description = "Updates the specified list of packages.")
public class CommandUpdate implements Runnable {

    @Option(names = {
            "-d",
            "--dry"
    }, description = "Performs a dry run (no files will be modified).")
    private boolean isDryRun = false;

    @Parameters(arity = "1..*", description = "The list of packages to update.", paramLabel = "PACKAGE:VERSION")
    private String[] interim_packagesToUpdate;

    @Override
    public void run() {
        // TODO
    }

}
