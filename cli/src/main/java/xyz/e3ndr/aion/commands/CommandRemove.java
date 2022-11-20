package xyz.e3ndr.aion.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "remove", description = "Removes the specified packages.")
public class CommandRemove implements Runnable {

    @Option(names = {
            "-d",
            "--dry"
    }, description = "Performs a dry run (no files will be modified.)")
    private boolean isDryRun = false;

    @Parameters(arity = "1..*", description = "The list of packages to remove.", paramLabel = "PACKAGE:VERSION")
    private String[] interim_packagesToRemove;

    @Override
    public void run() {
        // TODO
    }

}
