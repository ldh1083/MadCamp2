package com.example.myapplication;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class Maps {
    private String bm;
    private String name;
    private String memo;
    private LatLng latLng;
    private String marker_name;

    public Maps (String marker_name, LatLng latLng) {
        this.marker_name = marker_name;
        this.latLng = latLng;
    }

    public void setBm(String bm) {
        this.bm = bm;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
    public void setMarker_name(String marker_name) {
        this.marker_name = marker_name;
    }

    public String getBm() {
        return this.bm;
    }
    public String getName() {
        return this.name;
    }
    public String getMemo() {
        return this.memo;
    }
    public LatLng getLatLng() {
        return this.latLng;
    }
    public String getMarker_name() {
        return this.marker_name;
    }
}