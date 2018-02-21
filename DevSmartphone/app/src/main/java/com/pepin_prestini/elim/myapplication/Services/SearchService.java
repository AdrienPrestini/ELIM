package com.pepin_prestini.elim.myapplication.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.pepin_prestini.elim.myapplication.MainActivity;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pepin_prestini.elim.myapplication.Utils.AppDatabase;
import com.pepin_prestini.elim.myapplication.Utils.PositionGPS;


/**
 * Created by Adrien on 14/02/2018.
 */

public class SearchService extends Service {
    private static final String URL = "172.20.10.2";
    private static final String TAG = "ELIM-SEARCH";
    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_STRING = "myResponse";
    AppDatabase db = Room.databaseBuilder(this,
            AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            sendToServer(intent.getStringExtra("searchWord"));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    private void sendToServer(final String search) {
        // Get LocationManager object
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider;
        if (locationManager != null) {
            provider = locationManager.getBestProvider(criteria, true);
            // Get Current Location


            Location myLocation = locationManager.getLastKnownLocation(provider);
            //latitude of location
            final double myLatitude = myLocation.getLatitude();

            //longitude og location
            final double myLongitude = myLocation.getLongitude();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if(isOnline()){
                sendOldData();
                sendDataToServer(myLatitude,myLongitude, search);
            }else{
                storeDataInLocal(myLatitude,myLongitude, search, "123456789");
            }

        }
    }

    private void sendOldData() {
        List<PositionGPS> positionGPSList = db.positionGPSDao().getAll();
        for (PositionGPS p: positionGPSList) {
            sendOldDataToServer(p);
        }
    }

    private void sendOldDataToServer(final PositionGPS p) {
        System.out.println(p);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://"+URL+":3000/rechercheAncienne", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);

                broadcastResponse(response);
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
                System.out.println(p);
                map.put("imei", "123456789" );
                map.put("latitude", p.getLatitude()+"" );
                map.put("longitude", p.getLongitude() +"");
                map.put("search", p.getMot());
                map.put("date", p.getDateSearch());
                map.put("temps", p.getTimeSearch() + "");
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(stringRequest);
    }

    private void sendDataToServer(final Double  latitude, final Double longitude, final String motSearch) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://"+URL+":3000/recherche", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);

                broadcastResponse(response);
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
                map.put("latitude", latitude+"" );
                map.put("longitude", longitude +"");
                map.put("search", motSearch);
                System.out.println(map);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(stringRequest);
    }

    private void storeDataInLocal(Double lat, Double lng, String mot, String imei) {
        PositionGPS pos = new PositionGPS(lat, lng, mot,imei, new Date());
        this.db.positionGPSDao().insertAll(pos);
        this.db.setTransactionSuccessful();
    }

    private boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
            return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void broadcastResponse(String response) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.SearchServiceReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_STRING, response);
        sendBroadcast(broadcastIntent);
    }
}
