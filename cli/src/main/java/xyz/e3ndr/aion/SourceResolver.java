package xyz.e3ndr.aion;

import java.io.IOException;
import java.util.List;

import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionSourceList;

public class SourceResolver {

    public static AionSourceList resolve(String url) throws IOException {
        Bootstrap.LOGGER.debug("Fetching sourcelist: %s", url);
        return resolveAdditionalSource(url, "/sourcelist.json");
    }

    @SuppressWarnings("deprecation")
    private static AionSourceList resolveAdditionalSource(String baseUrl, String file) throws IOException {
        AionSourceList sourcelist = HttpUtil.get(baseUrl + file, AionSourceList.class);

        for (String additionalPackageList : sourcelist.getAdditionalPackages()) {
            Bootstrap.LOGGER.debug("Fetching additional packagelist: %s%s", baseUrl, additionalPackageList);
            List<AionPackage> add = resolveAdditionalPackages(baseUrl, additionalPackageList);

            Bootstrap.LOGGER.debug("Additional packagelist had %d packages.", add.size());
            sourcelist.getPackageList().addAll(add);
        }

        for (String additionalSourceList : sourcelist.getAdditionalSources()) {
            Bootstrap.LOGGER.debug("Fetching additional sourcelist: %s%s", baseUrl, additionalSourceList);
            List<AionPackage> add = resolveAdditionalSource(baseUrl, additionalSourceList).getPackageList();

            Bootstrap.LOGGER.debug("Additional sourcelist had %s packages.", add.size());
            sourcelist.getPackageList().addAll(add);
        }

        sourcelist.makeInternal(baseUrl);

        return sourcelist;
    }

    private static List<AionPackage> resolveAdditionalPackages(String baseUrl, String file) throws IOException {
        return HttpUtil.get(baseUrl + file, AionPackage.TT_LIST);
    }

}
