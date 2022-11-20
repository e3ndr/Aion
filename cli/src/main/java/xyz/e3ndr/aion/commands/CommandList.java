package xyz.e3ndr.aion.commands;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import xyz.e3ndr.aion.Bootstrap;
import xyz.e3ndr.aion.types.AionPackage;

@Command(name = "list", description = "Lists all installed packages.")
public class CommandList implements Runnable {

    @SneakyThrows
    @Override
    public void run() {
        if (Bootstrap.getInstallCache().isEmpty()) {
            Bootstrap.LOGGER.info("No packages installed.");
            return;
        }

        Bootstrap.LOGGER.info("Packages:");
        for (AionPackage.Version version : Bootstrap.getInstallCache()) {
            Bootstrap.LOGGER.info("    %s:%s (%s)", version.getPackageSlug(), version.getVersion(), version.getPatch());
        }
    }

}
