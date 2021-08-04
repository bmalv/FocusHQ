package com.example.focushq;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("User")
public class User extends ParseObject {

    public static final String USERNAME_KEY = "username";
    public static final String PROFILE_IMAGE_KEY = "profileImage";
 //   public static final String FOLLOWING_LIST_KEY = "following";
    public static ParseUser parseUser;

    public String getUsername(){
        return getUsername();
    }

    public ParseUser getUser(String name){
        parseUser = getParseUser(name);
        return parseUser;
    }

    public ParseFile getProfileImage(){
        return getParseFile(PROFILE_IMAGE_KEY);
    }

//    public List<ParseUser> getFollowingList(){
//        return getList(FOLLOWING_LIST_KEY);
//    }
//
//    public void setFollowingListKey(List<ParseUser> following){
//        put(FOLLOWING_LIST_KEY,following);
//    }
}
