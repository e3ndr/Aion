package xyz.e3ndr.aion.commands;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.UserInput;
import xyz.e3ndr.aion.Util;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Installed.InstallCacheEntry;
import xyz.e3ndr.aion.types.AionPackage;

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
        List<Pair<String, String>> packagesToFind = AionCommands.parseAllVersions(this.interim_packagesToRemove);

        // Look for the packages in the source cache.
        Aion.LOGGER.info("Looking for packages...");

        List<AionPackage.Version> packages = Aion
            .installCache()
            .parallelStream()
            .map((entry) -> entry.pkg.getVersions().get(entry.version))
            .filter((version) -> {
                for (Pair<String, String> toFind : packagesToFind) {
                    if (version.getPkg().getSlug().equals(toFind.a()) &&
                        version.getVersion().equals(toFind.b())) {
                        return true;
                    }
                }

                return false;
            })
            .collect(Collectors.toList());

        if (packages.size() == 0) {
            Aion.LOGGER.info("No packages will be removed.");
            return;
        } else {
            Aion.LOGGER.info("The following packages will be removed:");
            for (AionPackage.Version version : packages) {
                Aion.LOGGER.info("    %s:%s (%s) %s", version.getPkg().getSlug(), version.getVersion(), version.getPatch(), version.getPkg().getAliases());
            }
        }

        Aion.LOGGER.info("A total of %d package(s) will be removed.", packages.size());

        // Dry run
        if (this.isDryRun) {
            Aion.LOGGER.info("----End of dry run----");
            return;
        }

        if (!this.autoYes) {
            Aion.LOGGER.info("Are you sure you wish to remove the above packages? (Y/n)");

            if (!UserInput.confirm()) {
                Aion.LOGGER.info("Aborting remove.");
                return;
            }
        }

        for (AionPackage.Version version : packages) {
            // We already know that `version` is in this list, we're just removing it.
            Iterator<InstallCacheEntry> it = Aion.installCache().iterator();
            while (it.hasNext()) {
                InstallCacheEntry entry = it.next();
                if (entry.pkg.getSlug().equals(version.getPkg().getSlug()) &&
                    entry.version.equals(version.getVersion())) {
                    it.remove();
                    break;
                }
            }

            Installed.save(Aion.installCache());

            // Delete the dir.
            File packageWorkingDir = new File(
                Aion.PACKAGES_DIR,
                String.format("%s/%s", version.getPkg().getSlug(), version.getVersion())
            );

            Util.recursivelyDeleteDirectory(packageWorkingDir);
        }

        // TODO find orphaned dependencies and recursively remove them.

        AionCommands.path_rebuild(false);
    }

}
