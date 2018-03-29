package com.jctsl.jctslonline;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jctsl.jctslonline.BuilderManager.getImageResource;
import static com.jctsl.jctslonline.BuilderManager.getStringResource;

public class Dashboard extends AppCompatActivity implements OnMapReadyCallback {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String TAG = Dashboard.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;
    public double userlatitude;
    public double userlongitude;
    public static UserLocation gps;
    Button btncheckbus;
    String email;
    EditText editText_destination;
    String destination,qty;
    ListView listNearbybus;
    public static SweetAlertDialog pDialog;
    String busname,busnumber,cusername,srcstation,deststation,totalfare,crowd;
    MaterialDialog dialog;
    View view;
    CheckBox tourist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        btncheckbus = (Button) findViewById(R.id.btnCheckBuses);
        editText_destination = (EditText) findViewById(R.id.editText_destination);
        tourist = (CheckBox) findViewById(R.id.chkBox_tourist);

        pref = getApplicationContext().getSharedPreferences("LoginPref", 0);
        editor = pref.edit();
        email = pref.getString("USER_EMAIL",null);
        ActionBar mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View actionBar = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) actionBar.findViewById(R.id.title_text);
        mTitleTextView.setText("Dashboard");
        mActionBar.setCustomView(actionBar);
        mActionBar.setDisplayShowCustomEnabled(true);
        ((Toolbar) actionBar.getParent()).setContentInsetsAbsolute(0, 0);

        BoomMenuButton leftBmb = (BoomMenuButton) actionBar.findViewById(R.id.action_bar_left_bmb);


        leftBmb.setButtonEnum(ButtonEnum.TextOutsideCircle);
        leftBmb.setPiecePlaceEnum(PiecePlaceEnum.DOT_9_1);
        leftBmb.setButtonPlaceEnum(ButtonPlaceEnum.SC_9_1);

        for (int i = 0; i < leftBmb.getPiecePlaceEnum().pieceNumber(); i++) {
            TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
                    .normalImageRes(getImageResource())
                    .normalText(getStringResource())
                    .pieceColor(Color.WHITE).textSize(13)
                    .typeface(Typeface.MONOSPACE)
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            // When the boom-button corresponding this builder is clicked.
                           // Toast.makeText(getApplicationContext(), "Clicked " + index, Toast.LENGTH_SHORT).show();
                            if (index == 5) {
                                editor.clear();
                                editor.apply();
                                Intent i = new Intent(Dashboard.this, SplashScreen.class);
                                startActivity(i);
                                finish();

                            } else if (index == 1) {
                                Intent i = new Intent(Dashboard.this, TicketHistory.class);
                                startActivity(i);

                            } else if (index == 0) {
                                Intent i = new Intent(Dashboard.this,UserProfile.class);
                                startActivity(i);

                            } else if (1 == 2) {

                            } else if (index == 8) {
                                finish();
                            }


                        }
                    });
            leftBmb.addBuilder(builder);
        }

        //MENU END,MAP START

        gps = new UserLocation(this);
        if(gps.canGetLocation()){

            userlatitude = gps.getLatitude();
            userlongitude = gps.getLongitude();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(Dashboard.this);

        }else{
            gps.showSettingsAlert();
        }

        btncheckbus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destination = editText_destination.getText().toString();
                String lat= Double.toString(userlatitude);
                String lon= Double.toString(userlongitude);
                qty="2";
                CheckForBus chkbus = new CheckForBus(Dashboard.this);
                if(tourist.isChecked()){

                    chkbus.execute(lat,lon,qty,destination,"t");
                }else {
                    chkbus.execute(lat, lon, qty, destination,"nt");
                }
            }
        });

        PlaceAutocompleteFragment places= (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                SearchByLocation srchbyloc = new SearchByLocation(Dashboard.this);
                LatLng loc=place.getLatLng();
                srchbyloc.execute(Double.toString(loc.latitude),Double.toString(loc.longitude));
                Toast.makeText(getApplicationContext(),place.getLatLng().toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(),status.toString(),Toast.LENGTH_SHORT).show();

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);
        //own location mark
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userlatitude, userlongitude), 16));

        //loginToFirebase();

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        //  mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //  mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        subscribeToUpdates();
    }

    private void subscribeToUpdates() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarker(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void setMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(lat, lng);
        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon_map))));
        } else {
            mMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }

    private void removeMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        mMarkers.get(key).remove();

    }

    private void askforconfirmation(NearbyBusData res) {
        pDialog.dismissWithAnimation();
        try {
            busname=res.getBusname();
            busnumber=res.getBusnumber();
            cusername=res.getCusername();
            srcstation=res.getSrcstationname();
            deststation=res.getDeststationname();
            totalfare=res.getTotalfare();
            crowd=res.getCrowd();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateobj = new Date();
            final String datetime = df.format(dateobj);
            Log.e("fafafaf",datetime);
            String heading = "From "+srcstation+" to "+deststation;
            String display = "Bus Name: "+busname+"\nBus Number: "+busnumber+"\nTotal fare: "+totalfare+"\nSpace:"+crowd;
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(heading)
                    .setContentText(display)
                    .setConfirmText("Yes,book my ticket!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {

                            sDialog.dismissWithAnimation();
                            BookTicketUser bookticket = new BookTicketUser(Dashboard.this);
                            bookticket.execute(cusername,busname,busnumber,srcstation,deststation,qty,totalfare,datetime,email);


                        }
                    })
                    .show();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setList(final List<NearbyBusData> listnearby){

        boolean wrapInScrollView = true;
        dialog  =   new MaterialDialog.Builder(Dashboard.this)
                .customView(R.layout.listnearbybuses, wrapInScrollView)
                .positiveText("Okay")
                .build();
        view = dialog.getCustomView();
        listNearbybus =(ListView) view.findViewById(R.id.list_nearbybuses);

        NearbyBusAdapter adapter = new NearbyBusAdapter(Dashboard.this,R.layout.nearbybus_list_item,listnearby);
        listNearbybus.setAdapter(adapter);
        listNearbybus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                askforconfirmation(listnearby.get(position));
                //Log.d("sda",position+"");
            }
        });
        dialog.show();
    }

    class CheckForBus extends AsyncTask<String,String,JSONArray>
    {
        private Context context;
        public CheckForBus(Context context) {
            this.context=context;
        }

        @Override
        protected void onPreExecute() {
            pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Searching for required bus...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();

        }

        @Override
        protected JSONArray doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("lat",params[0])
                    .add("lon",params[1])
                    .add("qty",params[2])
                    .add("destination",params[3])
                    .add("tourist",params[4])
                    .build();

            Request request = new Request.Builder()
                    .url("http://tejasv.pythonanywhere.com/checkforbus/")
                    .post(body)
                    .build();
            Response response = null;
            try {
                response= client.newCall(request).execute();
                String jsonData= response.body().string();
                Log.e("jSonDatatatata",jsonData);
                try {
                    JSONArray json = new JSONArray(jsonData);
                    return json;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray res) {
            super.onPostExecute(res);
            pDialog.dismissWithAnimation();

            try {
                if(res.length()!=0){
                    List<NearbyBusData> listnearby = new ArrayList<>();
                    for(int i=0;i<res.length();i++){
                        NearbyBusData data = new NearbyBusData(res.getJSONObject(i).getString("busname"),
                                res.getJSONObject(i).getString("busnumber"),
                                res.getJSONObject(i).getString("cusername"),
                                res.getJSONObject(i).getString("srcstationname"),
                                res.getJSONObject(i).getString("deststationname"),
                                res.getJSONObject(i).getString("totalfare"),
                                res.getJSONObject(i).getString("crowd"),
                                res.getJSONObject(i).getString("arrtime"));
                        listnearby.add(data);
                    }
                    setList(listnearby);
                }else{
                    new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("No Bus Found")
                            .setContentText("There is no direct route from your location to specified destination")
                            .show();
                }

            }
            catch (Exception e)
            {
                Log.d("errrrrror",e+"");
                e.printStackTrace();
                new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Network Error")
                        .setContentText("Please Try Again!")
                        .show();
            }

        }
    }


    class BookTicketUser extends AsyncTask<String,String,JSONObject>
    {
        private Context context;

        public BookTicketUser(Context context) {
            this.context=context;
        }

        SweetAlertDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Booking your ticket...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("cusername",params[0])
                    .add("busname",params[1])
                    .add("busnumber",params[2])
                    .add("srcstation",params[3])
                    .add("deststation",params[4])
                    .add("qty",params[5])
                    .add("totalfare",params[6])
                    .add("datetime",params[7])
                    .add("email",params[8])
                    .build();

            Request request = new Request.Builder()
                    .url("http://tejasv.pythonanywhere.com/bookticketuser/")
                    .post(body)
                    .build();
            Response response = null;
            try {
                response= client.newCall(request).execute();
                String jsonData= response.body().string();
                Log.e("jSonDatatatatadasda33",jsonData);

                try {
                    JSONObject json = new JSONObject(jsonData);
                    return json;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            dialog.hide();
            super.onPostExecute(res);
            try {
                String result = res.getString("status");
                if (result.equals("ticketbooked")){
                    //Toast.makeText(context,"Ticket Booked",Toast.LENGTH_SHORT).show();
                    pDialog
                            .setTitleText("Booked!")
                            .setContentText("Your ticket is booked!\nCheck your ticket from Ticket History option!")
                            .setConfirmText("OK")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    /*mMap.clear();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path)).child(busname+" , "+busnumber);

                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String key = dataSnapshot.getKey();
                                            HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
                                            double lat = Double.parseDouble(value.get("latitude").toString());
                                            double lng = Double.parseDouble(value.get("longitude").toString());
                                            LatLng location = new LatLng(lat, lng);
                                            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon_map))));

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    */
                                    pDialog.dismissWithAnimation();
                                }
                            })
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                }

            }
            catch (Exception e)
            {
                pDialog.dismissWithAnimation();
                e.printStackTrace();
                new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Network Error")
                        .setContentText("Please Try Again!")
                        .show();
            }


        }
    }

    public class NearbyBusAdapter extends ArrayAdapter<NearbyBusData> {

        //the list values in the List of type hero
        List<NearbyBusData> nearbyBusList;

        //activity context
        Context context;

        //the layout resource file for the list items
        int resource;

        //constructor initializing the values
        public NearbyBusAdapter(Context context, int resource, List<NearbyBusData> nearbyBusList) {
            super(context, resource, nearbyBusList);
            this.context = context;
            this.resource = resource;
            this.nearbyBusList = nearbyBusList;
        }

        //this will return the ListView Item as a View
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            //we need to get the view of the xml for our list item
            //And for this we need a layoutinflater
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            //getting the view
            View view = layoutInflater.inflate(resource, null, false);

            //getting the view elements of the list from the view
            TextView nearbybussrc = (TextView) view.findViewById(R.id.textView_nearbybussrc);
            TextView nearbybusdest = (TextView) view.findViewById(R.id.textView_nearbybusdest);
            TextView nearbybusname = (TextView) view.findViewById(R.id.textView_nearbybusname);
            TextView nearbybusnumber = (TextView) view.findViewById(R.id.textView_nearbybusnumber);
            TextView nearbybustotalfare = (TextView) view.findViewById(R.id.textView_nearbybustotalfare);
            TextView nearbybuscrowd = (TextView) view.findViewById(R.id.textView_nearbybuscrowd);
            TextView arrtime = (TextView) view.findViewById(R.id.textView_arrtime);

            //getting the Nearbybusdata of the specified position
            NearbyBusData nearbybuslist = nearbyBusList.get(position);

            nearbybussrc.setText(nearbybuslist.getSrcstationname());
            nearbybusdest.setText(nearbybuslist.getDeststationname());
            nearbybusname.setText(nearbybuslist.getBusname());
            nearbybusnumber.setText(nearbybuslist.getBusnumber());
            nearbybustotalfare.setText(nearbybuslist.getTotalfare());
            nearbybuscrowd.setText(nearbybuslist.getCrowd());
            arrtime.setText(nearbybuslist.getArrtime());

            //finally returning the view
            return view;
        }
    }

    class SearchByLocation extends AsyncTask<String,String,JSONObject>
    {
        private Context context;

        public SearchByLocation(Context context) {
            this.context=context;
        }

        SweetAlertDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Searching nearby bus stop...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("latitude",params[0])
                    .add("longitude",params[1])
                    .build();

            Request request = new Request.Builder()
                    .url("http://tejasv.pythonanywhere.com/searchbylocation/")
                    .post(body)
                    .build();
            Response response = null;
            try {
                response= client.newCall(request).execute();
                String jsonData= response.body().string();
                //Log.e("jSonDatatatatadasda33",jsonData);

                try {
                    JSONObject json = new JSONObject(jsonData);
                    return json;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            pDialog.hide();
            super.onPostExecute(res);
            try {
                String result = res.getString("stationname");
                if (!result.isEmpty()) {
                    editText_destination.setText(result);
                    //Toast.makeText(context,result,Toast.LENGTH_SHORT).show();
                }

            }
            catch (Exception e)
            {
                pDialog.dismissWithAnimation();
                e.printStackTrace();
                new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Network Error")
                        .setContentText("Please Try Again!")
                        .show();
            }


        }
    }


}