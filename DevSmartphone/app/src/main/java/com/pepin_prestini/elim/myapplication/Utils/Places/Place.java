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
    public Place(String n, String a){
        nom = n;
        adresse = a;
    }
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "nom")
    public String nom;

    @ColumnInfo(name = "adresse")
    public String adresse;


}
