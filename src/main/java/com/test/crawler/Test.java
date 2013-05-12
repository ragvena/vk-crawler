package com.test.crawler;

import com.mongodb.DBCursor;
import com.test.utils.MongoDBStorage;

/**
 * User: ragvena
 * Date: 5/11/13
 * Time: 10:22 PM
 */
public class Test {
    public static void main(String[] args) {
        final MongoDBStorage storage = MongoDBStorage.getInstance();
        DBCursor cursor = storage.validateUserCollection.find();
    }
}
