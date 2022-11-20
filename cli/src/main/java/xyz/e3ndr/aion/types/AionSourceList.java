package xyz.e3ndr.aion.types;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.aion.types.AionPackage.Version;

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

    @JsonValidate
    private void $validate() {
        for (AionPackage entry : packageList) {
            entry.sourcelist = this;
        }
    }

    public @Nullable AionPackage.Version findPackage(String slug, String version) {
        Optional<Version> result = this.packageList.parallelStream()
            .filter((pkg) -> pkg.getSlug().equals(slug) || pkg.getAliases().contains(slug)) // Match the slug or alias
            .filter((pkg) -> pkg.getVersions().containsKey(version)) // See if it contains the requested version
            .map((pkg) -> pkg.getVersions().get(version)) // Create the result
            .findAny();

        if (result.isPresent()) {
            return result.get();
        } else {
            return null;
        }
    }

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
