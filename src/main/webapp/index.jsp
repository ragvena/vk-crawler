<%@ page import="com.test.network.VKNetworkManager" %>
<%@ page import="com.test.network.VKAPIProvider" %>
<%@ page import="com.test.crawler.Runner" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    VKNetworkManager manager = VKNetworkManager.getInstance();
    Runner crawler = Runner.getInstance(manager.getNetwork(request, response));
    crawler.saveFriends("11364269",2);
//    SocialRate rate = manager.getSocialRate(request, response);
%>
<html>
<body>
<h2>Hello</h2>
<%--<ul>--%>
    <%--<li>Friends: <%=rate.getFriends()%></li>--%>
    <%--<li>Posts: <%=rate.getPosts()%></li>--%>
    <%--<li>Reposts: <%=rate.getReposts()%></li>--%>
<%--</ul>--%>
</body>
</html>
