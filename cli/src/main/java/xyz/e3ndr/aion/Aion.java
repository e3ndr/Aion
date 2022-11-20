package xyz.e3ndr.aion;

import java.util.List;

import lombok.Getter;
import xyz.e3ndr.aion.configuration.Config;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionSourceList;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Aion {
    public static final FastLogger LOGGER = new FastLogger("Aion");

    private static @Getter Config config;
    private static @Getter List<AionSourceList> sourceCache;
    private static @Getter List<AionPackage.Version> installCache;

    public static void setup() {
        config = Config.load();
        sourceCache = Sources.load();
        installCache = Installed.load();
    }

}
