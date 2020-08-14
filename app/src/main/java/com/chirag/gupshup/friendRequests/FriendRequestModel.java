package com.chirag.gupshup.friendRequests;

public class FriendRequestModel {
    private String userId, userName, photoName;

    public FriendRequestModel(String userId, String userName, String photoName) {
        this.userId = userId;
        this.userName = userName;
        this.photoName = photoName;
    }

    @Override
    public String toString() {
        return "FriendRequestModel{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", photoName='" + photoName + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
