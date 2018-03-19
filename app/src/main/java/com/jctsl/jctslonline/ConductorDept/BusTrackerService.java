package com.jctsl.jctslonline.ConductorDept;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jctsl.jctslonline.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


public class BusTrackerService extends Service implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BusTrackerService.class.getSimpleName();
    String busname,busnumber;
    LocationCallback locationCallback;
    public static SharedPreferences pref;
    FusedLocationProviderClient client;
    GoogleApiClient mGoogleApiClient;
    LocationRequest request;
    public BusTrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
      //  android.os.Debug.waitForDebugger();

        client = LocationServices.getFusedLocationProviderClient(this);
       // buildNotification();
        //loginToFirebase();
        if (mGoogleApiClient != null && client != null) {
            requestLocationUpdates();
        } else {
            buildGoogleApiClient();
        }

    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.removeLocationUpdates(locationCallback);

            stopSelf();
        }
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        if (client != null) {
            client.removeLocationUpdates(locationCallback);

            stopSelf();
        }
        return super.stopService(name);
    }

    private void requestLocationUpdates() {
        request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(500);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);
        pref = getSharedPreferences("BusConductorLive", 0);
        busname=pref.getString("BUS_NAME",null);
        busnumber=pref.getString("BUS_NUMBER",null);

        final String path = getString(R.string.firebase_path) + "/" + busname +" , "+ busnumber;
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase

            locationCallback =  new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "location update " + location);
                        ref.setValue(location);
                    }
                }
            };
            client.requestLocationUpdates(request, locationCallback,null);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
