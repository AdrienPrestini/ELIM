package com.pepin_prestini.elim.myapplication.Utils;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.pepin_prestini.elim.myapplication.Utils.Places.Place;
import com.pepin_prestini.elim.myapplication.Utils.Places.PlacesDao;

/**
 * Created by Adrien on 11/02/2018.
 */
@Database(entities = {PositionGPS.class, Place.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase{
    public abstract PositionGPSDao positionGPSDao();
    public abstract PlacesDao placesDao();
}
