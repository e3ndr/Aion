package xyz.e3ndr.aion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.aion.configuration.Config;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Installed.InstallCacheEntry;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionSourceList;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Aion {
    // @formatter:off
    public static final File BASE_DIR     = new File(System.getProperty("aion.basedir", "."));
    
    public static final File DOWNLOAD_DIR = new File(BASE_DIR, "download-cache");
    public static final File PACKAGES_DIR = new File(BASE_DIR, "packages");
    public static final File PATH_DIR     = new File(BASE_DIR, "path");
    // @formatter:on

    public static final FastLogger LOGGER = new FastLogger("Aion");
    public static final String TEMP_FILE_EXT = ".tmp";

    // TODO move these over to sqlite.
    private static Config _config;
    private static List<AionSourceList> _sourceCache;
    private static Set<InstallCacheEntry> _installCache;

    public static void setup() {
        DOWNLOAD_DIR.mkdirs();
        PACKAGES_DIR.mkdirs();
        PATH_DIR.mkdirs();

        IOUtil.DEFAULT_BUFFER_SIZE = 1024; // 1kb

        // Add AION to the local path.
        AsyncTask.createNonDaemon(() -> {
            try {
                Files.write(
                    new File(PATH_DIR, "aion")
                        .toPath(),
                    Resolver
                        .getString("resource:///path/aion")
                        .getBytes()
                );

                Files.write(
                    new File(PATH_DIR, "aion.bat")
                        .toPath(),
                    Resolver
                        .getString("resource:///path/aion.bat")
                        .getBytes()
                );
            } catch (IOException e) {
                LOGGER.warn("Unable to write the `aion` command to path. Things may break.\n%s", e.getMessage());
            }
        });

    }

    public static void updatePath(String pkg, String version, String command) {
        AsyncTask.createNonDaemon(() -> {
            try {
                String unixExecutable = Resolver.getString("resource:///path/path_format");
                String windowsExecutable = Resolver.getString("resource:///path/path_format.bat");

                unixExecutable = unixExecutable
                    .replace("{package}", pkg)
                    .replace("{version}", version)
                    .replace("{command}", command);
                windowsExecutable = windowsExecutable
                    .replace("{package}", pkg)
                    .replace("{version}", version)
                    .replace("{command}", command + ".bat");

                Files.write(
                    new File(PATH_DIR, command)
                        .toPath(),
                    unixExecutable.getBytes()
                );
                Files.write(
                    new File(PATH_DIR, command + ".bat")
                        .toPath(),
                    windowsExecutable.getBytes()
                );
            } catch (IOException e) {
                LOGGER.warn("Unable to write the `aion` command to path. Things may break.\n%s", e.getMessage());
            }
        });
    }

    // Getters, we want to load these things on-demand.

    public static Config config() {
        if (_config == null) {
            _config = Config.load();
        }
        return _config;
    }

    public static List<AionSourceList> sourceCache() {
        if (_sourceCache == null) {
            _sourceCache = Sources.load();
        }
        return _sourceCache;
    }

    public static Set<InstallCacheEntry> installCache() {
        if (_installCache == null) {
            _installCache = Installed.load();
        }
        return _installCache;
    }

}
