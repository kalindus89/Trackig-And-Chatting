package com.trackigandchatting.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class UserLocation {


    private GeoPoint geo_point;
    private String name;
    private String image;
    private String uid;
    private Date lastUpdateTime;


    public UserLocation() {

    }

    public UserLocation(GeoPoint geo_point, String name, String image, String uid, Date lastUpdateTime) {
        this.geo_point = geo_point;
        this.name = name;
        this.image = image;
        this.uid = uid;
        this.lastUpdateTime = lastUpdateTime;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
