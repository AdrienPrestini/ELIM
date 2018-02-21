package com.pepin_prestini.elim.myapplication.Utils.Places;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.List;

/**
 * Created by Adrien on 19/02/2018.
 */
@Dao
public interface PlacesDao {

        @Query("SELECT * FROM places")
        List<Place> getAll();

        @Insert
        void insertAll(Place... place);


        @Update
        void updateUsers(Place... place);

        @Delete
        void delete(Place place);
}
