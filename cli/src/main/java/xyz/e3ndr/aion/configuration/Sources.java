package xyz.e3ndr.aion.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.AionSourceList;

@Getter
@JsonClass(exposeAll = true)
public class Sources {
    private static final File FILE = new File(Aion.BASE_DIR, "sources-cache.json");

    public static void save(List<AionSourceList> sourcesCache) {
        try {
            Files.write(
                FILE.toPath(),
                Rson.DEFAULT
                    .toJson(sourcesCache)
                    .toString(true)
                    .getBytes(StandardCharsets.UTF_8)
            );
            Aion.LOGGER.info("Updated sources cache.");
        } catch (IOException e) {
            Aion.LOGGER.fatal("Unable to save sources cache:\n%s", e.getMessage());
            System.exit(1);
        }
    }

    public static List<AionSourceList> load() {
        if (FILE.exists()) {
            try {
                String content = new String(
                    Files.readAllBytes(FILE.toPath()),
                    StandardCharsets.UTF_8
                );
                return Rson.DEFAULT.fromJson(content, AionSourceList.TT_LIST);
            } catch (IOException e) {
                Aion.LOGGER.fatal("Unable to parse sources cache:\n%s", e);
                System.exit(1);
                return null; // Compilier.
            }
        } else {
            return new LinkedList<>();
        }
    }

}
