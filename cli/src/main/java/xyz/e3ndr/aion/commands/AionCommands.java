package xyz.e3ndr.aion.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.AllArgsConstructor;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Installed.InstallCacheEntry;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionSourceList;

/**
 * This entire class is essentially a mapping of the commands to easy-to-use
 * methods. All command classes are annotated with {@link AllArgsConstructor} to
 * generate the constructors, just make sure that this class stays up-to-date
 * with the new method signatures (fix any errors).
 */
public class AionCommands {

    /* ---- Helpers ---- */

    /**
     * @implNote null result means abort.
     */
    public static @Nullable List<AionPackage.Version> searchPackages(List<Pair<String, String>> packagesToFind, Set<InstallCacheEntry> $alreadyHave) {
        List<AionPackage.Version> result = new LinkedList<>();
        List<Pair<String, String>> missing = new LinkedList<>();

        for (Pair<String, String> toFind : packagesToFind) {
            AionPackage.Version found = findPackage(toFind, $alreadyHave);

            if (found == null) {
                missing.add(toFind);
            } else {
                result.add(found);
            }
        }

        // Couldn't find some packages, abort.
        if (!missing.isEmpty()) {
            Aion.LOGGER.fatal("Could not find the following packages:");
            for (Pair<String, String> entry : missing) {
                Aion.LOGGER.fatal("    %s:%s", entry.a(), entry.b());
            }
            return null;
        }

        return result;
    }

    /**
     * @implNote null result means abort.
     */
    public static @Nullable List<AionPackage.Version> findPackages(List<Pair<String, String>> packagesToFind, Set<Installed.InstallCacheEntry> $alreadyHave, boolean silent) {
        Pair<List<AionPackage.Version>, List<Pair<String, String>>> result = findPackagesAndMissing(packagesToFind, $alreadyHave, silent);

        // Couldn't find some packages, abort.
        if (!result.b().isEmpty()) {
            Aion.LOGGER.fatal("Could not find the following packages:");
            for (Pair<String, String> entry : result.b()) {
                Aion.LOGGER.fatal("    %s:%s", entry.a(), entry.b());
            }
            return null;
        }

        return result.a();
    }

    public static Pair<List<AionPackage.Version>, List<Pair<String, String>>> findPackagesAndMissing(List<Pair<String, String>> packagesToFind, Set<Installed.InstallCacheEntry> $alreadyHave, boolean silent) {
        if (packagesToFind.isEmpty()) return new Pair<>(Collections.emptyList(), Collections.emptyList());

        List<AionPackage.Version> found = new LinkedList<>();

        List<Pair<String, String>> packagesNotFound = packagesToFind
            .parallelStream()
            // Make sure we're not already installing a required package.
            .filter((entry) -> {
                String slug = entry.a();
                String version = entry.b();

                boolean alreadyHas = $alreadyHave
                    .parallelStream()
                    .anyMatch((a) -> a.pkg.getSlug().equals(slug) && a.version.equals(version));

                if (alreadyHas && !silent) {
                    if ($alreadyHave == Aion.installCache()) {
                        // We want to change the this message if we're in dependency resolution.
                        // We know if we're in dependency resolution because during the first iteration,
                        // $alreadyHave will be the installCache. Scroll up to see the impl.
                        Aion.LOGGER.warn("    %s:%s is already installed, did you mean `update`?", slug, version);
                    } else {
                        Aion.LOGGER.info("    Dependency %s:%s is already installed.", slug, version);
                    }
                }

                return !alreadyHas; // Remove the package if we already have it.
            })
            .filter((entry) -> {
                AionPackage.Version v = findPackage(entry, $alreadyHave);

                if (v == null) {
                    return true; // Keep the current package.
                } else {
                    found.add(v);
                    return false; // Remove the current package.
                }
            })
            .collect(Collectors.toList());
        packagesToFind.clear();

        return new Pair<>(found, packagesNotFound);
    }

    public static @Nullable AionPackage.Version findPackage(Pair<String, String> packageToFind, Set<Installed.InstallCacheEntry> $alreadyHave) {
        String slug = packageToFind.a();
        String version = packageToFind.b();
//        Aion.LOGGER.debug("    Looking for package: %s:%s", slug, version);

        for (AionSourceList sourcelist : Aion.sourceCache()) {
            AionPackage.Version v = sourcelist.findPackage(slug, version);
            if (v == null) continue; // Next source.

            return v;
        }

        return null;
    }

    public static Pair<String, String> parseVersion(String pkg) {
        String[] parts = pkg.split(":");

        if (parts.length > 2) {
            throw new IllegalArgumentException("Package declaration must be in the form of PACKAGE[:VERSION], got: " + pkg);
        }

        String slug = parts[0].toLowerCase();
        String version = "LATEST";

        if (parts.length == 2) {
            version = parts[1];
        }

        if (version.equalsIgnoreCase("LATEST")) {
            // We have to figure out the real latest version from the sources cache.
            // The install cache will be inaccurate during package resolution.
            List<AionPackage> allPackages = new LinkedList<>();
            Aion
                .sourceCache()
                .parallelStream()
                .map((sl) -> sl.getPackageList())
                .forEach(allPackages::addAll);

            Optional<String> latest = allPackages
                .parallelStream()
                .filter((p) -> p.getSlug().equalsIgnoreCase(slug))
                .map((p) -> p.getLatest())
                .findAny();

            if (latest.isPresent()) {
                version = latest.get();
            }
        }

        return new Pair<>(slug, version);
    }

    public static List<Pair<String, String>> parseAllVersions(String... pkgs) {
        List<Pair<String, String>> result = new ArrayList<>(pkgs.length);

        for (String pkg : pkgs) {
            result.add(parseVersion(pkg));
        }

        return result;
    }

    /* ---- Install ---- */

    public static void install(boolean isDryRun, boolean autoYes, boolean reinstall, boolean soft, String... packagesToInstall) {
        new CommandInstall(isDryRun, autoYes, reinstall, packagesToInstall, true, soft)
            .run();
    }

    /* ---- List ---- */

    public static void list() {
        new CommandList()
            .run();
    }

    /* ---- Path ---- */

    public static void path_list() {
        new CommandPath()
            .run();
    }

    public static void path_what(String... packagesToCheck) {
        new CommandPath.CommandPathWhat(packagesToCheck)
            .run();
    }

    public static void path_update(boolean soft, boolean noRebuild, String packageToUse, String... commandsToUpdate) {
        new CommandPath.CommandPathUpdate(soft, noRebuild, packageToUse, commandsToUpdate)
            .run();
    }

    public static void path_rebuild(boolean force) {
        new CommandPath.CommandPathRebuild(force)
            .run();
    }

    /* ---- Remove ---- */

    public static void remove(boolean isDryRun, boolean autoYes, String... packagesToRemove) {
        new CommandRemove(isDryRun, autoYes, packagesToRemove)
            .run();
    }

    /* ---- Run ---- */

    public static void run(boolean autoInstall, String[] packagesToUse, String... command) {
        new CommandRun(autoInstall, packagesToUse, command)
            .run();
    }

    /* ---- Sources ---- */

    public static void sources_list() {
        new CommandSources()
            .run();
    }

    public static void sources_add(String... sourcesToAdd) {
        new CommandSources.CommandSourcesAdd(sourcesToAdd)
            .run();
    }

    public static void sources_remove(String... sourcesToRemove) {
        new CommandSources.CommandSourcesRemove(sourcesToRemove)
            .run();
    }

    public static void sources_refresh() {
        new CommandSources.CommandSourcesRefresh()
            .run();
    }

    /* ---- Update ---- */

    public static void update(boolean isDryRun, boolean autoYes) {
        new CommandUpdate(isDryRun, autoYes)
            .run();
    }

}
