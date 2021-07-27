package com.example.focushq;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseObject {

    public static final String USERNAME_KEY = "username";
    public static final String PROFILE_IMAGE_KEY = "profileImage";
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
}
