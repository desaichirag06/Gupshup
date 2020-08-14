package com.chirag.gupshup.findFriends;

public class FindFriendModel {
    private String userName, photoName, userId;
    private boolean requestSent;

    public FindFriendModel(String userName, String photoName, String userId, boolean requestSent) {
        this.userName = userName;
        this.photoName = photoName;
        this.userId = userId;
        this.requestSent = requestSent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

    @Override
    public String toString() {
        return "FindFriendModel{" +
                "userName='" + userName + '\'' +
                ", photoName='" + photoName + '\'' +
                ", userId='" + userId + '\'' +
                ", requestSent=" + requestSent +
                '}';
    }
}
