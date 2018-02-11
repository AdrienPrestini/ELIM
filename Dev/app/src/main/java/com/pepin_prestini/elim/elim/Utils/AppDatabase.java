package com.pepin_prestini.elim.elim.Utils;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Adrien on 11/02/2018.
 */
@Database(entities = {PositionGPS.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase{
    public abstract PositionGPSDao positionGPSDao();
}
