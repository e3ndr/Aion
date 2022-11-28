package xyz.e3ndr.aion;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Patch parsing & comparisons.

    public static int[] figureOutIntness(String patch) {
        final String COMMIT_HASH_REMOVE = "\\b[0-9a-f]{5,40}\\b";
        final Pattern NUMBER_MATCHING = Pattern.compile("\\b[0-9]+\\b");

        Matcher m = NUMBER_MATCHING.matcher(
            patch
                .replace(COMMIT_HASH_REMOVE, "")
        );

        List<String> numbers = new LinkedList<>();
        while (m.find()) {
            numbers.add(m.group());
        }

        int[] intness = new int[numbers.size()];

        for (int i = 0; i < numbers.size(); i++) {
            intness[i] = Integer.parseInt(numbers.get(i));
        }

        return intness;
    }

    /**
     * @return true if intness1 is newer than intness2.
     */
    public static boolean compare(int[] intness1, int[] intness2) {
        // Longer length means older.
        if (intness1.length > intness2.length) {
            return false;
        } else if (intness1.length < intness2.length) {
            return true;
        } else {
            for (int i = 0; i < intness1.length; i++) {
                if (intness1[i] < intness2[i]) {
                    return false;
                }
            }

            return true;
        }
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
