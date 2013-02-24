package com.test.utils;

import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class Helper {
    public static final Logger log = Logger.getLogger(Helper.class);

    private Helper() {
    }

    private static class SingletonHolder {
        public static Helper instance = new Helper();
    }

    public static Helper getInstance() {
        return SingletonHolder.instance;
    }


    public void setCookie(String name, String value, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(365 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    public String getCookieValue(String name, HttpServletRequest request) {
        Cookie cookie = getCookie(name, request);
        return cookie != null ? cookie.getValue() : null;
    }

    public Cookie getCookie(String name, HttpServletRequest request) {
        if (request.getCookies() != null)
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equalsIgnoreCase(name)) {
                    return cookie;
                }
            }
        return null;
    }

    public void removeCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(name, request);
        if (cookie != null) {
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    public void clearRedirect(HttpServletRequest request, HttpServletResponse response) {
        log.info(request.getSession().getId());
        if (!request.getRequestURI().equals("") && !request.getRequestURI().equals("/"))
            try {
                response.sendRedirect("");
            } catch (IOException e) {
                log.error("", e);
            }
    }
}

