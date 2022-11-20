package xyz.e3ndr.aion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class Config {
    private List<String> sources = null;

    @JsonValidate
    private void $validate() {
        if (this.sources == null) {
            Bootstrap.LOGGER.debug("Configured sources is null, setting to default.");
            this.sources = new ArrayList<>(Arrays.asList("test///"));
        }
    }

    public void save() {
        try {
            Files.write(
                Bootstrap.CONFIG_FILE.toPath(),
                Rson.DEFAULT.toJsonString(this).getBytes(StandardCharsets.UTF_8)
            );
            Bootstrap.LOGGER.debug("Updated config.");
        } catch (IOException e) {
            Bootstrap.LOGGER.severe("Unable to save config, changes/settings will NOT persist.\n%s", e.getMessage());
        }
    }

}
