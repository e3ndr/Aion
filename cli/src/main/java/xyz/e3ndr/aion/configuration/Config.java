package xyz.e3ndr.aion.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.commands.AionCommands;

@Getter
@JsonClass(exposeAll = true)
public class Config {
    private static final File FILE = new File(Aion.BASE_DIR, "config.json");
    private static final List<String> DEFAULT_SOURCES = Arrays.asList("test///");

    private List<String> sources = new LinkedList<>(DEFAULT_SOURCES);
    private @JsonExclude Map<String, Pair<String, String>> pathConfiguration = new HashMap<>();

    @JsonValidate
    private void $validate() {
        if (this.sources == null) {
            Aion.LOGGER.debug("Configured sources is null, setting to default.");
            this.sources = new LinkedList<>(DEFAULT_SOURCES);
        }
    }

    public void save() {
        try {
            Files.write(
                FILE.toPath(),
                Rson.DEFAULT.toJsonString(this).getBytes(StandardCharsets.UTF_8)
            );
            Aion.LOGGER.debug("Updated config.");
        } catch (IOException e) {
            Aion.LOGGER.severe("Unable to save config, changes/settings will NOT persist.\n%s", e.getMessage());
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
                Aion.LOGGER.fatal("Unable to parse config file:\n%s", e);
                System.exit(1);
                return null; // Compilier.
            }
        } else {
            Config config = new Config();
            config.save();
            return config;
        }
    }

    // Rakurai patches

    @JsonDeserializationMethod("pathConfiguration")
    private void $deserialize_pathConfiguration(JsonElement e) {
        for (Entry<String, JsonElement> entry : e.getAsObject()) {
            Pair<String, String> configuration = AionCommands.parseVersion(entry.getValue().getAsString());

            this.pathConfiguration.put(entry.getKey(), configuration);
        }
    }

    @JsonSerializationMethod("pathConfiguration")
    private JsonElement $serialize_pathConfiguration() {
        JsonObject result = new JsonObject();

        for (Entry<String, Pair<String, String>> entry : this.pathConfiguration.entrySet()) {
            String value = String.format("%s:%s", entry.getValue().a(), entry.getValue().b());

            result.put(entry.getKey(), value);
        }

        return result;
    }

}
