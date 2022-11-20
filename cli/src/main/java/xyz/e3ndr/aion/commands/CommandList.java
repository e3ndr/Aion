package xyz.e3ndr.aion.commands;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.AionPackage;

@Command(name = "list", description = "Lists all installed packages.")
public class CommandList implements Runnable {

    @SneakyThrows
    @Override
    public void run() {
        if (Aion.installCache().isEmpty()) {
            Aion.LOGGER.info("No packages installed.");
            return;
        }

        Aion.LOGGER.info("Packages:");
        for (AionPackage.Version version : Aion.installCache()) {
            Aion.LOGGER.info("    %s:%s (%s) %s", version.getPackageSlug(), version.getVersion(), version.getPatch(), version.getPackageAliases());
        }
    }

}
