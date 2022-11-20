package xyz.e3ndr.aion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionSourceList;

public class Resolver {
    public static final OkHttpClient client = new OkHttpClient();

    public static <T> T get(String url, Class<T> expected) throws IOException {
        return Rson.DEFAULT.fromJson(get(url), expected);
    }

    public static <T> T get(String url, TypeToken<T> expected) throws IOException {
        return Rson.DEFAULT.fromJson(get(url), expected);
    }

    private static String get(String location) throws IOException {
        URI uri = URI.create(location);
        String scheme = uri.getScheme().toLowerCase();

        Aion.LOGGER.trace("Requesting from scheme: %s", scheme);
        switch (scheme) {
            case "file": {
                File file = new File(uri.getPath());
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

                Aion.LOGGER.trace("File %s:\n%s", uri, content);
                return content;
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
                    String body = response.body().string();
                    Aion.LOGGER.trace("Response from %s:\n%s", uri, body);
                    return body;
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

    public static AionSourceList resolve(String url) throws IOException {
        Aion.LOGGER.debug("Fetching sourcelist: %s", url);
        return resolveAdditionalSource(url, "/sourcelist.json");
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
