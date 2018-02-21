package com.pepin_prestini.elim.myapplication.Utils;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Time;
import java.util.Date;

/**
 * Created by Adrien on 11/02/2018.
 */
@Entity(tableName = "positionsGPS")
public class PositionGPS {

    public PositionGPS(){}
    public PositionGPS(Double lat, Double lon, String i, String m, Date d){
        latitude = lat;
        longitude = lon;
        mot = m;
        dateSearch = d.toString();
        timeSearch = d.getTime();
        imei = i;
    }
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "mot")
    public String mot;

    @ColumnInfo(name = "date")
    public String dateSearch;

    @ColumnInfo(name = "imei")
    public String imei;

    @ColumnInfo(name = "time")
    public long timeSearch;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public String getMot() {
        return mot;
    }

    public void setMot(String mot) {
        this.mot = mot;
    }

    public String getDateSearch() {
        return dateSearch;
    }

    public void setDateSearch(String dateSearch) {
        this.dateSearch = dateSearch;
    }

    public String getTimeSearch() {
        return timeSearch + "";
    }

    public void setTimeSearch(long timeSearch) {
        this.timeSearch = timeSearch;
    }

    @Override
    public String toString() {
        return "PositionGPS{" +
                "uid=" + uid +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", mot='" + mot + '\'' +
                ", dateSearch='" + dateSearch + '\'' +
                ", timeSearch=" + timeSearch +
                '}';
    }
}
