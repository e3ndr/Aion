package xyz.e3ndr.aion.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.UserInput;
import xyz.e3ndr.aion.types.AionPackage;

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
        // Figure out what is out-of-date.
        List<AionPackage.Version> toUpdate = new LinkedList<>();

        Aion
            .installCache()
            .forEach((entry) -> {
                AionPackage.Version latestVersion = AionCommands.findPackage(new Pair<>(entry.pkg.getSlug(), entry.version), Collections.emptySet());

                int[] patch1 = Aion.figureOutIntness(entry.pkg.getVersions().get(entry.version).getPatch());
                int[] patch2 = Aion.figureOutIntness(latestVersion.getPatch());

                if (!Aion.compare(patch1, patch2)) {
                    // We have an update.
                    toUpdate.add(latestVersion);
                }
            });

        // Print out the changes.
        if (toUpdate.size() == 0) {
            Aion.LOGGER.info("No packages will be updated.");
            return;
        } else {
            Aion.LOGGER.info("The following packages will be updated:");
            for (AionPackage.Version version : toUpdate) {
                Aion.LOGGER.info("    %s:%s (%s) %s", version.getPkg().getSlug(), version.getVersion(), version.getPatch(), version.getPkg().getAliases());
            }
        }

        Aion.LOGGER.info("A total of %d package(s) will be updated.", toUpdate.size());

        // Dry run
        if (this.isDryRun) {
            Aion.LOGGER.info("----End of dry run----");
            return;
        }

        if (!this.autoYes) {
            Aion.LOGGER.info("Are you sure you wish to update the above packages? (Y/n)");

            if (!UserInput.confirm()) {
                Aion.LOGGER.info("Aborting update.");
                return;
            }
        }

        List<String> toUpdateAsString = new ArrayList<>(toUpdate.size());
        for (AionPackage.Version version : toUpdate) {
            toUpdateAsString.add(String.format("%s:%s", version.getPkg().getSlug(), version.getVersion()));
        }

        // Install, this'll update the install cache.
        AionCommands.install(false, true, true, true, toUpdateAsString.toArray(new String[0]));

        // Rebuild path, this'll update the local path config.
        AionCommands.path_rebuild();
    }

}
