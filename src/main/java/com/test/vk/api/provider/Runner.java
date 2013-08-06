package com.test.vk.api.provider;

import com.test.network.VKAPIProvider;
import org.apache.log4j.Logger;

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


    public static VKAPIProvider getVkapiProvider() {
        return vkapiProvider;
    }

    private void setVkapiProvider(VKAPIProvider vkapiProvider) {
        this.vkapiProvider = vkapiProvider;
    }


}
