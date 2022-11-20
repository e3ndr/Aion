package xyz.e3ndr.aion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    public static final OkHttpClient client = new OkHttpClient();

    public static <T> T get(String url, Class<T> expected) throws IOException {
        return Rson.DEFAULT.fromJson(get(url), expected);
    }

    public static <T> T get(String url, TypeToken<T> expected) throws IOException {
        return Rson.DEFAULT.fromJson(get(url), expected);
    }

    private static String get(String url) throws IOException {
        if (url.startsWith(".") || url.startsWith("/") || url.startsWith("file://")) {
            if (url.startsWith("file://")) {
                url = url.substring("file://".length());
            }

            File file = new File(url);
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            Aion.LOGGER.trace("File %s:\n%s", url, content);
            return content;
        }

        try (Response response = client.newCall(
            new Request.Builder()
                .url(url)
                .build()
        ).execute()) {
            String body = response.body().string();
            Aion.LOGGER.trace("Response from %s:\n%s", url, body);
            return body;
        }
    }

}
