package com.test.crawler;


import com.test.utils.MongoDBStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 11:42 AM
 */
@Component
public class FriendsCrawler {
    @Autowired
    private MongoDBStorage mongoDBStorage;
    public static void main(String[] args) {

//        request.setCharacterEncoding("UTF-8");
//        response.setCharacterEncoding("UTF-8");
//        SocialWebManager<VK> manager = VKNetworkManager.getInstance();
//        SocialRate rate = manager.getSocialRate(request, response);
    }
    public void test() throws UnknownHostException {
        mongoDBStorage.test();
    }
}
