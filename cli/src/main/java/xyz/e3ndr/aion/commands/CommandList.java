package xyz.e3ndr.aion.commands;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.configuration.Installed.InstallCacheEntry;

@AllArgsConstructor
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
        for (InstallCacheEntry entry : Aion.installCache()) {
            Aion.LOGGER.info(
                "    %s:%s (%s) %s",
                entry.pkg.getSlug(), entry.version, entry.pkg.getVersions().get(entry.version).getPatch(), entry.pkg.getAliases()
            );
        }
    }

}
