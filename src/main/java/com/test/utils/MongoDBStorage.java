package com.test.utils;


import com.mongodb.*;
import com.test.user.MongoDbUserInterface;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 11:58 AM
 */

public class MongoDBStorage {
    private static final String MONGO_STORAGE = "crawler";
    private static final String USER_COLLECTION = "users";
    private static final Logger LOGGER = Logger.getLogger(MongoDBStorage.class);
    private static MongoDBStorage instance;
    public DB database;
    public DBCollection userCollection;


    private MongoDBStorage() {
        Mongo mongo = null;
        try {
            mongo = new Mongo("localhost", 27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        database = mongo.getDB(MONGO_STORAGE);
        database.setWriteConcern(WriteConcern.NORMAL.continueOnErrorForInsert(true));
        userCollection = database.getCollection(USER_COLLECTION);
    }

    public static MongoDBStorage getInstance() {
        if (instance == null) {
            instance = new MongoDBStorage();
        }
        return instance;
    }

    public void insertRecord(BasicDBObject record) {
        database.requestStart();
        userCollection.insert(record, WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database.requestDone();
    }

    public void insertRecordList(BasicDBList list) {
        database.requestStart();
        userCollection.insert(list, WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database.requestDone();
    }

    public DBCursor getFriendCircle(String rootUser, Integer circle) {
        database.requestStart();
        DBObject query = BasicDBObjectBuilder.start()
                .add(MongoDbUserInterface.ROOT_USER, rootUser)
                .add(MongoDbUserInterface.FRIEND_CIRCLE, circle)
                .get();
        DBCursor friends = userCollection.find(query);
        database.requestDone();
        return  friends;
    }

    public void disconnect() {

    }

    public void test() {
        System.out.print("yep");
        LOGGER.info("yep");
    }


}
