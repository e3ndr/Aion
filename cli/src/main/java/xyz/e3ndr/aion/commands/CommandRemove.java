package xyz.e3ndr.aion.commands;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;

@NoArgsConstructor
@AllArgsConstructor
@Command(name = "remove", description = "Removes the specified packages.")
public class CommandRemove implements Runnable {

    @Option(names = {
            "-d",
            "--dry"
    }, description = "Performs a dry run (no files will be modified).")
    private boolean isDryRun = false;

    @Option(names = {
            "-y",
            "--yes"
    }, description = "Automatically confirms the Yes/No prompt.")
    private boolean autoYes = false;

    @Parameters(arity = "1..*", description = "The list of packages to remove.", paramLabel = "PACKAGE:VERSION")
    private String[] interim_packagesToRemove;

    @Override
    public void run() {
        // TODO
        Aion.LOGGER.fatal("TODO");
    }

}
