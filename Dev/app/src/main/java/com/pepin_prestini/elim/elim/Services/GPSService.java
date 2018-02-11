package com.pepin_prestini.elim.elim.Services;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/*import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;*/
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.pepin_prestini.elim.elim.Utils.AppDatabase;
import com.pepin_prestini.elim.elim.Utils.PositionGPS;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Adrien on 16/12/2017.
 */

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOGSERVICE = "#######";
    public static final String SERVICE_TO_ACTIVITY = "action";
    public static final String ACTIVITY_TO_SERVICE = "action2";
    public static final String STOP = "action2";

    public static final String LAT = "latitude";
    public static final String LONG = "longitude";
    public static final String ALT = "altitude";
    private IntentFilter intentFilter;
    AppDatabase db = Room.databaseBuilder(this,
            AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ciao();
        }
    };

    private void ciao() {
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
            Intent intent = new Intent();
            intent.setAction(SERVICE_TO_ACTIVITY);

            intent.putExtra(GPSService.LAT, 0.0);
            intent.putExtra(GPSService.LONG, 0.0);
            intent.putExtra(GPSService.ALT, 0.0);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.i(LOGSERVICE, "onCreate");

        intentFilter = new IntentFilter();
        intentFilter.addAction(GPSService.ACTIVITY_TO_SERVICE);
        registerReceiver(receiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGSERVICE, "onStartCommand");

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOGSERVICE, "onConnected" + bundle);

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
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (l != null) {
            Double alt = l.getAltitude();
            Double lon = l.getLongitude();
            Double lat = l.getLatitude();
            Log.i(LOGSERVICE, "lat " + lat);
            Log.i(LOGSERVICE, "lng " + lon);
            Log.i(LOGSERVICE, "alt " + alt);

        }

        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOGSERVICE, "onConnectionSuspended " + i);

    }

    @Override
    public void onLocationChanged(Location location) {
        Double alt = location.getAltitude();
        Double lon = location.getLongitude();
        Double lat = location.getLatitude();
        Log.i(LOGSERVICE, "lat " + lat);
        Log.i(LOGSERVICE, "lng " + lon);
        Log.i(LOGSERVICE, "alt " + alt);
        LatLng mLocation = (new LatLng(lat, lon));


        sendData(lon, lat, alt);

        Intent intent = new Intent();
        intent.setAction(SERVICE_TO_ACTIVITY);

        intent.putExtra(GPSService.LAT, lat);
        intent.putExtra(GPSService.LONG, lon);
        intent.putExtra(GPSService.ALT, alt);
        sendBroadcast(intent);
    }

    private void sendData(Double lon, Double lat, Double alt) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()){
            //Vérifier si le cache est vide
            //S'il est vide envoyer directement la position actuelle
            //Sinon envoyer toutes les anciennes positions

            System.out.println("lat : "+ lat +"\nlng : "+ lon +"\nalt : " + alt);
            List<PositionGPS> listPositions = db.positionGPSDao().getAll();
            for (PositionGPS pos: listPositions) {
                System.out.println(pos.toString());
                db.positionGPSDao().delete(pos);
            }
        }else{

            Toast.makeText(getApplicationContext(),"Connection FAIL",Toast.LENGTH_LONG).show();
            PositionGPS positionGPS =  new PositionGPS();
            positionGPS.setAltitude(alt);
            positionGPS.setLatitude(lat);
            positionGPS.setLongitude(lon);
            db.positionGPSDao().insertAll(positionGPS);
        }
        System.out.println("lat : "+ lat +"\nlng : "+ lon +"\nalt : " + alt);
        /*String url = "http://172.20.10.4:3000";
        JSONObject rootJSON = new JSONObject();
        try {
            rootJSON.put("longitude", lon);
            rootJSON.put("latitude", lat);
            rootJSON.put("altitude", alt);
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, rootJSON, null, null);
            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOGSERVICE, "onDestroy - on détruit tout");
        mGoogleApiClient.disconnect();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOGSERVICE, "onConnectionFailed ");

    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        initLocationRequest();
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }


}

/*class SendMessage extends AsyncTask<String, Void, Void> {
    private Exception exception;
    private Socket socket;
    private PrintWriter outToServer;

    @Override
    protected Void doInBackground(String... strings) {
        try{
            try{
                socket = new Socket("172.20.10.4",3000);
                outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                outToServer.print("toto");
                outToServer.flush();
                outToServer.close();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }catch (Exception e){
            this.exception = e;
            return null;
        }
        return null;
    }
}*/