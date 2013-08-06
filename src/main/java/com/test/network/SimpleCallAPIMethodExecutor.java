package com.test.network;

import com.test.data.VKDataTags;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * User: ragvena
 * Date: 5/12/13
 * Time: 12:51 PM
 */
public class SimpleCallAPIMethodExecutor {
    public static final String API_URL = "https://api.vk.com/method/";
    private String accessToken;
    private Random sleepTime;
    private static final Integer RETRY_COUNTER = 0;
    private static final Integer MAX_SLEEP_DURATION = 25;
    private static final Object EMPTY_OBJECT = new Object();

    private static Logger LOGGER = Logger.getLogger(VKAPIProvider.class);

    public SimpleCallAPIMethodExecutor(String accessToken) {
        this.accessToken = accessToken;
        this.sleepTime = new Random();
    }

    private JSONObject callAPIMethod(String method, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL).append(method)
                .append("?access_token=").append(accessToken);

        for (String key : params.keySet()) {
            urlBuilder.append("&").append(key).append("=").append(params.get(key));
        }
        HttpGet get = new HttpGet(urlBuilder.toString());
        HttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;
        try {
            httpResponse = client.execute(get);
        } catch (IOException e) {
//            throw new RuntimeException(e);
            return new JSONObject();
        }
        try {
            return new JSONObject(new Scanner(httpResponse.getEntity().getContent()).useDelimiter("\\A").next());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(String method, Map<String, String> requestParameters) throws InterruptedException {
        Object result = callAPIMethod(method, requestParameters).opt(VKDataTags.RESPONSE);
        Integer localRetryCounter = 1;
        while (result == null && localRetryCounter <= RETRY_COUNTER) {
            result = callAPIMethod(method, requestParameters).opt(VKDataTags.RESPONSE);
            Thread.sleep(sleepTime.nextInt(MAX_SLEEP_DURATION));
            LOGGER.info("retry:\t" + localRetryCounter);
            localRetryCounter++;
        }
        return result == null ? EMPTY_OBJECT : result;
    }
}
