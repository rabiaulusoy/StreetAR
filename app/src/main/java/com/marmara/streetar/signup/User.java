package com.marmara.streetar.signup;

/**
 * Created by EDA on 26.04.2018.
 */

public class User {

    private String userName;
    private String uId;
    private String favoritePlaces;

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public User(){

    }

    public User(String userName, String uId,String favoritePlaces) {
        this.userName = userName;
        this.uId = uId;
        this.favoritePlaces=favoritePlaces;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFavoritePlaces() {
        return favoritePlaces;
    }

    public void setFavoritePlaces(String favoritePlaces) {
        this.favoritePlaces = favoritePlaces;
    }
}

