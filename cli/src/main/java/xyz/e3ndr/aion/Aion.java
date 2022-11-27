package xyz.e3ndr.aion;

import java.io.File;
import java.util.List;
import java.util.Set;

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
    }

    public static void teardown() {
        _config = null;
        _sourceCache = null;
        _installCache = null;
        System.gc();
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
