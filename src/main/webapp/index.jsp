<%@ page import="com.test.servlet.SocialRate" %>
<%@ page import="com.test.servlet.SocialWebManager" %>
<%@ page import="com.test.servlet.vk.VKNetworkManager" %>
<%@ page import="com.test.servlet.vk.VK" %>
<%@ page import="com.test.servlet.vk.VKAPIProvider" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

//    SocialWebManager<Twitter> manager = TwitterManager.getInstance();
    SocialWebManager<VKAPIProvider> manager = VKNetworkManager.getInstance();
    SocialRate rate = manager.getSocialRate(request, response);
%>
<html>
<body>
<h2>Hello <%=rate.getUserName()%>!</h2>
<ul>
    <li>Friends: <%=rate.getFriends()%></li>
    <li>Posts: <%=rate.getPosts()%></li>
    <li>Reposts: <%=rate.getReposts()%></li>
</ul>
</body>
</html>
