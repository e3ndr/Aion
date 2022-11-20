package xyz.e3ndr.aion.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.casterlabs.commons.platform.Arch;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class AionPackage {
    public static final TypeToken<List<AionPackage>> TT_PACKAGE_LIST = new TypeToken<List<AionPackage>>() {
    };

    private String slug;
    private String latest;

    private Map<String, Version> versions = new HashMap<>();

    @JsonValidate
    private void $validate() {
        // Get the LATEST version info, add it to the map. It's fine if this.latest
        // doesn't actually correspond to a version, we deal with it later.
        this.versions.put(this.latest, this.versions.get(this.latest));
    }

    @Getter
    @JsonClass(exposeAll = true)
    public static class Version {
        private String patch;

        private List<String> depends = Collections.emptyList();
        private List<String> conflicts = Collections.emptyList();

        private Map<String, String> commands = new HashMap<>();

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

    }

}
