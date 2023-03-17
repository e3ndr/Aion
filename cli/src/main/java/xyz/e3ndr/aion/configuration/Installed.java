package xyz.e3ndr.aion.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.AionPackage;

@Getter
@JsonClass(exposeAll = true)
public class Installed {
    private static final File FILE = new File(Aion.BASE_DIR, "install-cache.json");

    public static final TypeToken<Set<InstallCacheEntry>> TT_SET = new TypeToken<Set<InstallCacheEntry>>() {
    };

    public static void save(Set<InstallCacheEntry> installList) {
        try {
            Files.write(
                FILE.toPath(),
                Rson.DEFAULT
                    .toJson(installList)
                    .toString(true)
                    .getBytes(StandardCharsets.UTF_8)
            );
            Aion.LOGGER.info("Updated install cache.");
        } catch (IOException e) {
            Aion.LOGGER.fatal("Unable to save install cache:\n%s", e.getMessage());
            System.exit(1);
        }
    }

    public static Set<InstallCacheEntry> load() {
        if (FILE.exists()) {
            try {
                String content = new String(
                    Files.readAllBytes(FILE.toPath()),
                    StandardCharsets.UTF_8
                );

                return new HashSet<>(
                    Rson.DEFAULT.fromJson(content, TT_SET)
                );
            } catch (IOException e) {
                Aion.LOGGER.fatal("Unable to parse install cache:\n%s", e);
                System.exit(1);
                return null; // Compilier.
            }
        } else {
            return new HashSet<>();
        }
    }

    @JsonClass(exposeAll = true)
    @AllArgsConstructor
    public static class InstallCacheEntry {
        public final AionPackage pkg;
        public final String version;

        // For Rson.
        public InstallCacheEntry() {
            this(null, null);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.pkg, this.version);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            return this.hashCode() == obj.hashCode();
        }

    }

}
