package com.test.vk.api.provider;

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

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: ragvena
 * Date: 6/01/13
 * Time: 4:22 PM
 */
public class SpeedTest {
    private static final Logger LOGGER = Logger.getLogger(SpeedTest.class);

    public static void speedTest(final VKAPIProvider vkapiProvider) throws JSONException {
        if (vkapiProvider != null) {
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            DBCursor cursor = storage.rawUserCollection.find().limit(1000);
            StringBuilder userList = new StringBuilder("");
            Integer currentUserCounter = 0;
            final AtomicLong totalAddedUserCounter = new AtomicLong(0);
            final AtomicLong totalProcessedUserCounter = new AtomicLong(0);
            final Integer maxCounter = 100;
            ExecutorService executorService = new BlockingThreadPoolExecutor(5,
                    10, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
            final CountDownLatch latch = new CountDownLatch(1000);
            Date currentDate = new Date();
            LOGGER.info(currentDate.getTime());
            while (cursor.hasNext()) {
                DBObject currentUserInfo = cursor.next();
                if (userList.length() > 0) {
                    userList.append(",");
                }
                userList.append((String) currentUserInfo.get(MongoDBDataTags.UID));
                final String usersListString = userList.toString();
                userList.setLength(0);
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

                                if (User.isEmpty(user, VKDataTags.DEACTIVATED)) {
                                    if (!User.isEmpty(user, VKDataTags.B_DATE) &&
                                            !User.isEmpty(user, VKDataTags.SEX) && user.getInt(VKDataTags.SEX) != 0 &&
                                            !User.isEmpty(user, VKDataTags.CITY) && user.getInt(VKDataTags.CITY) == 106) {
                                        storage.validateUserCollection.insert(BasicDBObjectBuilder.start()
                                                .add(MongoDBDataTags.UID, user.optString(VKDataTags.UID))
                                                .add(VKDataTags.FIRST_NAME, user.optString(VKDataTags.FIRST_NAME))
                                                .add(VKDataTags.SURNAME, user.optString(VKDataTags.SURNAME))
                                                .add(VKDataTags.B_DATE, user.optString(VKDataTags.B_DATE))
                                                .add(VKDataTags.SEX, user.optString(VKDataTags.SEX))
                                                .get());
                                        totalAddedUserCounter.incrementAndGet();
                                        if (totalAddedUserCounter.get() % 10 == 0) {
                                            LOGGER.info("UserDetailedInfo:TotalAdded:\t" + totalAddedUserCounter.get());
                                            LOGGER.info("UserDetailedInfo:TotalProcessed:\t" + totalProcessedUserCounter.get());
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                        latch.countDown();
                    }
                });

                if (latch.getCount() % 250 == 0) {
                    currentDate = new Date();
                    LOGGER.info(currentDate.getTime() + "\t" + latch.getCount());
                }

                totalProcessedUserCounter.incrementAndGet();
            }
            currentDate = new Date();
            LOGGER.info(currentDate.getTime() + "\t" + latch.getCount());
            LOGGER.info("UserDetailedInfo:TotalAdded:\t" + totalAddedUserCounter.get());
            LOGGER.info("UserDetailedInfo:TotalProcessed:\t" + totalProcessedUserCounter.get());
            try {

                latch.await(5, TimeUnit.HOURS);
            } catch (Exception e) {
            }


        }
    }

    public static void speedTestLiner(final VKAPIProvider vkapiProvider) throws JSONException {
        if (vkapiProvider != null) {
            final MongoDBStorage storage = MongoDBStorage.getInstance();
            DBCursor cursor = storage.rawUserCollection.find().limit(1000);
            StringBuilder userList = new StringBuilder("");
            Integer currentUserCounter = 0;
            final AtomicLong totalAddedUserCounter = new AtomicLong(0);
            final AtomicLong totalProcessedUserCounter = new AtomicLong(0);
            final Integer maxCounter = 100;
            Date currentDate = new Date();
            LOGGER.info(currentDate.getTime());
            while (cursor.hasNext()) {
                DBObject currentUserInfo = cursor.next();
                if (userList.length() > 0) {
                    userList.append(",");
                }
                userList.append((String) currentUserInfo.get(MongoDBDataTags.UID));
                final String usersListString = userList.toString();
                userList.setLength(0);

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

                        if (User.isEmpty(user, VKDataTags.DEACTIVATED)) {
                            if (!User.isEmpty(user, VKDataTags.B_DATE) &&
                                    !User.isEmpty(user, VKDataTags.SEX) && user.getInt(VKDataTags.SEX) != 0 &&
                                    !User.isEmpty(user, VKDataTags.CITY) && user.getInt(VKDataTags.CITY) == 106) {
                                storage.validateUserCollection.insert(BasicDBObjectBuilder.start()
                                        .add(MongoDBDataTags.UID, user.optString(VKDataTags.UID))
                                        .add(VKDataTags.FIRST_NAME, user.optString(VKDataTags.FIRST_NAME))
                                        .add(VKDataTags.SURNAME, user.optString(VKDataTags.SURNAME))
                                        .add(VKDataTags.B_DATE, user.optString(VKDataTags.B_DATE))
                                        .add(VKDataTags.SEX, user.optString(VKDataTags.SEX))
                                        .get());
                                totalAddedUserCounter.incrementAndGet();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                if (totalProcessedUserCounter.incrementAndGet() % 10 == 0) {
                    LOGGER.info(totalProcessedUserCounter.get());
                }
            }
        }
        Date currentDate = new Date();
        LOGGER.info(currentDate.getTime());
    }


}
