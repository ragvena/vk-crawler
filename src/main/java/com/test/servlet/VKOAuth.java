package com.test.servlet;

import com.test.network.VKNetworkManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class VKOAuth extends HttpServlet {
    public static final Logger log = Logger.getLogger(VKOAuth.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("ip [" + request.getRemoteAddr() + "]");
        VKNetworkManager.getInstance().createNetwork(request, response);
        response.sendRedirect("");
    }
}
