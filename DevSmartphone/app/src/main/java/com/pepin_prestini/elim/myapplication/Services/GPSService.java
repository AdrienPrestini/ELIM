package com.pepin_prestini.elim.myapplication.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.pepin_prestini.elim.myapplication.MainActivity;
import com.pepin_prestini.elim.myapplication.Utils.HelperLocationStrategies;

/**
 * Created by Adrien on 16/02/2018.
 */

public class GPSService extends Service {
    private static final String TAG = "ELIM-GPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    public static final String LATITUDE_STRING = "latitude";
    public static final String LONGITUDE_STRING = "longitude";
    public static final String ALTITUDE_STRING = "altitude";
    public static final String RESPONSE_STRING = "myResponse";


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

            /*StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.137.98:3000/routine", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println(response);

                    //broadcastResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Erreur",error.toString());
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new Hashtable<>();
                    map.put("imei", "123456789" );
                    map.put("latitude", mLastLocation.getLatitude()+"" );
                    map.put("longitude", mLastLocation.getLongitude() +"");
                    return map;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(stringRequest);*/
            broadcastResponse("");

            /*Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(SearchService.REQUEST_STRING);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(LATITUDE_STRING, mLastLocation.getLatitude());
            broadcastIntent.putExtra(LONGITUDE_STRING, mLastLocation.getLongitude());
            broadcastIntent.putExtra(ALTITUDE_STRING, mLastLocation.getAltitude());
            sendBroadcast(broadcastIntent);*/
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
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
        }
    }

    private void broadcastResponse(String s) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.RoutineServiceReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(GPSService.RESPONSE_STRING, s);
        sendBroadcast(broadcastIntent);
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
