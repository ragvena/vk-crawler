package com.test.crawler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.test.network.VKAPIProvider;
import com.test.utils.MongoDBStorage;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 8:31 PM
 */
public class Runner {
    private static final Logger LOGGER = Logger.getLogger(Runner.class);
    private static Runner instance;
    private static VKAPIProvider vkapiProvider;

    private Runner() {

    }

    public static Runner getInstance(VKAPIProvider vkapiProvider) {
        if (instance == null) {
            instance = new Runner();
        }
        instance.setVkapiProvider(vkapiProvider);
        return instance;
    }

    public static void saveFriends(String rootUserId, final Integer depth) {
        if (vkapiProvider != null) {
            Integer currentDepth = 1;
            MongoDBStorage storage = MongoDBStorage.getInstance();
            storage.userCollection.insert(new BasicDBObject("_id",rootUserId));

            while (currentDepth > depth) {
                JSONArray friends = vkapiProvider.getFriendsIdList(rootUserId);
                for (int i = 0; i < friends.length() / 10; i++) {
                    try {
                        LOGGER.info(vkapiProvider.getUsersDetailInfo(friends.get(i).toString(), ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                currentDepth++;
                LOGGER.info("yep");
            }
        } else {
            LOGGER.info("nop");
        }
    }

    public static VKAPIProvider getVkapiProvider() {
        return vkapiProvider;
    }

    private void setVkapiProvider(VKAPIProvider vkapiProvider) {
        this.vkapiProvider = vkapiProvider;
    }


}
