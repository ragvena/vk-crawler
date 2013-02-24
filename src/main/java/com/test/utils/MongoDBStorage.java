package com.test.utils;


import com.mongodb.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

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
    public DB database;
    public DBCollection collection;

    public void connect() throws UnknownHostException {
        Mongo mongo = new Mongo("localhost", 27017);
        database.setWriteConcern(WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database = mongo.getDB(MONGO_STORAGE);
        collection = database.getCollection(USER_COLLECTION);
    }

    public void insertRecord(BasicDBObject record){
        database.requestStart();
        collection.insert(record, WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database.requestDone();
    }

    public void insertRecordList(BasicDBList list){
        database.requestStart();
        collection.insert(list,WriteConcern.NORMAL.continueOnErrorForInsert(true));
        database.requestDone();
    }

    public void disconnect(){

    }
    public void test(){
        System.out.print("yep");
        LOGGER.info("yep");
    }




}
