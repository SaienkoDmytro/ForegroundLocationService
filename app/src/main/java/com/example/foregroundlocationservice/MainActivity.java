package com.example.foregroundlocationservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    public final static String PARAM_LATITUDE = "LATI";
    public final static String PARAM_LONGITUDE = "LONGI";
    public final static String BROADCAST_ACTION = "com.example.foregroundlocationservice";

    private BroadcastReceiver broadcastReceiver;
    private RecyclerAdapter mRecyclerAdapter;
    private final ArrayList<DataModel> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPref.init(getApplicationContext());

        String latitude = SharedPref.read(SharedPref.LATITUDE, null);
        String longitude = SharedPref.read(SharedPref.LONGITUDE, null);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_set_coordinates_main_activity);
        mRecyclerAdapter = new RecyclerAdapter(locations);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        DataModel obj1 = new DataModel(latitude, longitude);
        locations.add(obj1);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        if (grantPermission()) {
            if (checkLocationEnableOrNot()) {
                Intent intent = new Intent(this, LocationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            } else new AlertDialog.Builder(this)
                    .setTitle("Provide permissions for working and restart application!")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> grantPermission()).setNegativeButton("CANCEL", null)
                    .show();
        }

        // create BroadcastReceiver
        broadcastReceiver = new BroadcastReceiver() {
            // on receive massages
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0);
                double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0);
                Log.d(LOG_TAG, "onReceive: Lati = " + latitude + ", Longi = " + longitude);
                SharedPref.write(SharedPref.LATITUDE, "" + latitude);
                SharedPref.write(SharedPref.LONGITUDE, "" + longitude);
                String x = "" + latitude;
                String y = "" + longitude;

                DataModel obj = new DataModel(x, y);
                locations.add(obj);
                mRecyclerAdapter.notifyData(locations);
            }
        };
        // create BroadcastReceiver filter
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        // start BroadcastReceiver
        registerReceiver(broadcastReceiver, intentFilter);

        }


private boolean checkLocationEnableOrNot() {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    boolean gpsEnable = false;
    boolean netEnable = false;
    try {
        gpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception e){
        e.printStackTrace();
    }
    try {
        netEnable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception e){
        e.printStackTrace();
    }

    if (!gpsEnable && !netEnable) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alerttitle)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_positive, (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton(R.string.alert_negative, null)
                .show();
        return false;
    } else return true;
}

    private boolean grantPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!checkLocationEnableOrNot()) {
            stopService(new Intent(this, LocationService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if (!checkLocationEnableOrNot()) {
            stopService(new Intent(this, LocationService.class));
        }
    }

}