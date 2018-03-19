package com.jctsl.jctslonline.ConductorDept;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.processbutton.iml.ActionProcessButton;
import com.jctsl.jctslonline.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

public class ConductorLogin extends AppCompatActivity {
    private ActionProcessButton loginButton;
    private EditText usernameText,passwordText;
    private CheckBox showpass;
    public static SharedPreferences pref,pref1;
    public static SharedPreferences.Editor editor,editor1;
    public static SweetAlertDialog pDialog;
    String username,password;
    MaterialDialog dialog;
    String busname,busnumber;
    public static String wallet;
    View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_login);
        getSupportActionBar().hide();

        loginButton = (ActionProcessButton) findViewById(R.id.btnSignIn);
        usernameText = (EditText) findViewById(R.id.login_username);
        passwordText = (EditText) findViewById(R.id.login_password);
        showpass = (CheckBox) findViewById(R.id.chkbox_showpass_cond);
        showpass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passwordText.setInputType(TYPE_CLASS_TEXT);
                }else{
                    passwordText.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        pref = getSharedPreferences("LoginCondPref",0);
        pref1 = getSharedPreferences("BusConductorLive",0);
        editor = pref.edit();
        editor1 = pref1.edit();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username=usernameText.getText().toString();
                password=passwordText.getText().toString();
                if (username.isEmpty() || username.length() < 3) {
                    usernameText.setError("At least 3 characters");
                }else if (password.isEmpty() || password.length() < 3) {
                    passwordText.setError("At least 3 characters");
                }else{
                    pDialog = new SweetAlertDialog(ConductorLogin.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Authenticating....");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    CheckConductorLogin conductorlogin= new CheckConductorLogin(ConductorLogin.this);
                    conductorlogin.execute(username,password);
                }


            }
        });



    }

    void adddetails(){

        boolean wrapInScrollView = true;
        dialog  =   new MaterialDialog.Builder(ConductorLogin.this)
                .customView(R.layout.conductordetails, wrapInScrollView)
                .positiveText("Submit")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        //   View view = dialog.getCustomView();
                        view = dialog.getCustomView();
                        EditText busnameText = (EditText) view.findViewById(R.id.bus_name);
                        EditText busnumberText = (EditText) view.findViewById(R.id.bus_number);


                        busname = busnameText.getText().toString().toUpperCase().trim();
                        busnumber = busnumberText.getText().toString();
                        if(busname.length()<=0 || busnumber.length()<=0){
                            Toast.makeText(ConductorLogin.this,"Enter correct details!",Toast.LENGTH_SHORT).show();
                            adddetails();
                        }else{
                            AddConductorDetails adddetails= new AddConductorDetails(ConductorLogin.this);
                            adddetails.execute(username,busname,busnumber);
                        }
                    }
                }).build();
        dialog.show();

    }

    private void processFinish(String result) {
        if(result.equals("success"))
        {
            adddetails();
        }else if(result.equals("You have entered wrong password"))
        {
            new SweetAlertDialog(ConductorLogin.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("Wrong Password!")
                    .show();
        }
        else if(result.equals("You are not registered"))
        {
            new SweetAlertDialog(ConductorLogin.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("You are not registered!")
                    .show();
        }else if(result.equals("conductordetailsadded"))
        {
            new SweetAlertDialog(ConductorLogin.this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success!")
                    .setContentText("Details Added Successfully!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Intent i = new Intent(getApplicationContext(),ConductorDashboard.class);
                            i.putExtra("CONDUCTOR_USERNAME",username);
                            i.putExtra("BUS_NAME",busname);
                            i.putExtra("BUS_NUMBER",busnumber);
                            i.putExtra("WALLET", ConductorLogin.wallet);

                            editor.putString("CONDUCTOR_USERNAME",username);
                            editor.putString("WALLET",ConductorLogin.wallet);
                            editor.putBoolean("IS_COND_LOGGED_IN",true);
                            editor.commit();

                            editor1.putString("BUS_NAME",busname);
                            editor1.putString("BUS_NUMBER",busnumber);
                            editor1.commit();

                            startActivity(i);
                            finish();
                        }
                    })
                    .show();
        }else if(result.equals("busnotfound"))
        {
            new SweetAlertDialog(ConductorLogin.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("Specified bus is not found!")
                    .show();
        }
    }

    //CONDUCTOR_LOGIN_CLASS
    class CheckConductorLogin extends AsyncTask<String,String,JSONObject>
    {
        private Context context;
        public CheckConductorLogin(Context context) {
            this.context=context;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("username",params[0])
                    .add("password",params[1])
                    .build();

            Request request = new Request.Builder()
                    .url("http://tejasv.pythonanywhere.com/checkloginconductor/")
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
            super.onPostExecute(res);
            String result = "";
            try {
                result = res.getString("status");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Network Error")
                        .setContentText("Please Try Again!")
                        .show();
            }
            processFinish(result);

            ConductorLogin.pDialog.dismiss();
        }


    }

    //ADD_CONDUCTOR_DETAILS

    class AddConductorDetails extends AsyncTask<String,String,JSONObject>
    {
        private Context context;
        public AddConductorDetails(Context context) {
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
                    .add("username",params[0])
                    .add("busname",params[1])
                    .add("busnumber",params[2])
                    .build();

            Request request = new Request.Builder()
                    .url("http://tejasv.pythonanywhere.com/addconductordetails/")
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
            String result = "";

            try {
                result = res.getString("status");
                wallet = res.getString("wallet");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Network Error")
                        .setContentText("Please Try Again!")
                        .show();
            }
            processFinish(result);
        }
    }
}

