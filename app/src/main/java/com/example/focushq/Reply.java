package com.example.focushq;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Reply")
public class Reply extends ParseObject {
    public static final String COMMENT_KEY = "comment";
    public static final String POST_KEY = "post";
    public static final String USER_KEY = "userRepliers";


    public String getReply(){
        return getString(COMMENT_KEY);
    }

    public Post getPost(){
        return (Post) getParseObject(POST_KEY);
    }

    public ParseUser getUser(){
        return getParseUser(USER_KEY);
    }
}
