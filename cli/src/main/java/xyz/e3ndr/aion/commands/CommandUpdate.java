package xyz.e3ndr.aion.commands;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.aion.Aion;

@NoArgsConstructor
@AllArgsConstructor
@Command(name = "update", description = "Updates the specified list of packages.")
public class CommandUpdate implements Runnable {

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

    @Override
    public void run() {
        // TODO
        Aion.LOGGER.fatal("TODO");
    }

}
