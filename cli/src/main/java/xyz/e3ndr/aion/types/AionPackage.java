package xyz.e3ndr.aion.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import co.casterlabs.commons.platform.Arch;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class AionPackage {
    public static final TypeToken<List<AionPackage>> TT_LIST = new TypeToken<List<AionPackage>>() {
    };

    private String slug;
    private List<String> aliases = Collections.emptyList();
    private String latest;

    @JsonExclude
    private Map<String, Version> versions = new HashMap<>();

    @JsonValidate
    private void $validate() {
        // Get the LATEST version info, add it to the map. It's fine if this.latest
        // doesn't actually correspond to a version, we deal with it later.
        this.versions.put(this.latest, this.versions.get(this.latest));

        for (Map.Entry<String, Version> entry : this.versions.entrySet()) {
            Version version = entry.getValue();
            version.version = entry.getKey();
            version.packageSlug = this.slug;
        }
    }

    @Getter
    @JsonClass(exposeAll = true)
    public static class Version {
        public static final TypeToken<List<Version>> TT_LIST = new TypeToken<List<Version>>() {
        };

        private String packageSlug;
        private String version;
        private String patch;

        private List<String> depends = Collections.emptyList();

        @JsonExclude
        private Map<String, String> commands = new HashMap<>();

        @JsonExclude
        private Map<Arch, Map<OSDistribution, String>> binaries = new HashMap<>();

        @JsonField("extract")
        private ExtractionPlan extractionPlan;

        @Getter
        @JsonClass(exposeAll = true)
        public static class ExtractionPlan {
            private String base = "";
            private String[] keep = null;
            private String[] discard = {};
        }

        // Rakurai patches

        @JsonDeserializationMethod("commands")
        private void $deserialize_commands(JsonElement e) throws JsonValidationException, JsonParseException {
            for (Entry<String, JsonElement> entry : e.getAsObject()) {
                this.commands.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        @JsonSerializationMethod("commands")
        private JsonElement $serialize_commands() {
            return Rson.DEFAULT.toJson(this.commands);
        }

        @JsonDeserializationMethod("binaries")
        private void $deserialize_binaries(JsonElement e) throws JsonValidationException, JsonParseException {
            for (Entry<String, JsonElement> entry : e.getAsObject()) {
                Map<OSDistribution, String> dists = new HashMap<>();

                for (Entry<String, JsonElement> entry2 : entry.getValue().getAsObject()) {
                    OSDistribution key = OSDistribution.valueOf(entry2.getKey());
                    dists.put(key, entry2.getValue().getAsString());
                }

                Arch key = Arch.valueOf(entry.getKey());
                this.binaries.put(key, dists);
            }
        }

        @JsonSerializationMethod("binaries")
        private JsonElement $serialize_binaries() {
            return Rson.DEFAULT.toJson(this.binaries);
        }

    }

    // Rakurai patches

    @JsonDeserializationMethod("versions")
    private void $deserialize_versions(JsonElement e) throws JsonValidationException, JsonParseException {
        for (Entry<String, JsonElement> entry : e.getAsObject()) {
            Version version = Rson.DEFAULT.fromJson(entry.getValue(), Version.class);
            this.versions.put(entry.getKey(), version);
        }
    }

    @JsonSerializationMethod("versions")
    private JsonElement $serialize_versions() {
        return Rson.DEFAULT.toJson(this.versions);
    }

}
