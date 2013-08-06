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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: ragvena
 * Date: 6/23/13
 * Time: 4:23 PM
 */
public class Group {
    private static final Logger LOGGER = Logger.getLogger(SpeedTest.class);
    public static final Comparator COMPARATOR = new Comparator<Map.Entry<String, Long>>() {
        @Override
        public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    };

    public static void findPopularGroups(final VKAPIProvider vkapiProvider) throws JSONException {
        if (vkapiProvider != null) {
            final AtomicInteger tCounter = new AtomicInteger(0);
            ExecutorService executorService = new BlockingThreadPoolExecutor(2,
                    1, 1000L, TimeUnit.MILLISECONDS, 1000L, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            });
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
                                LOGGER.info("findPopularGroups\t" + tCounter.get());
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

    public static void getPagesDetailedInfo(final VKAPIProvider vkapiProvider) throws JSONException {
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
                if (!currentPage.containsField(VKDataTags.NAME)) {
                    if (pagesList.length() > 0) {
                        pagesList.append(",");
                    }
                    pagesList.append((String) currentPage.get(VKDataTags.GID));
                    currentPageCounter++;
                    if (currentPageCounter.equals(maxCounter)) {
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
                }
                if (totalProcessedPageCounter.incrementAndGet() % 1000 == 0) {
                    LOGGER.info("getPagesDetailedInfo\t" + totalProcessedPageCounter.get());
                }

            }
        }
    }


}
