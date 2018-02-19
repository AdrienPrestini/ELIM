package com.pepin_prestini.elim.myapplication.Services;

import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.pepin_prestini.elim.myapplication.Utils.AppDatabase;
import com.pepin_prestini.elim.myapplication.Utils.HelperLocationStrategies;
import com.pepin_prestini.elim.myapplication.Utils.PositionGPS;

import java.util.List;
import java.util.Objects;

/**
 * Created by Adrien on 16/02/2018.
 */

public class GPSService extends Service {
    private static final String TAG = "ELIM-GPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_STRING = "myResponse";
    public static final String RESPONSE_MESSAGE = "myResponseMessage";

    public static final String LATITUDE_STRING = "latitude";
    public static final String LONGITUDE_STRING = "longitude";
    public static final String ALTITUDE_STRING = "altitude";
    AppDatabase db = Room.databaseBuilder(this,
            AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();


    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER)};

    class LocationListener implements android.location.LocationListener{

        public Location mLastLocation;
        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            if(HelperLocationStrategies.isBetterLocation(location, mLastLocation))
                mLastLocation.set(location);
            System.out.println(mLastLocation.getLatitude() + "/" +mLastLocation.getLongitude() + "/" +mLastLocation.getAltitude()  );
            /*Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(SearchService.REQUEST_STRING);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(LATITUDE_STRING, mLastLocation.getLatitude());
            broadcastIntent.putExtra(LONGITUDE_STRING, mLastLocation.getLongitude());
            broadcastIntent.putExtra(ALTITUDE_STRING, mLastLocation.getAltitude());
            sendBroadcast(broadcastIntent);*/
            Intent broadcastIntent = new Intent();

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    //Vérifier si le cache est vide
                    //S'il est vide envoyer directement la position actuelle
                    //Sinon envoyer toutes les anciennes positions

                    List<PositionGPS> listPositions = db.positionGPSDao().getAll();
                    if(listPositions.size() > 0)
                        Log.e(TAG, "On rémet la postition GPS de la base de données");
                    for (PositionGPS pos : listPositions) {
                        //System.out.println(pos.toString());
                        Log.e(TAG, "-->"+pos.toString());
                        broadcastIntent.setAction(SearchService.REQUEST_STRING);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(LATITUDE_STRING, pos.getLatitude());
                        broadcastIntent.putExtra(LONGITUDE_STRING, pos.getLongitude());
                        broadcastIntent.putExtra(ALTITUDE_STRING, pos.getAltitude());
                        sendBroadcast(broadcastIntent);
                        db.positionGPSDao().delete(pos);
                    }
                    Log.e(TAG, "position actuelle : "+mLastLocation.toString());
                    broadcastIntent.setAction(SearchService.REQUEST_STRING);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra(LATITUDE_STRING, mLastLocation.getLatitude());
                    broadcastIntent.putExtra(LONGITUDE_STRING, mLastLocation.getLongitude());
                    broadcastIntent.putExtra(ALTITUDE_STRING, mLastLocation.getAltitude());
                    sendBroadcast(broadcastIntent);
                } else {
                    Log.e(TAG, "On met la postition GPS dans la base de données");
                    //Toast.makeText(getApplicationContext(), "Connection FAIL", Toast.LENGTH_LONG).show();
                    PositionGPS positionGPS = new PositionGPS();
                    positionGPS.setAltitude(mLastLocation.getAltitude());
                    positionGPS.setLatitude(mLastLocation.getLatitude());
                    positionGPS.setLongitude(mLastLocation.getLongitude());
                    db.positionGPSDao().insertAll(positionGPS);
                    List<PositionGPS> listPositions = db.positionGPSDao().getAll();
                    Log.e(TAG, "Taille = " + listPositions.size());
                }
            }


        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
            if(provider.equals( "network")){
                Log.e(TAG, "On met la postition GPS dans la base de données");
                Toast.makeText(getApplicationContext(), "Connection FAIL", Toast.LENGTH_LONG).show();
                PositionGPS positionGPS = new PositionGPS();
                positionGPS.setAltitude(mLastLocation.getAltitude());
                positionGPS.setLatitude(mLastLocation.getLatitude());
                positionGPS.setLongitude(mLastLocation.getLongitude());
                db.positionGPSDao().insertAll(positionGPS);
            }
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider +" status : " + status);
            //System.out.println(mLastLocation.getLatitude() + "/" +mLastLocation.getLongitude() + "/" +mLastLocation.getAltitude()  );
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }
    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }
    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
