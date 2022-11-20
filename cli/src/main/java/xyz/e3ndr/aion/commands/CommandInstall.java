package xyz.e3ndr.aion.commands;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.Util;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionPackage.Version;
import xyz.e3ndr.aion.types.AionSourceList;

@Command(name = "install", description = "Installs the specified list of packages.")
public class CommandInstall implements Runnable {

    @Option(names = {
            "-d",
            "--dry"
    }, description = "Performs a dry run (no files will be modified).")
    private boolean isDryRun = false;

    @Parameters(arity = "1..*", description = "The list of packages to install. Ommiting version will install the latest available.", paramLabel = "PACKAGE[:VERSION]")
    private String[] interim_packagesToInstall;

    @SneakyThrows
    @Override
    public void run() {
        // Parse the cli parameters.
        List<Pair<String, String>> packagesToFind = new LinkedList<>();
        for (String pkg : this.interim_packagesToInstall) {
            packagesToFind.add(parseVersion(pkg));
        }

        // Look for the packages in the source cache.
        Aion.LOGGER.info("Looking for packages...");
        List<AionPackage.Version> packages = findPackages(packagesToFind, Aion.installCache()); // There's a comment below referring to this line.
        if (packages == null) return; // The error message will already be printed.

        Aion.LOGGER.info("The following packages will be installed:");
        for (AionPackage.Version version : packages) {
            Aion.LOGGER.info("    %s:%s (%s) %s", version.getPackageSlug(), version.getVersion(), version.getPatch(), version.getPackageAliases());
        }

        Aion.LOGGER.info("Resolving dependencies...");
        List<AionPackage.Version> dependencies = resolveDependencies(packages, Util.concat(Aion.installCache(), packages));

        List<AionPackage.Version> newInstallCache = new LinkedList<>();
        newInstallCache.addAll(Aion.installCache());
        newInstallCache.addAll(dependencies);
        newInstallCache.addAll(packages);

        if (dependencies.size() == 0) {
            Aion.LOGGER.info("No dependencies will be installed.");
        } else {
            Aion.LOGGER.info("The following dependencies will be installed:");
            for (AionPackage.Version version : dependencies) {
                Aion.LOGGER.info("    %s:%s (%s) %s", version.getPackageSlug(), version.getVersion(), version.getPatch(), version.getPackageAliases());
            }
        }

        // Print out the changes.
        int total = dependencies.size() + packages.size();
        if (total == 0) {
            Aion.LOGGER.info("No packages will be installed.");
            return;
        }

        Aion.LOGGER.info("A total of %d package(s) will be installed.", total);

        // Dry run
        if (this.isDryRun) {
            Aion.LOGGER.info("----End of dry run----");
            return;
        }

        Aion.LOGGER.info("Are you sure you wish to install the following packages? (Y/n)");
        // TODO

        Installed.save(newInstallCache);

        // TODO
    }

    private static Pair<String, String> parseVersion(String pkg) {
        String[] parts = pkg.split(":");

        if (parts.length > 2) {
            throw new IllegalArgumentException("Package declaration must be in the form of PACKAGE[:VERSION], got: " + pkg);
        }

        String slug = parts[0].toLowerCase();
        String version = "LATEST";

        if (parts.length == 2) {
            version = parts[1];
        }

        return new Pair<>(slug, version);
    }

    /**
     * @implNote null result means abort.
     */
    private static @Nullable List<AionPackage.Version> findPackages(List<Pair<String, String>> packagesToFind, List<Version> $alreadyHave) {
        if (packagesToFind.isEmpty()) return Collections.emptyList();

        List<AionPackage.Version> found = new LinkedList<>();

        List<Pair<String, String>> packagesNotFound = packagesToFind
            .parallelStream()
            // Make sure we're not already installing a required package.
            .filter((entry) -> {
                String slug = entry.a();
                String version = entry.b();

                boolean alreadyHas = $alreadyHave
                    .parallelStream()
                    .anyMatch((v) -> v.getPackageSlug().equals(slug) && v.getVersion().equals(version));

                if (alreadyHas) {
                    if ($alreadyHave == Aion.installCache()) {
                        // We want to change the this message if we're in dependency resolution.
                        // We know if we're in dependency resolution because during the first iteration,
                        // $alreadyHave will be the installCache. Scroll up to see the impl.
                        Aion.LOGGER.info("    %s:%s is already installed, did you mean `update`?", slug, version);
                    } else {
                        Aion.LOGGER.info("    Dependency %s:%s is already installed.", slug, version);
                    }
                }

                return !alreadyHas; // Remove the package if we already have it.
            })
            .filter((entry) -> {
                String slug = entry.a();
                String version = entry.b();
//                Aion.LOGGER.debug("    Looking for package: %s:%s", slug, version);

                for (AionSourceList sourcelist : Aion.sourceCache()) {
                    AionPackage.Version v = sourcelist.findPackage(slug, version);
                    if (v == null) continue; // Next source.

                    found.add(v);
                    return false; // Remove the current package.
                }

                return true; // Keep the current package.
            })
            .collect(Collectors.toList());
        packagesToFind.clear();

        // Couldn't find some packages, abort.
        if (!packagesNotFound.isEmpty()) {
            Aion.LOGGER.info("Could not find the following packages:");
            for (Pair<String, String> entry : packagesNotFound) {
                Aion.LOGGER.info("    %s:%s", entry.a(), entry.b());
            }
            return null;
        }
        return found;
    }

    /**
     * @implNote null result means abort.
     */
    private static @Nullable List<AionPackage.Version> resolveDependencies(List<AionPackage.Version> packages, List<AionPackage.Version> $alreadyHave) {
        if (packages.isEmpty()) return Collections.emptyList();

        List<Pair<String, String>> found = new LinkedList<>();

        for (AionPackage.Version pkg : packages) {
            for (String depend : pkg.getDepends()) {
                found.add(parseVersion(depend));
            }
        }

        List<AionPackage.Version> dependencies = findPackages(found, $alreadyHave);
        dependencies.addAll(resolveDependencies(dependencies, $alreadyHave)); // Recurse.

        return dependencies;
    }

}
