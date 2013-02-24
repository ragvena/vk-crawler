package com.test.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * andry.krp
 */
public interface SocialWebManager<T> {
    public T getNetwork(HttpServletRequest request, HttpServletResponse response);
    public void logout(HttpServletRequest request, HttpServletResponse response);
    public SocialRate getSocialRate(HttpServletRequest request, HttpServletResponse response);
}
