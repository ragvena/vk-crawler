package com.test.network;

import com.test.data.VKDataTags;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 10:58 AM
 */
public class VKAPIProvider {


    public static final String FIELDS_REQUEST_PARAMETER_VALUE = "sex,bdate,city";
    private static Logger LOGGER = Logger.getLogger(VKAPIProvider.class);
    private String accessToken;
    private Long expiresIn;
    private Long userId;
    private SimpleCallAPIMethodExecutor apiMethodExecutor;

    public VKAPIProvider(String accessToken, Long expiresIn, Long userId) {
        this.accessToken = accessToken;
        this.apiMethodExecutor = new SimpleCallAPIMethodExecutor(accessToken);
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

    public JSONArray getFriendsIdList(String userId) throws InterruptedException {
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put(VKDataTags.UID, userId);
        return (JSONArray) apiMethodExecutor.execute(VKAPIMethod.FRIENDS_GET, requestParameters);
    }

    public JSONArray getUsersDetailInfo(String usersId, String infoFields) throws JSONException, InterruptedException {
        infoFields = FIELDS_REQUEST_PARAMETER_VALUE;
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put(VKDataTags.UIDS, usersId);
        requestParameters.put(VKDataTags.FIELDS, infoFields);
        return (JSONArray) apiMethodExecutor.execute(VKAPIMethod.USERS_GET, requestParameters);
    }

    public JSONObject getSubscribePages(String userId) throws JSONException, InterruptedException {
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put(VKDataTags.UID, userId);
        requestParameters.put(VKDataTags.EXTENDED, "0");
        requestParameters.put(VKDataTags.COUNT, "200");
        return (JSONObject) apiMethodExecutor.execute(VKAPIMethod.USERS_GEt_SUBSCRIPTIONS, requestParameters);
    }

    public JSONArray getSubscribePagesDetailedInfo(String pagesIdList) throws JSONException, InterruptedException {
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put(VKDataTags.GIDS, pagesIdList);
        requestParameters.put(VKDataTags.FIELDS, VKDataTags.DESCRIPTION);
        return (JSONArray) apiMethodExecutor.execute(VKAPIMethod.GROUPS_GET_BY_ID, requestParameters);
    }
}
