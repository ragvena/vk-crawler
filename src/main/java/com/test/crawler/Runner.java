package com.test.crawler;

import com.test.utils.MongoDBStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.UnknownHostException;

/**
 * User: ragvena
 * Date: 2/24/13
 * Time: 2:18 PM
 */
public class Runner {



    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "context.xml");
       FriendsCrawler friendsCrawler =  ctx.getBean(FriendsCrawler.class);
        friendsCrawler.test();
//        mongoDBStorage.test();
//        new FriendsCrawler().test();
//        new Runner().test();

    }
    public void test() throws UnknownHostException {

    }
}
