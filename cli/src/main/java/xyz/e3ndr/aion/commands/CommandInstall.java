package xyz.e3ndr.aion.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.io.IOUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.Resolver;
import xyz.e3ndr.aion.UserInput;
import xyz.e3ndr.aion.Util;
import xyz.e3ndr.aion.archive.Archives;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Installed.InstallCacheEntry;
import xyz.e3ndr.aion.types.AionPackage;

@NoArgsConstructor
@AllArgsConstructor
@Command(name = "install", description = "Installs the specified list of packages.")
public class CommandInstall implements Runnable {

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

    @Option(names = {
            "-re",
            "--reinstall"
    }, description = "Allows you to fully reinstall a package and it's dependencies without having to remove it first.")
    private boolean reinstall = false;

    @Parameters(arity = "1..*", description = "The list of packages to install. Ommiting version will install the latest available.", paramLabel = "PACKAGE[:VERSION]")
    private String[] interim_packagesToInstall;

    private boolean silent = false;

    @SneakyThrows
    @Override
    public void run() {
        List<Pair<String, String>> packagesToFind = AionCommands.parseAllVersions(this.interim_packagesToInstall);

        Set<InstallCacheEntry> currentInstallCache = Aion.installCache();
        Set<InstallCacheEntry> predictedNewInstallCache = new HashSet<>();

        if (!this.reinstall) {
            // We need to make sure we don't already have the packages/dependencies.
            predictedNewInstallCache.addAll(currentInstallCache);
        }

        // Look for the packages in the source cache.
        Aion.LOGGER.info("Looking for packages...");
        List<AionPackage.Version> packages = AionCommands.findPackages(packagesToFind, this.reinstall ? Collections.emptySet() : currentInstallCache, this.silent); // There's a comment below referring to this line.
        if (packages == null) return; // The error message will already be printed.

        if (packages.size() == 0) {
            Aion.LOGGER.info("No packages will be installed.");
        } else {
            Aion.LOGGER.info("The following packages will be installed:");
            for (AionPackage.Version version : packages) {
                Aion.LOGGER.info("    %s:%s (%s) %s", version.getPkg().getSlug(), version.getVersion(), version.getPatch(), version.getPkg().getAliases());
            }
        }

        packages
            .parallelStream()
            .forEach((v) -> predictedNewInstallCache.add(new InstallCacheEntry(v.getPkg(), v.getVersion())));

        Aion.LOGGER.info("Resolving dependencies...");
        List<AionPackage.Version> dependencies = resolveDependencies(packages, predictedNewInstallCache, this.silent);

        if (dependencies.size() == 0) {
            Aion.LOGGER.info("No dependencies will be installed.");
        } else {
            Aion.LOGGER.info("The following dependencies will be installed:");
            for (AionPackage.Version version : dependencies) {
                Aion.LOGGER.info("    %s:%s (%s) %s", version.getPkg().getSlug(), version.getVersion(), version.getPatch(), version.getPkg().getAliases());
            }

            dependencies
                .parallelStream()
                .forEach((v) -> predictedNewInstallCache.add(new InstallCacheEntry(v.getPkg(), v.getVersion())));
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

        if (!this.autoYes) {
            Aion.LOGGER.info("Are you sure you wish to install the above packages? (Y/n)");

            if (!UserInput.confirm()) {
                Aion.LOGGER.info("Aborting install.");
                return;
            }
        }

        for (AionPackage.Version version : Util.concat(dependencies, packages)) {
            String binaryLocation = version.getBinaryLocation(Platform.arch, Platform.osDistribution);
            if (binaryLocation == null) {
                Aion.LOGGER.fatal("Could not find a binary for %s (%s) for package %s:%s", Platform.osDistribution, Platform.arch, version.getPkg().getSlug(), version.getVersion());
                return;
            }

            if (!binaryLocation.contains("://")) {
                // Relative URLs needs to be modified.
                binaryLocation = version.getPkg().getSourcelist().getUrl() + binaryLocation;
            }

            Archives.Format format = Archives.probeFormat(binaryLocation);
            if (format == null) {
                Aion.LOGGER.fatal("Unable to figure out archive format for: %s", binaryLocation);
                return;
            }

            File downloadWorkingDir = new File(
                Aion.DOWNLOAD_DIR,
                String.format("%s/%s", version.getPkg().getSlug(), version.getVersion())
            );
            File downloadedFile = new File(downloadWorkingDir, "package" + format.extension);

            if (downloadedFile.exists()) {
                Aion.LOGGER.info("Package binary already exists locally, using that: %s", downloadedFile);
            } else {
                File tempFile = new File(downloadedFile.toString() + Aion.TEMP_FILE_EXT);
                Aion.LOGGER.info("Downloading '%s' to '%s'", binaryLocation, tempFile);

                downloadWorkingDir.mkdirs();
                IOUtil.writeInputStreamToOutputStream(Resolver.get(binaryLocation), new FileOutputStream(tempFile));

                tempFile.renameTo(downloadedFile);
                Aion.LOGGER.info("Downloaded '%s'", downloadedFile);
            }

            format.extractor.extract(downloadedFile, downloadWorkingDir, version.getExtractionPlan());
            downloadedFile.delete();

            File packageWorkingDir = new File(
                Aion.PACKAGES_DIR,
                String.format("%s/%s", version.getPkg().getSlug(), version.getVersion())
            );
            File packageBinaryDir = new File(packageWorkingDir, "binary");
            File packageCommandDir = new File(packageWorkingDir, "commands");

            // Empty out the workingDir if it already exists. Could cause conflicts.
            Util.recursivelyDeleteDirectory(packageWorkingDir);

            // Make the directories.
            packageBinaryDir.mkdirs();
            packageCommandDir.mkdirs();

            // Copy the extracted content to the package's binary directory.
            // Then, delete the downloadWorkingDir.
            Util.recursivelyMoveDirectoryContents(downloadWorkingDir, packageBinaryDir);
            Util.recursivelyDeleteDirectory(downloadWorkingDir.getParentFile());

            for (Entry<String, String> entry : version.getCommands().entrySet()) {
                String commandName = entry.getKey();
                String command = entry.getValue();

                try {
                    String unixExecutable = Resolver.getString("resource:///path/command_format");
                    String windowsExecutable = Resolver.getString("resource:///path/command_format.bat");

                    unixExecutable = unixExecutable
                        .replace("{command}", command)
                        .replace("\\", "/"); // Correct any path separators.
                    windowsExecutable = windowsExecutable
                        .replace("{command}", command)
                        .replace("/", "\\"); // Correct any path separators.

                    File unixExecutableFile = new File(packageCommandDir, commandName);
                    File windowsExecutableFile = new File(packageCommandDir, commandName + ".bat");

                    Files.write(
                        unixExecutableFile.toPath(),
                        unixExecutable.getBytes()
                    );
                    Files.write(
                        windowsExecutableFile.toPath(),
                        windowsExecutable.getBytes()
                    );

                    unixExecutableFile.setExecutable(true);
                } catch (IOException e) {
                    Aion.LOGGER.fatal(
                        "Unable to write the `%s` command to %s:%s's commands directory.\n%s",
                        version.getPkg().getSlug(), version.getVersion(), e
                    );
                    return;
                }
            }

            // Update the path commands IF not already taken.
            AionCommands.path_update(
                true,
                true,
                String.format("%s:%s", version.getPkg().getSlug(), version.getVersion()),
                version.getCommands().keySet().toArray(new String[0])
            );

            // Update the install cache with the current progress.
            currentInstallCache.add(new InstallCacheEntry(version.getPkg(), version.getVersion()));
            Installed.save(currentInstallCache);
        }

        AionCommands.path_rebuild();
    }

    /**
     * @implNote null result means abort.
     */
    private static @Nullable List<AionPackage.Version> resolveDependencies(List<AionPackage.Version> packages, Set<Installed.InstallCacheEntry> $alreadyHave, boolean silent) {
        if (packages.isEmpty()) return Collections.emptyList();

        List<Pair<String, String>> found = new LinkedList<>();

        for (AionPackage.Version pkg : packages) {
            for (String depend : pkg.getDepends()) {
                found.add(AionCommands.parseVersion(depend));
            }
        }

        List<AionPackage.Version> dependencies = AionCommands.findPackages(found, $alreadyHave, silent);
        dependencies.addAll(resolveDependencies(dependencies, $alreadyHave, silent)); // Recurse.

        return dependencies;
    }

}
