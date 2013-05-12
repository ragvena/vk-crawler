package com.test.crawler;

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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 8:31 PM
 */
public class Runner {
    public static final Comparator COMPARATOR = new Comparator<Map.Entry<String, Long>>() {
        @Override
        public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    };
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

    public static void saveFriends(final String rootUserId, final Integer depth) {
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

    public static void getPreferencies(String userId) {

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

    public static VKAPIProvider getVkapiProvider() {
        return vkapiProvider;
    }

    private void setVkapiProvider(VKAPIProvider vkapiProvider) {
        this.vkapiProvider = vkapiProvider;
    }

    public static void getUsersDetailedInfo() throws JSONException {
        if (vkapiProvider != null) {
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            DBCursor cursor = storage.rawUserCollection.find();
            StringBuilder userList = new StringBuilder("");
            Integer currentUserCounter = 0;
            final AtomicLong totalAddedUserCounter = new AtomicLong(0);
            final AtomicLong totalProcessedUserCounter = new AtomicLong(0);
            final Integer maxCounter = 100;
            ExecutorService executorService = new BlockingThreadPoolExecutor(3,
                    1, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
            while (cursor.hasNext()) {
                DBObject currentUserInfo = cursor.next();
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
                                                LOGGER.info("TotalAdded:\t" + totalAddedUserCounter.get());
                                                LOGGER.info("TotalProcessed:\t" + totalProcessedUserCounter.get());
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                            }
                        }
                    });

                }
                totalProcessedUserCounter.incrementAndGet();
            }
        }
    }

    public static void findPopularGroups() throws JSONException {
        if (vkapiProvider != null) {
            final AtomicInteger tCounter = new AtomicInteger(0);
            ExecutorService executorService = new BlockingThreadPoolExecutor(2,
                    1, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
            final Random sleepTime = new Random();
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            DBCursor cursor = storage.validateUserCollection.find();
            DBCursor data = storage.pagesCollection.find();
            final ConcurrentHashMap<String, Long> counter = new ConcurrentHashMap<String, Long>();
            while (data.hasNext()) {
                DBObject count = data.next();
                counter.put((String) count.get(VKDataTags.GID), (Long) count.get(VKDataTags.CNT));
            }
            while (cursor.hasNext()) {
                final DBObject user = cursor.next();
                if (!user.containsField(VKDataTags.GROUPS)) {
                    final String uid = (String) user.get(MongoDBDataTags.UID);
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject responce = null;
                            try {
                                responce = vkapiProvider.getSubscribePages(uid);

                                if (responce.optJSONObject(VKDataTags.GROUPS) != null) {
                                    JSONArray groupList = responce
                                            .optJSONObject(VKDataTags.GROUPS)
                                            .optJSONArray(VKDataTags.ITEMS);
                                    user.put(VKDataTags.GROUPS, groupList.toString());
                                    storage.validateUserCollection.update(new BasicDBObject(MongoDBDataTags.UID, uid), user);
                                    if (groupList != null) {
                                        for (int i = 0; i < groupList.length(); i++) {
                                            Long c = 1L;
                                            if (counter.containsKey(groupList.get(i) + "")) {
                                                c += counter.get(groupList.get(i) + "");
                                            }
                                            counter.put(groupList.get(i) + "", c);
                                        }

                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            if (tCounter.incrementAndGet() % 100 == 0) {
                                LOGGER.info(tCounter.get());
                                List<Map.Entry<String, Long>> ordered = new ArrayList<Map.Entry<String, Long>>(counter.entrySet());
                                Collections.sort(ordered, COMPARATOR);
                                for (int i = 0; i < 10; i++) {
                                    LOGGER.info(ordered.get(i));
                                }
                                for (Map.Entry<String, Long> page : ordered) {
                                    storage.pagesCollection.update(new BasicDBObject(VKDataTags.GID, page.getKey()),
                                            BasicDBObjectBuilder.start()
                                                    .add(VKDataTags.GID, page.getKey())
                                                    .add(VKDataTags.CNT, page.getValue())
                                                    .get(),
                                            true, false);
                                }

                            }
                        }
                    });
                }
            }
        }
    }

    public static void getPagesDetailedInfo() throws JSONException {
        if (vkapiProvider != null) {
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            DBCursor cursor = storage.pagesCollection.find(new BasicDBObject(VKDataTags.CNT, new BasicDBObject("$gt", 100)));
            StringBuilder pagesList = new StringBuilder("");
            Integer currentPageCounter = 0;
            final AtomicLong totalProcessedPageCounter = new AtomicLong(0);
            final Integer maxCounter = 20;
            ExecutorService executorService = new BlockingThreadPoolExecutor(2,
                    1, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
            while (cursor.hasNext()) {
                DBObject currentPage = cursor.next();
                if (pagesList.length() > 0) {
                    pagesList.append(",");
                }
                pagesList.append((String) currentPage.get(VKDataTags.GID));
                currentPageCounter++;
                if (currentPageCounter == maxCounter) {
                    final String pagesListString = pagesList.toString();
                    pagesList.setLength(0);
                    currentPageCounter = 0;
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONArray response = null;
                                response = vkapiProvider.getSubscribePagesDetailedInfo(pagesListString);
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject page = response.getJSONObject(i);
                                    DBObject key = new BasicDBObject(VKDataTags.GID, page.getString(VKDataTags.GID));
                                    DBObject pageData = storage.pagesCollection.findOne(key);
                                    pageData.put(VKDataTags.DESCRIPTION, page.getString(VKDataTags.DESCRIPTION));
                                    pageData.put(VKDataTags.NAME, page.getString(VKDataTags.NAME));
                                    pageData.put(VKDataTags.SCREEN_NAME, page.getString(VKDataTags.SCREEN_NAME));
                                    storage.pagesCollection.update(key, pageData);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    });

                }
                totalProcessedPageCounter.incrementAndGet();
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
