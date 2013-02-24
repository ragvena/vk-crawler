package com.test.servlet;

/**
 * andry.krp
 */
public class SocialRate {
    private String userName;
    private Integer friend;
    private Integer post;
    private Integer repost;

    public SocialRate() {}

    public SocialRate(String userName, Integer friend, Integer post, Integer repost) {
        this.userName = userName;
        this.friend = friend;
        this.post = post;
        this.repost = repost;
    }

    public Integer getFriends() {
        return friend;
    }

    public void setFriend(Integer friend) {
        this.friend = friend;
    }

    public Integer getPosts() {
        return post;
    }

    public void setPost(Integer post) {
        this.post = post;
    }

    public Integer getReposts() {
        return repost;
    }

    public void setRepost(Integer repost) {
        this.repost = repost;
    }

    public static SocialRate blank(){
        return new SocialRate("", 0, 0, 0);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
