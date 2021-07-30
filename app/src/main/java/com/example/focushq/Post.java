package com.example.focushq;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String IMAGE_KEY = "image";
    public static final String USER_KEY = "user";
    public static final String LOCATION_ID_KEY = "locationID";
    public static final String LOCATION_NAME_KEY = "locationName";
    public static final String KEY_DESCRIPTION = "description";
    public static final String CREATED_AT_KEY = "createdAt";
    public static final String PROFILE_IMAGE_KEY = "profileImage";
    public static final String REPLY_KEY = "replies";

    //function retrieves the image under the image key
    public ParseFile getImage(){
        return getParseFile(IMAGE_KEY);
    }

    //function retrieves the user under the user key
    public ParseUser getUser(){
        return getParseUser(USER_KEY);
    }

    //function retrieves the location's name
    public String getLocationName(){
        return getString(LOCATION_NAME_KEY);
    }

    public String getDescription(){
        return getString(KEY_DESCRIPTION);
    }

    public String getLocationId(){
        return getString(LOCATION_ID_KEY);
    }

    public List<String> getReplies(){
        return getList(REPLY_KEY);
    }

    public ParseFile getProfileImage(){
        ParseUser user = getUser();
        return user.getParseFile(PROFILE_IMAGE_KEY);
    }


    //function calculates the time the post was published
    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }

    //function sets the Image
    public void setImage(ParseFile file){
        put(IMAGE_KEY,file);
    }

    //function sets the profile image
    public void setProfileImage(ParseFile file){ put(PROFILE_IMAGE_KEY, file); }

    //function sets the User
    public void setUser(ParseUser user){
        put(USER_KEY, user);
    }

    //function sets the location's name
    public void setLocationName(String locationName){
        put(LOCATION_NAME_KEY,locationName);
    }

    //function sets the description
    public void setDescription(String description){
        put(KEY_DESCRIPTION,description);
    }

    public void setLocationID(String placeID){
        put(LOCATION_ID_KEY,placeID);
    }



}
