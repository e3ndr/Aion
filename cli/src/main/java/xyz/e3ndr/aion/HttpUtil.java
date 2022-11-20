package xyz.e3ndr.aion;

import java.io.IOException;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    public static final OkHttpClient client = new OkHttpClient();

    public static <T> T get(String url, Class<T> expected) throws IOException {
        try (Response response = client.newCall(
            new Request.Builder()
                .url(url)
                .build()
        ).execute()) {
            String body = response.body().string();
            Bootstrap.LOGGER.trace("Response from %s:\n%s", url, body);
            return Rson.DEFAULT.fromJson(body, expected);
        }
    }

    public static <T> T get(String url, TypeToken<T> expected) throws IOException {
        try (Response response = client.newCall(
            new Request.Builder()
                .url(url)
                .build()
        ).execute()) {
            String body = response.body().string();
            Bootstrap.LOGGER.trace("Response from %s:\n%s", url, body);
            return Rson.DEFAULT.fromJson(body, expected);
        }
    }

}
