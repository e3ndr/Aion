package xyz.e3ndr.aion;

import java.util.List;

import xyz.e3ndr.aion.configuration.Config;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionPackage.Version;
import xyz.e3ndr.aion.types.AionSourceList;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Aion {
    public static final FastLogger LOGGER = new FastLogger("Aion");

    private static Config config;
    private static List<AionSourceList> sourceCache;
    private static List<AionPackage.Version> installCache;

    public static void setup() {
        // :^)
    }

    // Getters, we want to load these things on-demand.

    public static Config config() {
        if (config == null) {
            config = Config.load();
        }

        return config;
    }

    public static List<AionSourceList> sourceCache() {
        if (sourceCache == null) {
            sourceCache = Sources.load();
        }

        return sourceCache;
    }

    public static List<Version> installCache() {
        if (installCache == null) {
            installCache = Installed.load();
        }

        return installCache;
    }

}
