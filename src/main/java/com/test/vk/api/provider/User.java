package com.test.vk.api.provider;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.test.data.MongoDBDataTags;
import com.test.data.VKDataTags;
import com.test.network.VKAPIProvider;
import com.test.utils.BlockingThreadPoolExecutor;
import com.test.utils.MongoDBStorage;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: ragvena
 * Date: 5/01/13
 * Time: 4:17 PM
 */
public class User {
    private static final Logger LOGGER = Logger.getLogger(User.class);

    public static void saveFriends(final VKAPIProvider vkapiProvider, final String rootUserId, final Integer depth) {
        if (vkapiProvider != null) {
            final AtomicInteger currentDepth = new AtomicInteger(2);
            final AtomicLong requestCounter = new AtomicLong(0);
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            storage.rawUserCollection.insert(BasicDBObjectBuilder.start()
                    .add(MongoDBDataTags.UID, rootUserId)
                    .add(MongoDBDataTags.FRIEND_CIRCLE, 0)
                    .add(MongoDBDataTags.ROOT_USER, rootUserId)
                    .get()
            );
            ExecutorService executorService = new BlockingThreadPoolExecutor(1,
                    1, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
            final ConcurrentSkipListSet<String> users = new ConcurrentSkipListSet<String>();
            while (currentDepth.get() < depth) {
                final DBCursor currentCircle = storage.getFriendCircle(rootUserId, currentDepth.get());
                currentDepth.incrementAndGet();
                final CountDownLatch latch = new CountDownLatch(currentCircle.count());
                LOGGER.info("Processed circle:\t" + currentDepth.get());
                while (currentCircle.hasNext()) {

                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {

                            DBObject currentUser = currentCircle.next();
                            JSONArray friends = null;
                            try {
                                friends = vkapiProvider.getFriendsIdList((String) currentUser.get(MongoDBDataTags.UID));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            requestCounter.incrementAndGet();
                            for (int i = 0; i < friends.length(); i++) {
                                try {
                                    users.add(friends.get(i).toString());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (requestCounter.get() % 100 == 0) {
                                LOGGER.info("RN:\t" + requestCounter);
                            }
                            if (requestCounter.get() % 1000 == 0) {
                                storeUserList(users, currentDepth, rootUserId, storage);
                                LOGGER.info("Intermediate store\t" + requestCounter.get() / 1000);

                            }
                            latch.countDown();
                        }

                    });
                }
                try {
                    latch.await(5, TimeUnit.HOURS);
                } catch (Exception e) {
                    storeUserList(users, currentDepth, rootUserId, storage);
                }
                storeUserList(users, currentDepth, rootUserId, storage);

            }
        } else {
            LOGGER.info("nop");
        }
    }


    public static void storeUserList(ConcurrentSkipListSet<String> users, AtomicInteger currentDepth, String rootUserId, MongoDBStorage storage) {
        for (String user : users) {
            DBObject exist = storage.rawUserCollection.findOne(
                    new BasicDBObject(MongoDBDataTags.UID, user));
            if (exist == null) {
                storage.rawUserCollection.insert(BasicDBObjectBuilder.start()
                        .add(MongoDBDataTags.UID, user)
                        .add(MongoDBDataTags.FRIEND_CIRCLE, currentDepth.get())
                        .add(MongoDBDataTags.ROOT_USER, rootUserId)
                        .get()
                );
            }
        }
    }

    public static void getUsersDetailedInfo(final VKAPIProvider vkapiProvider) throws JSONException {
        if (vkapiProvider != null) {
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            DBCursor cursor = storage.rawUserCollection.find();
            StringBuilder userList = new StringBuilder("");
            Integer currentUserCounter = 0;
            final AtomicLong totalAddedUserCounter = new AtomicLong(0);
            final AtomicLong totalProcessedUserCounter = new AtomicLong(0);
            final Integer maxCounter = 100;
            ExecutorService executorService = new BlockingThreadPoolExecutor(2,
                    1, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
            while (cursor.hasNext()) {
                DBObject currentUserInfo = cursor.next();
                DBObject existing = storage.validateUserCollection.findOne(new BasicDBObject(MongoDBDataTags.UID, (String) currentUserInfo.get(MongoDBDataTags.UID)));
                if (existing == null) {
                    if (userList.length() > 0) {
                        userList.append(",");
                    }
                    userList.append((String) currentUserInfo.get(MongoDBDataTags.UID));
                    currentUserCounter++;
                    if (currentUserCounter == maxCounter) {
                        final String usersListString = userList.toString();
                        userList.setLength(0);
                        currentUserCounter = 0;
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                JSONArray response = null;
                                try {
                                    response = vkapiProvider.getUsersDetailInfo(usersListString, "");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject user = null;
                                    try {
                                        user = response.getJSONObject(i);

                                        if (isEmpty(user, VKDataTags.DEACTIVATED)) {
                                            if (!isEmpty(user, VKDataTags.B_DATE) &&
                                                    !isEmpty(user, VKDataTags.SEX) && user.getInt(VKDataTags.SEX) != 0 &&
                                                    !isEmpty(user, VKDataTags.CITY) && user.getInt(VKDataTags.CITY) == 106) {
                                                storage.validateUserCollection.insert(BasicDBObjectBuilder.start()
                                                        .add(MongoDBDataTags.UID, user.optString(VKDataTags.UID))
                                                        .add(VKDataTags.FIRST_NAME, user.optString(VKDataTags.FIRST_NAME))
                                                        .add(VKDataTags.SURNAME, user.optString(VKDataTags.SURNAME))
                                                        .add(VKDataTags.B_DATE, user.optString(VKDataTags.B_DATE))
                                                        .add(VKDataTags.SEX, user.optString(VKDataTags.SEX))
                                                        .get());
                                                totalAddedUserCounter.incrementAndGet();
                                                if (totalAddedUserCounter.get() % 1000 == 0) {
                                                    LOGGER.info("UserDetailedInfo:TotalAdded:\t" + totalAddedUserCounter.get());
                                                    LOGGER.info("UserDetailedInfo:TotalProcessed:\t" + totalProcessedUserCounter.get());
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

                    }
                }
                if (totalProcessedUserCounter.incrementAndGet() % 1000 == 0) {
                    LOGGER.info(totalProcessedUserCounter.get());
                }
            }
        }
    }

    public static Boolean isEmpty(JSONObject user, String field) {
        if (user.optString(field).equals("")) {
            return true;
        }
        return false;
    }
}
