package org.yagna.lambda.util;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by asish on 8/25/18.
 */
public class HttpUtil {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static HttpUtil httpUtil;
    private OkHttpClient client;

    private HttpUtil() {
        this.client = new OkHttpClient();
    }

    public static HttpUtil getInstance() {
        if (httpUtil == null) {
            httpUtil = new HttpUtil();
        }

        return httpUtil;
    }


    public String postJsonData(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = this.client.newCall(request).execute();
        return response.body().string();
    }

    public String postRawData(String url, String json, Map<String, String> headers) throws IOException {
        Headers headerBuild = null;
        if (headers != null && headers.size() > 0) {
            headerBuild = Headers.of(headers);
        }
        RequestBody body = RequestBody.create(null, json);
        Request request = new Request.Builder()
                .url(url)
                .headers(headerBuild)
                .post(body)
                .build();
        Response response = this.client.newCall(request).execute();
        return response.body().string();
    }

    public String get(String url, Map<String, String> headers) throws IOException {
        Headers headerBuild = null;
        if (headers != null && headers.size() > 0) {
            headerBuild = Headers.of(headers);
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(headerBuild)
                .build();

        Response response = this.client.newCall(request).execute();
        return response.body().string();
    }

}
