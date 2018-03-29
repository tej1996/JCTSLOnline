package com.jctsl.jctslonline.ConductorDept;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jctsl.jctslonline.R;
import com.jctsl.jctslonline.SplashScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConductorDashboard extends AppCompatActivity implements ConductorFunctions{

    private static final int PERMISSIONS_REQUEST = 1;
    private IntentIntegrator qrScan;
    String busname,busnumber,cusername,wallet;
    public static SharedPreferences prefBusCondLive,prefLoginCond;
    public static SharedPreferences.Editor editorBusCondLive,editorLoginCond;
    Button btnlogout,btnbuyticket,btn_stationarrived;
    MaterialSpinner spinner_livebus,spinner_destination;
    TextView txtView_route,txtView_wallet;
    EditText nooftickets;
    String whichway,selected_station;
    public static String start,end;
    public static ArrayList<String> originalstations;
    public static ArrayList<String> stations;
    public static ArrayList<StationListLocations> liststation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_dashboard);
        final String path = getString(R.string.firebase_path);

        qrScan = new IntentIntegrator(this);
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        qrScan.setPrompt("Place QRCode in front of camera to scan the ticket!");
        qrScan.setOrientationLocked(false);


        spinner_livebus= (MaterialSpinner) findViewById(R.id.spinner_livebusstation);
        spinner_destination= (MaterialSpinner) findViewById(R.id.spinner_destination);
        btnlogout = (Button) findViewById(R.id.btn_logoutconductor);
        btn_stationarrived = (Button) findViewById(R.id.btn_arrived);
        btnbuyticket = (Button) findViewById(R.id.btn_buyticket);
        txtView_route=(TextView) findViewById(R.id.textView_route);
        txtView_wallet=(TextView) findViewById(R.id.textView_wallet);
        nooftickets = (EditText) findViewById(R.id.nooftickets);

        prefBusCondLive = getSharedPreferences("BusConductorLive",0);
        prefLoginCond = getSharedPreferences("LoginCondPref",0);
        editorBusCondLive=prefBusCondLive.edit();
        editorLoginCond=prefLoginCond.edit();

        cusername=prefLoginCond.getString("CONDUCTOR_USERNAME",null);
        wallet=prefLoginCond.getString("WALLET",null);
        busname=prefBusCondLive.getString("BUS_NAME",null);
        busnumber=prefBusCondLive.getString("BUS_NUMBER",null);

        txtView_wallet.setText("Your wallet amount is: "+ wallet);
        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            //finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
/*
        FetchStationsLocation fetchStationsLocation= new FetchStationsLocation(ConductorDashboard.this);
        fetchStationsLocation.execute(busname);


        Intent i= new Intent(this, CurrentStationService.class);
        i.putExtra("LIST_STATIONS",(Serializable) liststation);
        i.putExtra("busname",busname);
        i.putExtra("busnumber",busnumber);
        //bindService(i,serviceConnection, BIND_AUTO_CREATE);
        startService(i);
*/

        BusRouteFetch fetchspinnerdata= new BusRouteFetch(ConductorDashboard.this);
        fetchspinnerdata.delegate=this;
        fetchspinnerdata.execute(busname);

        //STATION ARRIVE CODE
        btn_stationarrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_station = stations.get(spinner_livebus.getSelectedIndex());
                if(selected_station.equals(end) && stations.size()==1){
                    whichway="reverse";
                    stations.clear();
                    stations.addAll(originalstations);
                    Collections.reverse(stations);
                    for(int i=0;i<stations.size();i++){
                        Toast.makeText(ConductorDashboard.this,stations.get(i),Toast.LENGTH_SHORT).show();
                    }

                }else if(selected_station.equals(start) && stations.size()==1){
                    whichway="forward";
                    stations.clear();
                    stations.addAll(originalstations);
                }else{
                    stations.remove(selected_station);
                }
                new LiveBusDetailAdd(ConductorDashboard.this).execute(cusername,busname,busnumber,selected_station,whichway);
                editorBusCondLive.putString("CURR_STATION",selected_station);
                editorBusCondLive.putString("WHICH_WAY",whichway);
                editorBusCondLive.commit();
                spinner_livebus.setItems(stations);
                spinner_destination.setItems(stations);
            }
        });

        //BUY TICKET CODE
        btnbuyticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String deststation = stations.get(spinner_destination.getSelectedIndex());
                String qty = nooftickets.getText().toString();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dateobj = new Date();
                String datetime = df.format(dateobj);
                BookTicketConductor bookticket = new BookTicketConductor(ConductorDashboard.this);
                bookticket.delegate=ConductorDashboard.this;
                bookticket.execute(cusername,busname,busnumber,qty,deststation,datetime);

            }
        });

        //LOGOUT CODE
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutConductor logoutConductor= new LogoutConductor(ConductorDashboard.this);
                logoutConductor.delegate2=ConductorDashboard.this;
                logoutConductor.execute(cusername);
                //will redirect to logout() after successful response
            }
        });



    }

    private void startTrackerService() {
        startService(new Intent(this, BusTrackerService.class).putExtra("BUS_NAME",busname).putExtra("BUS_NUMBER",busnumber));
        //finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startTrackerService();
        } else {
            //finish();
        }
    }

    @Override
    public void fillSpinnerData(final ArrayList<String> stations, final String start, final String end) {
        whichway="forward";
        if(prefBusCondLive.getString("WHICH_WAY",null)==null){
            //TEXTVIEW ROUTE UPDATE
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("You are traveling from "+start+" to "+end)
                    .setCancelText("Opposite direction")
                    .setConfirmText("Same direction")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            whichway="reverse";
                            Collections.reverse(stations);
                            txtView_route.setText("You are on route from "+end+" to "+start);
                            //SPINNER
                            spinner_livebus.setItems(stations);
                            spinner_destination.setItems(stations);
                            sDialog.cancel();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            txtView_route.setText("You are on route from "+start+" to "+end);
                            //SPINNER
                            spinner_livebus.setItems(stations);
                            spinner_destination.setItems(stations);
                            sweetAlertDialog.cancel();
                        }
                    })
                    .show();
            editorBusCondLive.putString("WHICH_WAY",whichway);
            editorBusCondLive.commit();
        }else{
            spinner_livebus.setItems(stations);
            spinner_destination.setItems(stations);
        }

       // spinner_livebus.setItems("Ice Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow");
        spinner_livebus.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                selected_station = item;
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void logout(){
        String path = getString(R.string.firebase_path);
        String lkey = busname +" , "+ busnumber;
        editorBusCondLive.clear();
        editorBusCondLive.apply();
        editorLoginCond.clear();
        editorLoginCond.apply();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.child(lkey).removeValue();
        stopService(new Intent(ConductorDashboard.this, BusTrackerService.class));
       // stopService(new Intent(ConductorDashboard.this, CurrentStationService.class));
        startActivity(new Intent(ConductorDashboard.this,SplashScreen.class));
        finish();

    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conductor_dashboard,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.menu_scanqr:

                qrScan.initiateScan();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    String str = result.getContents();
                    List<String> liststr = Arrays.asList(str.split(","));
                    String status=liststr.get(liststr.size()-1);
                    Toast.makeText(ConductorDashboard.this,liststr.get(liststr.size()-1)+"",Toast.LENGTH_LONG).show();
                    if(status.equals("active")){
                        new SweetAlertDialog(ConductorDashboard.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Ticket Verification Success")
                                .setContentText("Your ticket is verified!")
                                .show();

                    }else{
                        new SweetAlertDialog(ConductorDashboard.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Ticket Verification Failure")
                                .setContentText("Your ticket is not valid, penalty will be applied accordingly!")
                                .show();

                    }
                    //converting the data to json
                    //JSONObject obj = new JSONObject(result.getContents());
                    //setting values to textviews
                    //textViewName.setText(obj.getString("name"));
                    //textViewAddress.setText(obj.getString("address"));
                } catch (Exception e) {
                    e.printStackTrace();
                    new SweetAlertDialog(ConductorDashboard.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Network Error")
                            .setContentText("Please Try Again!")
                            .show();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

class BusRouteFetch extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public ConductorFunctions delegate = null;

    public BusRouteFetch(Context context) {
        this.context=context;
    }

    SweetAlertDialog pDialog;
    @Override
    protected void onPreExecute() {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("busname",params[0])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/busroutefetch/")
                .post(body)
                .build();
        Response response = null;
        try {
            response= client.newCall(request).execute();
            String jsonData= response.body().string();

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
        pDialog.dismissWithAnimation();
        super.onPostExecute(res);
        ConductorDashboard.stations = new ArrayList<String>();
        ConductorDashboard.originalstations = new ArrayList<String>();
        try {
            for(int i=0;i<res.length();i++){
                if(i==0){
                    ConductorDashboard.start=res.getString(Integer.toString(i+1));
                }else if(i==res.length()-1){
                    ConductorDashboard.end=res.getString(Integer.toString(i+1));
                }
                ConductorDashboard.stations.add(res.getString(Integer.toString(i+1)));
            }

            ConductorDashboard.originalstations.addAll(ConductorDashboard.stations);
            String currstation =ConductorDashboard.prefBusCondLive.getString("CURR_STATION",null);
            if(currstation!=null){
                if(ConductorDashboard.prefBusCondLive.getString("WHICH_WAY",null).equals("reverse")){
                    Collections.reverse(ConductorDashboard.stations);
                    while(!ConductorDashboard.stations.get(0).equals(currstation))
                    {
                        ConductorDashboard.stations.remove(0);
                    }
                }else{
                    while(!ConductorDashboard.stations.get(0).equals(currstation))
                    {
                        ConductorDashboard.stations.remove(0);
                    }
                }
                ConductorDashboard.stations.remove(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }

        delegate.fillSpinnerData(ConductorDashboard.stations,ConductorDashboard.start,ConductorDashboard.end);

    }
}

class LiveBusDetailAdd extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public ConductorFunctions delegate = null;
    public LiveBusDetailAdd(Context context) {
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("cusername",params[0])
                .add("busname",params[1])
                .add("busnumber",params[2])
                .add("currstation",params[3])
                .add("whichway",params[4])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/livebusdetailadd/")
                .post(body)
                .build();
        Response response = null;
        try {
            response= client.newCall(request).execute();
            String jsonData= response.body().string();
            Log.e("asdsada",jsonData);
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
        super.onPostExecute(res);

        try {
            String status=res.getString("status");
            if(status.equals("updatedlivebusdetails")){
                Toast.makeText(context,"okay done",Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }
        //delegate.fillSpinnerData(stations);

    }
}

class BookTicketConductor extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public ConductorFunctions delegate = null;

    public BookTicketConductor(Context context) {
        this.context=context;
    }

    SweetAlertDialog pDialog;
    @Override
    protected void onPreExecute() {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
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
                .add("qty",params[3])
                .add("deststation",params[4])
                .add("datetime",params[5])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/bookticketconductor/")
                .post(body)
                .build();
        Response response = null;
        try {
            response= client.newCall(request).execute();
            String jsonData= response.body().string();
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
        pDialog.dismissWithAnimation();
        super.onPostExecute(res);
        try {
            String result = res.getString("status");
            if (result.equals("ticketbooked")){
                Toast.makeText(context,"Ticket Booked",Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }


    }
}


class LogoutConductor extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public ConductorFunctions delegate2 = null;

    public LogoutConductor(Context context) {
        this.context=context;
    }

    SweetAlertDialog pDialog;
    @Override
    protected void onPreExecute() {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Logging out...");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("cusername",params[0])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/logoutcond/")
                .post(body)
                .build();
        Response response = null;
        try {
            response= client.newCall(request).execute();
            String jsonData= response.body().string();
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
        pDialog.dismissWithAnimation();
        super.onPostExecute(res);
        try {
            String result = res.getString("status");
            if (result.equals("success")){
                Toast.makeText(context,"Logged Out",Toast.LENGTH_SHORT).show();
                delegate2.logout();
            }else{
                Toast.makeText(context,result,Toast.LENGTH_SHORT).show();

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }


    }
}

class FetchStationsLocation extends AsyncTask<String,String,JSONArray>
{
    private Context context;

    public FetchStationsLocation(Context context) {
        this.context=context;
    }

    SweetAlertDialog pDialog;
    @Override
    protected void onPreExecute() {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Logging out...");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected JSONArray doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("busname",params[0])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/liststationlocations/")
                .post(body)
                .build();
        Response response = null;
        try {
            response= client.newCall(request).execute();
            String jsonData= response.body().string();
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
        pDialog.dismissWithAnimation();
        super.onPostExecute(res);
        try {
            if(res.length()!=0){
                ConductorDashboard.liststation = new ArrayList<>();
                for(int i=0;i<res.length();i++){
                    StationListLocations data = new StationListLocations(res.getJSONObject(i).getString("busname"),
                            res.getJSONObject(i).getDouble("latitude"),
                            res.getJSONObject(i).getDouble("longitude"));
                    ConductorDashboard.liststation.add(data);
                }
            }
            Toast.makeText(context,"Done",Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error33")
                    .setContentText("Please Try Again!")
                    .show();
        }


    }
}

interface ConductorFunctions{

    void fillSpinnerData(ArrayList<String> stations,String start,String end);
    void logout();

}


