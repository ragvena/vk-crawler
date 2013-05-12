package com.test.utils;


import com.mongodb.*;
import com.test.data.MongoDBDataTags;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 11:58 AM
 */

public class MongoDBStorage {
    private static final String MONGO_STORAGE = "crawler";
    private static final String RAW_USER_COLLECTION = "usersTest";
    private static final String VALIDATE_USER_COLLECTION = "users";
    private static final String PAGES_COLLECTION = "page";
    private static final Logger LOGGER = Logger.getLogger(MongoDBStorage.class);
    private static MongoDBStorage instance;
    public DB database;
    public DBCollection rawUserCollection;
    public DBCollection validateUserCollection;
    public DBCollection pagesCollection;


    private MongoDBStorage() {
        Mongo mongo = null;
        try {
            mongo = new Mongo("localhost", 27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        database = mongo.getDB(MONGO_STORAGE);
        database.setWriteConcern(WriteConcern.NORMAL.continueOnErrorForInsert(true));
        rawUserCollection = database.getCollection(RAW_USER_COLLECTION);
        validateUserCollection = database.getCollection(VALIDATE_USER_COLLECTION);
        pagesCollection = database.getCollection(PAGES_COLLECTION);
    }

    public static MongoDBStorage getInstance() {
        if (instance == null) {
            instance = new MongoDBStorage();
        }
        return instance;
    }

    public void insertRecord(BasicDBObject record) {
        database.requestStart();
        rawUserCollection.insert(record, WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database.requestDone();
    }

    public void insertRecordList(BasicDBList list) {
        database.requestStart();
        rawUserCollection.insert(list, WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database.requestDone();
    }

    public DBCursor getFriendCircle(String rootUser, Integer circle) {
        database.requestStart();
        DBObject query = BasicDBObjectBuilder.start()
                .add(MongoDBDataTags.ROOT_USER, rootUser)
                .add(MongoDBDataTags.FRIEND_CIRCLE, circle)
                .get();
        DBCursor friends = rawUserCollection.find(query);
        database.requestDone();
        return friends;
    }

    public void disconnect() {

    }

    public void test() {
        System.out.print("yep");
        LOGGER.info("yep");
    }


}
