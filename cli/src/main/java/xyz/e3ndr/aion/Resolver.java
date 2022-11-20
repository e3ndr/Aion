package xyz.e3ndr.aion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionSourceList;

public class Resolver {
    public static final OkHttpClient client = new OkHttpClient();

    /* -------------------- */
    /* Location Resolving   */
    /* -------------------- */

    public static <T> T get(String location, Class<T> expected) throws IOException {
        return get(location, TypeToken.of(expected));
    }

    public static <T> T get(String location, TypeToken<T> expected) throws IOException {
        try (InputStream in = get(location)) {
            String json = IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
            return Rson.DEFAULT.fromJson(json, expected);
        }
    }

    public static InputStream get(String location) throws IOException {
        URI uri = URI.create(location);
        String scheme = uri.getScheme().toLowerCase();

        Aion.LOGGER.trace("Requesting from scheme: %s", scheme);
        switch (scheme) {
            case "file": {
                File file = new File(uri.getPath());

                return new FileInputStream(file);
            }

            case "http": {
                Aion.LOGGER.warn("HTTP sources are unsupported, attempting to rewrite to https. (%s)", uri);
                return get("https://" + location.substring("http://".length()));
            }

            case "https": {
                try (Response response = client.newCall(
                    new Request.Builder()
                        .url(uri.toString())
                        .build()
                ).execute()) {
                    return response.body().byteStream();
                }
            }

            case "ftp":
            case "sftp": {
                // TODO (S)FTP support.
                String message = String.format("TODO: %s support.", scheme);
                Aion.LOGGER.fatal(message);
                throw new IOException(message);
            }

            default:
                throw new IllegalArgumentException(String.format("Unsupported scheme '%s' for source: '%'", scheme, uri));
        }
    }

    /* -------------------- */
    /* Source Resolving     */
    /* -------------------- */

    public static AionSourceList resolve(String location) throws IOException {
        Aion.LOGGER.debug("Fetching sourcelist: %s", location);
        return resolveAdditionalSource(location, "/sourcelist.json");
    }

    @SuppressWarnings("deprecation")
    private static AionSourceList resolveAdditionalSource(String baseUrl, String file) throws IOException {
        AionSourceList sourcelist = get(baseUrl + file, AionSourceList.class);

        for (String additionalPackageList : sourcelist.getAdditionalPackages()) {
            Aion.LOGGER.debug("Fetching additional packagelist: %s%s", baseUrl, additionalPackageList);
            List<AionPackage> add = resolveAdditionalPackages(baseUrl, additionalPackageList);

            Aion.LOGGER.debug("Additional packagelist had %d packages.", add.size());
            sourcelist.getPackageList().addAll(add);
        }

        for (String additionalSourceList : sourcelist.getAdditionalSources()) {
            Aion.LOGGER.debug("Fetching additional sourcelist: %s%s", baseUrl, additionalSourceList);
            List<AionPackage> add = resolveAdditionalSource(baseUrl, additionalSourceList).getPackageList();

            Aion.LOGGER.debug("Additional sourcelist had %s packages.", add.size());
            sourcelist.getPackageList().addAll(add);
        }

        sourcelist.makeInternal(baseUrl);

        return sourcelist;
    }

    private static List<AionPackage> resolveAdditionalPackages(String baseUrl, String file) throws IOException {
        return get(baseUrl + file, AionPackage.TT_LIST);
    }

}
