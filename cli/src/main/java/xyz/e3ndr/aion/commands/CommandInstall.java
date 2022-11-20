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
import xyz.e3ndr.aion.Bootstrap;
import xyz.e3ndr.aion.Util;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionPackage.Version;
import xyz.e3ndr.aion.types.AionSourceList;

@Command(name = "install", description = "Installs the specified list of packages")
public class CommandInstall implements Runnable {

    @Option(names = {
            "-d",
            "--dry"
    }, description = "Performs a dry run (no files will be modified)")
    private boolean isDryRun = false;

    @Parameters(arity = "1..*", description = "The list of packages to install", paramLabel = "PACKAGE[:VERSION]")
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
        Bootstrap.LOGGER.info("Looking for packages...");
        List<AionPackage.Version> packages = findPackages(packagesToFind, Bootstrap.getInstallCache()); // There's a comment below referring to this line.
        if (packages == null) return; // The error message will already be printed.

        Bootstrap.LOGGER.info("The following packages will be installed:");
        for (AionPackage.Version version : packages) {
            Bootstrap.LOGGER.info("    %s:%s (%s)", version.getPackageSlug(), version.getVersion(), version.getPatch());
        }

        Bootstrap.LOGGER.info("Resolving dependencies...");
        List<AionPackage.Version> dependencies = resolveDependencies(packages, Util.concat(Bootstrap.getInstallCache(), packages));

        List<AionPackage.Version> newInstallCache = new LinkedList<>();
        newInstallCache.addAll(Bootstrap.getInstallCache());
        newInstallCache.addAll(dependencies);
        newInstallCache.addAll(packages);

        if (dependencies.size() == 0) {
            Bootstrap.LOGGER.info("No dependencies will be installed.");
        } else {
            Bootstrap.LOGGER.info("The following dependencies will be installed:");
            for (AionPackage.Version version : dependencies) {
                Bootstrap.LOGGER.info("    %s:%s (%s)", version.getPackageSlug(), version.getVersion(), version.getPatch());
            }
        }

        // Print out the changes.
        int total = dependencies.size() + packages.size();
        if (total == 0) {
            Bootstrap.LOGGER.info("No packages will be installed.");
            return;
        }

        Bootstrap.LOGGER.info("A total of %d package(s) will be installed.", total);

        Bootstrap.LOGGER.info("Checking for conflicts...");
        boolean didConflict = false;

        // Figure out if any packages have conflicts.
        for (AionPackage.Version pkg : packages) {
            for (String interim_conflict : pkg.getConflicts()) {
                Pair<String, String> conflict = parseVersion(interim_conflict);

                boolean found = newInstallCache
                    .parallelStream()
                    .anyMatch((v2) -> v2.getPackageSlug().equals(conflict.a()) && v2.getVersion().equals(conflict.b()));

                if (found) {
                    didConflict = true;
                    Bootstrap.LOGGER.info("    Package %s:%s conflicts with %s:%s", pkg.getPackageSlug(), pkg.getVersion(), conflict.a(), conflict.b());
                }
            }
        }

        // Figure out if any dependencies have conflicts.
        for (AionPackage.Version pkg : dependencies) {
            for (String interim_conflict : pkg.getConflicts()) {
                Pair<String, String> conflict = parseVersion(interim_conflict);

                boolean found = newInstallCache
                    .parallelStream()
                    .anyMatch((v2) -> v2.getPackageSlug().equals(conflict.a()) && v2.getVersion().equals(conflict.b()));

                if (found) {
                    didConflict = true;
                    Bootstrap.LOGGER.info("    Dependency %s:%s conflicts with %s:%s", pkg.getPackageSlug(), pkg.getVersion(), conflict.a(), conflict.b());
                }
            }
        }

        // Figure out if any EXISTING packages have conflicts with any PACKAGES.
        for (AionPackage.Version pkg : Bootstrap.getInstallCache()) {
            for (String interim_conflict : pkg.getConflicts()) {
                Pair<String, String> conflict = parseVersion(interim_conflict);

                boolean found = packages
                    .parallelStream()
                    .anyMatch((v2) -> v2.getPackageSlug().equals(conflict.a()) && v2.getVersion().equals(conflict.b()));

                if (found) {
                    didConflict = true;
                    Bootstrap.LOGGER.info("    Installed package %s:%s conflicts with package %s:%s", pkg.getPackageSlug(), pkg.getVersion(), conflict.a(), conflict.b());
                }
            }
        }

        // Figure out if any EXISTING packages have conflicts with any DEPENDENCIES.
        for (AionPackage.Version pkg : Bootstrap.getInstallCache()) {
            for (String interim_conflict : pkg.getConflicts()) {
                Pair<String, String> conflict = parseVersion(interim_conflict);

                boolean found = dependencies
                    .parallelStream()
                    .anyMatch((v2) -> v2.getPackageSlug().equals(conflict.a()) && v2.getVersion().equals(conflict.b()));

                if (found) {
                    didConflict = true;
                    Bootstrap.LOGGER.info("    Installed package %s:%s conflicts with dependency %s:%s", pkg.getPackageSlug(), pkg.getVersion(), conflict.a(), conflict.b());
                }
            }
        }

        if (didConflict) {
            Bootstrap.LOGGER.info("No packages will be installed, resolve conflicts to proceed.");
            return;
        } else {
            Bootstrap.LOGGER.info("No conflicts found.");
        }

        // Dry run
        if (this.isDryRun) {
            Bootstrap.LOGGER.info("----End of dry run----");
            return;
        }

        Bootstrap.LOGGER.info("Are you sure you wish to install the following packages? (Y/n)");
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
                    if ($alreadyHave == Bootstrap.getInstallCache()) {
                        // We want to change the this message if we're in dependency resolution.
                        // We know if we're in dependency resolution because during the first iteration,
                        // $alreadyHave will be the installCache. Scroll up to see the impl.
                        Bootstrap.LOGGER.info("    %s:%s is already installed, did you mean `update`?", slug, version);
                    } else {
                        Bootstrap.LOGGER.info("    Dependency %s:%s is already installed.", slug, version);
                    }
                }

                return !alreadyHas; // Remove the package if we already have it.
            })
            .filter((entry) -> {
                String slug = entry.a();
                String version = entry.b();
//                Bootstrap.LOGGER.debug("    Looking for package: %s:%s", slug, version);

                for (AionSourceList sourcelist : Bootstrap.getSourceCache()) {
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
            Bootstrap.LOGGER.info("Could not find the following packages:");
            for (Pair<String, String> entry : packagesNotFound) {
                Bootstrap.LOGGER.info("    %s:%s", entry.a(), entry.b());
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
