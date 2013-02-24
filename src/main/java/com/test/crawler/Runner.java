package com.test.crawler;

import com.mongodb.*;
import com.test.network.VKAPIProvider;
import com.test.user.MongoDbUserInterface;
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
            Integer currentDepth = 0;
            Long requestCounter=0L;
            MongoDBStorage storage = MongoDBStorage.getInstance();
            storage.userCollection.insert(BasicDBObjectBuilder.start()
                    .add(MongoDbUserInterface.USER_ID, rootUserId)
                    .add(MongoDbUserInterface.FRIEND_CIRCLE, 0)
                    .add(MongoDbUserInterface.ROOT_USER, rootUserId)
                    .get()
            );
           currentDepth=2;
            while (currentDepth < depth) {

                DBCursor currentCircle = storage.getFriendCircle(rootUserId,currentDepth);
                currentDepth++;
                LOGGER.info("Processed circle:\t"+currentDepth);
                while(currentCircle.hasNext()){
                    DBObject currentUser = currentCircle.next();
                    JSONArray friends = vkapiProvider.getFriendsIdList((String) currentUser.get(MongoDbUserInterface.USER_ID));
                    requestCounter++;
                    for (int i = 0; i < friends.length(); i++) {
                        try {
                            storage.userCollection.insert(BasicDBObjectBuilder.start()
                                    .add(MongoDbUserInterface.USER_ID, friends.get(i).toString())
                                    .add(MongoDbUserInterface.FRIEND_CIRCLE, currentDepth)
                                    .add(MongoDbUserInterface.ROOT_USER, rootUserId)
                                    .get()
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    LOGGER.info("RN:\t"+requestCounter);

                }




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
