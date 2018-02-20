package com.pepin_prestini.elim.myapplication.Utils.Places;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Adrien on 11/02/2018.
 */
@Entity(tableName = "places")
public class Place {

    public Place(){}
    public Place(String n, Double la, Double lo,String path, String adresse){
        nom = n;
        imagePath = path;
        lat = la;
        lng = lo;
        this.adresse = adresse;
    }
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "nom")
    public String nom;

    @ColumnInfo(name = "adresse")
    public String adresse;

    @ColumnInfo(name = "lat")
    public Double lat;

    @ColumnInfo(name = "lng")
    public Double lng;

    @ColumnInfo(name = "imagePath")
    public String imagePath;

}
