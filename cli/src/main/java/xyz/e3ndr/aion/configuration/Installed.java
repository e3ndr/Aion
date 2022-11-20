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
import xyz.e3ndr.aion.types.AionPackage;

@Getter
@JsonClass(exposeAll = true)
public class Installed {
    private static final File FILE = new File(Aion.BASE_DIR, "install-cache.json");

    public static void save(List<AionPackage.Version> installList) {
        try {
            Files.write(
                FILE.toPath(),
                Rson.DEFAULT.toJsonString(installList).getBytes(StandardCharsets.UTF_8)
            );
            Aion.LOGGER.info("Updated install cache.");
        } catch (IOException e) {
            Aion.LOGGER.fatal("Unable to save install cache:\n%s", e.getMessage());
            System.exit(1);
        }
    }

    public static List<AionPackage.Version> load() {
        if (FILE.exists()) {
            try {
                String content = new String(
                    Files.readAllBytes(FILE.toPath()),
                    StandardCharsets.UTF_8
                );
                return Rson.DEFAULT.fromJson(content, AionPackage.Version.TT_LIST);
            } catch (IOException e) {
                Aion.LOGGER.fatal("Unable to parse install cache:\n%s", e);
                System.exit(1);
                return null; // Compilier.
            }
        } else {
            return new LinkedList<>();
        }
    }

}
