package com.pepin_prestini.elim.elim;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.pepin_prestini.elim.elim.Services.GPSService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {


    TextView mLatitudeTextView ;
    TextView mLongitudeTextView ;
    TextView mAltitudeTextView ;
    //private boolean serviceIsStarted = false;
    private IntentFilter intentFilter;
    FloatingActionButton fab;
    private boolean GPSEnable = false;


    public static final int ID_NOTIFICATION = 1988;



    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double lat = intent.getDoubleExtra(GPSService.LAT,0);
            Double lon = intent.getDoubleExtra(GPSService.LONG,0);
            Double alt = intent.getDoubleExtra(GPSService.ALT,0);
            mLatitudeTextView.setText(lat.toString());
            mLongitudeTextView.setText(lon.toString());
            mAltitudeTextView.setText(alt.toString());
        }
    };

        public static final int MY_PERMISSIONS_REQUEST_GPS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_AppBarOverlay);

        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLatitudeTextView =  findViewById((R.id.latitude_textview));
        mLongitudeTextView = findViewById((R.id.longitude_textview));
        mAltitudeTextView = findViewById((R.id.altitude_textview));
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                startGPSPreview();
            }
        });


        intentFilter = new IntentFilter();
        intentFilter.addAction(GPSService.SERVICE_TO_ACTIVITY);
        registerReceiver(receiver, intentFilter);
        refreshViewReopen();
        /*Intent intent = new Intent();
        intent.setAction(GPSService.ACTIVITY_TO_SERVICE);

        intent.putExtra(GPSService.STOP, 0);
        sendBroadcast(intent);*/
    }

    private void refreshViewReopen() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        GPSEnable = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        if(GPSEnable) {
            if (isMyServiceRunning(GPSService.class)) {
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#40900e")));
            }
            else{
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C50D0C")));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.VIBRATE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.INTERNET)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(findViewById(R.id.main_layout), "Le GPS est necessaire pour accèder à la position",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.VIBRATE},
                            MY_PERMISSIONS_REQUEST_GPS);
                }
            }).show();

        } else {
            Snackbar.make(findViewById(R.id.main_layout),
                    "Permission non disponible. Demande de permission pour le GPS",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_GPS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == MY_PERMISSIONS_REQUEST_GPS) {
            // Request for camera permission.
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && ( grantResults[1] == PackageManager.PERMISSION_GRANTED) && ( grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                // Permission has been granted. GPS camera preview Activity.
                /*Snackbar.make(findViewById(R.id.main_layout), "GPS autorisé. Démarrage du GPS.",
                        Snackbar.LENGTH_SHORT)
                        .show();*/
                startGPS();
            } else {
                // Permission request was denied.
                /*Snackbar.make(findViewById(R.id.main_layout), "Le GPS n'est pas autorisé.",
                        Snackbar.LENGTH_SHORT)
                        .show();*/
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    private void startGPS() {
        verifyGPS();
        //création d'une notif


        if(GPSEnable) {
            if (!isMyServiceRunning(GPSService.class)) {
                //sendDataToServerTest();
                createNotification();
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#40900e")));
                startService(new Intent(this, GPSService.class));
                //Toast.makeText(this.getApplicationContext(), "ON COMMENCE LE SERVICE", Toast.LENGTH_LONG).show();
            } else {
                NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notifManager != null) {
                    notifManager.cancelAll();
                }
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C50D0C")));
                //Toast.makeText(this.getApplicationContext(), "ON ARRETE LE SERVICE", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(GPSService.ACTIVITY_TO_SERVICE);

                intent.putExtra(GPSService.STOP, 0);
                sendBroadcast(intent);
                stopService(new Intent(this, GPSService.class));

            }
        }
    }
    private void sendDataToServerTest(){
        Toast.makeText(this.getApplicationContext(), "Envoi du paquet au serveur", Toast.LENGTH_LONG).show();
        //envoyer un paquet de test au serveur


        //Client myClient = new Client("172.20.10.4", 3000);
       // myClient.execute();
        /*Socket socket = new Socket(addr, 3000);
        OutputStream out = socket.getOutputStream();

        out.write("(Test) Bonjour Nicolas Pepin".getBytes());
        out.flush();
        out.close();

        socket.close();*/

    }
    private void createNotification() {



        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon_plan)
                        .setContentTitle("ELIM activé")
                        .setContentText("L'application récupère vos données GPS")
                .setOngoing(true);

        Intent notificationIntent = new Intent(this, this.getClass());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //builder.setVibrate(new long[] {0,200,100,200,100,200});
        if (manager != null) {
            manager.notify(0, builder.build());
        }
    }

    private void startGPSPreview() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            /*Snackbar.make(findViewById(R.id.main_layout),
                    "La permission de localisation GPS  is available. Starting preview.",
                    Snackbar.LENGTH_SHORT).show();*/
            startGPS();
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
        verifyGPS();
    }

    @Override
    protected void onStop() {
        super.onStop();


    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    private void verifyGPS(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (manager != null) {
            GPSEnable = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        }
        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) || !manager.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {

            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}

/*class Client extends AsyncTask<Void, Void, String> {

    private String dstAddress;
    private int dstPort;
    private String response = "";
    //TextView textResponse;

    Client(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
        //this.textResponse = textResponse;
    }

    @Override
    protected String doInBackground(Void... arg0) {

        Socket socket = null;

        try {
            socket = new Socket(dstAddress, dstPort);
            //OutputStream out = socket.getOutputStream();
            PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            outToServer.print("Hello Nico");
            outToServer.flush();
            socket.close();
            /*out.write("some data".getBytes());
            out.flush();
            out.close();

            /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();


              //notice: inputStream.read() will block if no data return

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        //textResponse.setText(response);
        super.onPostExecute(result);
    }

}*/