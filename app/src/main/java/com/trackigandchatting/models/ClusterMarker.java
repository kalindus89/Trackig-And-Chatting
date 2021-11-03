package com.trackigandchatting.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

    // clusters use to customized markers.(with images and avatars. when map zoom out avatars combine together)
    //but for project we use only single clusters. means not grouping when zoom out
    // https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=en

    // fo
    private  LatLng position;
    private  String title;
    private  String snippet;
    private  int iconPicture;
    private String user;
    private String uId;

    public ClusterMarker() {
    }

    public ClusterMarker(LatLng position, String title, String snippet, int iconPicture, String uId) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
        this.uId = uId;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
