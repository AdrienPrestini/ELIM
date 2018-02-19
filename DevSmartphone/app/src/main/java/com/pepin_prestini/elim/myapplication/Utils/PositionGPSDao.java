package com.pepin_prestini.elim.myapplication.Utils;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Adrien on 11/02/2018.
 */
@Dao
public interface PositionGPSDao {
    @Query("SELECT * FROM positionsGPS")
    List<PositionGPS> getAll();

    @Insert
    void insertAll(PositionGPS... positionGPS);


    @Update
    void updateUsers(PositionGPS... positionGPS);

    @Delete
    void delete(PositionGPS positionGPS);
}
