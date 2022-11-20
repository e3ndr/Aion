package xyz.e3ndr.aion.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.aion.Bootstrap;

@Getter
@JsonClass(exposeAll = true)
public class Config {
    private static final File FILE = new File("config.json");

    private List<String> sources = null;

    @JsonValidate
    private void $validate() {
        if (this.sources == null) {
            Bootstrap.LOGGER.debug("Configured sources is null, setting to default.");
            this.sources = new LinkedList<>(Arrays.asList("test///"));
        }
    }

    public void save() {
        try {
            Files.write(
                FILE.toPath(),
                Rson.DEFAULT.toJsonString(this).getBytes(StandardCharsets.UTF_8)
            );
            Bootstrap.LOGGER.debug("Updated config.");
        } catch (IOException e) {
            Bootstrap.LOGGER.severe("Unable to save config, changes/settings will NOT persist.\n%s", e.getMessage());
        }
    }

    public static Config load() {
        if (FILE.exists()) {
            try {
                String content = new String(
                    Files.readAllBytes(FILE.toPath()),
                    StandardCharsets.UTF_8
                );
                return Rson.DEFAULT.fromJson(content, Config.class);
            } catch (IOException e) {
                Bootstrap.LOGGER.fatal("Unable to parse config file:\n%s", e);
                System.exit(1);
                return null; // Compilier.
            }
        } else {
            Config config = new Config();
            config.save();
            return config;
        }
    }

}
