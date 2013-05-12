package com.test.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Scanner;

/**
 * nazarovtr
 */
public class VKNetworkManager {

    private static final String VK_NAME = "vk_api";
    private static final String APP_ID = "3449144";
    private static final String APP_SECRET = "hjhHIUhsdfhiuGIrefoppqi";
    private static final String SCOPE = "friends,wall";
    private static final String REDIRECT_URI = "http://test.com:8080/oauth.vk";
    private static final String OAUTH_VK = "oauth.vk.com/";
    private static final String RESPONSE_TYPE = "code";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final String USER_ID = "user_id";
    private static final String CODE = "code";

    private VKNetworkManager() {
    }

    public VKAPIProvider getNetwork(HttpServletRequest request, HttpServletResponse response) {
        Object ob = request.getSession().getAttribute(VK_NAME);
        if (ob != null) {
            return (VKAPIProvider) ob;
        }
        try {
            response.sendRedirect("http://" + OAUTH_VK + "authorize?" +
                    "client_id=" + APP_ID +
                    "&scope=" + SCOPE +
                    "&response_type=" + RESPONSE_TYPE +
                    "&redirect_uri=" + REDIRECT_URI
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void createNetwork(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        Long expiresIn = null;
        Long userId = null;
        String code = request.getParameter(CODE);
        if (code != null) {
            HttpGet get = new HttpGet("https://" + OAUTH_VK + "access_token?" +
                    "client_id=" + APP_ID +
                    "&client_secret=" + APP_SECRET +
                    "&code=" + code +
                    "&redirect_uri=" + REDIRECT_URI);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse;
            try {
                httpResponse = client.execute(get);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JSONObject json;
            try {
                json = new JSONObject(new Scanner(httpResponse.getEntity().getContent()).useDelimiter("\\A").next());
                accessToken = json.getString(ACCESS_TOKEN);
                expiresIn = json.getLong(EXPIRES_IN);
                userId = json.getLong(USER_ID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        if (accessToken != null) {
            request.getSession().setAttribute(VK_NAME, new VKAPIProvider(accessToken, expiresIn, userId));
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().setAttribute(VK_NAME, null);
    }

//    public SocialRate getSocialRate(HttpServletRequest request, HttpServletResponse response) {
//        VKAPIProvider vk = getNetwork(request, response);
//        if (vk != null) {
//            return new SocialRate(vk.getFriendsIdList("11364269").toString(), 1, 1, 1);
//        } else {
//            return SocialRate.blank();
//        }
//    }


    private static class SingletonHolder {
        public static VKNetworkManager instance = new VKNetworkManager();
    }

    public static VKNetworkManager getInstance() {
        return SingletonHolder.instance;
    }
}
