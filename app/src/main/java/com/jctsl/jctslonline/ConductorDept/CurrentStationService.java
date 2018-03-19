package com.jctsl.jctslonline.ConductorDept;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jctsl.jctslonline.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentStationService extends Service {

    ArrayList<StationListLocations> ar;
    String busname,busnumber;
    String currstation;
    public CurrentStationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        ar = (ArrayList<StationListLocations>) intent.getSerializableExtra("LIST_STATIONS");
        busname = intent.getStringExtra("busname");
        busnumber = intent.getStringExtra("busnumber");
        throw new UnsupportedOperationException("Not yet implemented");

    }
    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public class MyBinder extends Binder {
        CurrentStationService getService() {
            return CurrentStationService.this;
        }
    }
    public double distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String path = getString(R.string.firebase_path);
        String lkey = busname +" , "+ busnumber;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);

        ref.child(lkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double lng=0,lat = 0;
                lat = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                lng = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());

                for(int i=0;i<ar.size();i++){
                    if(distance(lat,lng,ar.get(i).latitude,ar.get(i).longitude)<100000){
                        currstation = ar.get(i).stationame;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),currstation,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
