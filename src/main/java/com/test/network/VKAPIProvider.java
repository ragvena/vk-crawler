package com.test.network;

import com.mongodb.DBObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.test.utils.JSONToDBObjectConverter;

import java.io.IOException;
import java.util.*;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 10:58 AM
 */
public class VKAPIProvider {
    public static final String API_URL = "https://api.vk.com/method/";
    public static final String METHOD_GET_FRIENDS = "friends.get";
    public static final String METHOD_GET_USER_INFO = "users.get";
    public static final String SINGLE_UID_REQUEST_PARAMETER = "uid";
    public static final String LIST_UID_REQUEST_PARAMETER = "uids";
    public static final String FIELDS_REQUEST_PARAMETER = "fields";


    private String accessToken;
    private Long expiresIn;
    private Long userId;

    public VKAPIProvider(String accessToken, Long expiresIn, Long userId) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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

        for (String key : params.keySet()) {
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

    public JSONArray getFriendsIdList(String userId)  {
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put(SINGLE_UID_REQUEST_PARAMETER, userId);
        JSONArray friendIdList = null;
        try {
            friendIdList = callAPIMethod(METHOD_GET_FRIENDS, requestParameters).getJSONArray("response");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return friendIdList;
    }

    public JSONArray getUsersDetailInfo(String usersIdList, String infoFields) throws JSONException {
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put(LIST_UID_REQUEST_PARAMETER, usersIdList);
        requestParameters.put(FIELDS_REQUEST_PARAMETER, infoFields);
        JSONArray usersDetailInfo = callAPIMethod(METHOD_GET_USER_INFO, requestParameters).getJSONArray("response");
        return usersDetailInfo;
    }
}
