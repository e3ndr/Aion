package xyz.e3ndr.aion.types;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class ExtractionPlan {
    private String base = "";
    private String[] keep = null;
    private String[] discard = {};

    public boolean allowFile(String filename) {
        for (String regex : this.discard) {
            if (filename.matches(regex)) {
                return false;
            }
        }

        // Null means ALL.
        if (this.keep == null) return true;

        for (String regex : this.keep) {
            if (filename.matches(regex)) {
                return true;
            }
        }

        return false;
    }

}