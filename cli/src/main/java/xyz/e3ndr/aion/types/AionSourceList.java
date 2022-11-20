package xyz.e3ndr.aion.types;

import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class AionSourceList {
    public static final TypeToken<List<AionSourceList>> TT_LIST = new TypeToken<List<AionSourceList>>() {
    };

    private String name;
    private String url;

    private String[] additionalSources;
    private String[] additionalPackages;

    private List<AionPackage> packageList = new LinkedList<>();

    /**
     * @deprecated Do not use, only used internally.
     */
    @Deprecated
    public void makeInternal(String url) {
        this.additionalSources = null;
        this.additionalPackages = null;
        this.url = url;
    }

}
