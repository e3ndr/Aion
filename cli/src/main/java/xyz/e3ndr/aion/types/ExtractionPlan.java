package xyz.e3ndr.aion.types;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class ExtractionPlan {
    private String base = "";
    private String[] keep = null;
    private String[] discard = {};

}