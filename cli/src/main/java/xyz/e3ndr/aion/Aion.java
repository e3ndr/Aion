package xyz.e3ndr.aion;

import java.io.File;
import java.util.List;

import xyz.e3ndr.aion.configuration.Config;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionPackage.Version;
import xyz.e3ndr.aion.types.AionSourceList;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Aion {
    public static final File BASE_DIR = new File(System.getProperty("aion.basedir", "."));
    public static final FastLogger LOGGER = new FastLogger("Aion");

    private static Config _config;
    private static List<AionSourceList> _sourceCache;
    private static List<AionPackage.Version> _installCache;

    public static void setup() {
        // :^)
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

    public static List<Version> installCache() {
        if (_installCache == null) {
            _installCache = Installed.load();
        }

        return _installCache;
    }

}
