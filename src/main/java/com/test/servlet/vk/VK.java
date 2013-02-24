package com.test.servlet.vk;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * nazarovtr
 */
public class VK {

    public static final Logger LOGGER = Logger.getLogger(VK.class);

    public static final String API_URL = "https://api.vk.com/method/";
    public static final String METHOD_GET_FRIENDS = "friends.get";
    public static final String METHOD_GET_USER_INFO = "users.get";
    private String accessToken;
    private Long expiresIn;
    private Long userId;

    public VK(String accessToken, Long expiresIn, Long userId) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    private JSONObject callAPIMethod(String method, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL).append(method)
                .append("?access_token=").append(accessToken);

        for(String key : params.keySet()) {
            urlBuilder.append("&").append(key).append("=").append(params.get(key));
        }
        HttpGet get = new HttpGet(urlBuilder.toString());
        HttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;
        try {
            httpResponse = client.execute(get);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return new JSONObject(new Scanner(httpResponse.getEntity().getContent()).useDelimiter("\\A").next());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject callAPIMethod(String method) {
        return callAPIMethod(method, Collections.<String, String>emptyMap());
    }

    public Integer getFriendCount() {
        try {
            Map<String, String> params = new HashMap<String, String>();
//            params.put("uid", userId.toString());
            params.put("uid", "7998");
            return callAPIMethod(METHOD_GET_FRIENDS, params).getJSONArray("response").length();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public Integer getPostCount() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("uid", userId.toString());
            return callAPIMethod(METHOD_GET_FRIENDS, params).getJSONArray("response").length();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserName() {
        try {
            Map<String, String> params = new HashMap<String, String>();
//            params.put("uids", userId.toString());
//            params.put("uids", "7998,21904,122568,217205,285964");
            params.put("uids", "827,1113");
            params.put("fields", "sex,bdate,relation");
            LOGGER.info("test");
            JSONObject json = callAPIMethod(METHOD_GET_USER_INFO, params).getJSONArray("response").getJSONObject(0);
            return json.getString("first_name") + " " +json.getString("last_name");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getRepostCount() {
        return 1;
    }
}
