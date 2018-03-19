package com.jctsl.jctslonline;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Signup extends AppCompatActivity implements AsyncResponse{
    EditText passengerName;
    EditText passengerEmail;
    EditText passengerPassword;
    EditText passengerNumber;
    ActionProcessButton signupButton;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String name,email,number,password;
    public static SweetAlertDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();

        signupButton = (ActionProcessButton) findViewById(R.id.btn_signup);
        passengerName = (EditText) findViewById(R.id.get_passenger_name);
        passengerEmail = (EditText) findViewById(R.id.get_passenger_email);
        passengerPassword = (EditText) findViewById(R.id.get_passenger_password);
        passengerNumber = (EditText) findViewById(R.id.get_passenger_number);
        pref = getSharedPreferences("LoginPref",0);
        editor = pref.edit();
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                signup();
            }
        });


    }

    public void signup() {

       if (!validate()) {
            return;
        }
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        name = passengerName.getText().toString();
        email = passengerEmail.getText().toString();
        password = passengerPassword.getText().toString();
        number = passengerNumber.getText().toString();


        checkAlready(email,number);

    }

    public boolean validate() {
        boolean valid = true;

        String name = passengerName.getText().toString();
        String email = passengerEmail.getText().toString();
        String password = passengerPassword.getText().toString();
        String number = passengerNumber.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            passengerName.setError("At least 3 characters");
            valid = false;
        } else {
            passengerPassword.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            passengerEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            passengerEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 8) {
            passengerPassword.setError("Minimum 8 characters");
            valid = false;
        } else {
            passengerPassword.setError(null);
        }
        if (number.isEmpty() || number.length() !=10) {
            passengerNumber.setError("Enter a number with 10 digits");
            valid = false;
        } else {
            passengerNumber.setError(null);
        }

        return valid;
    }

    public void checkAlready(String email, String number)
    {
        CheckIfRegistered cc = new CheckIfRegistered(getApplicationContext());
        cc.delegate = this;
        cc.execute(email,number);
    }

    @Override
    public void processFinish(String output) {
        if(output.equals("true"))
        {
            register();
        }
        else
        {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("You are already registered")
                    .show();
        }
    }

    public void register()
    {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        DataTransmitPost dtp =  new DataTransmitPost(this.getApplicationContext());
        dtp.delegate1 = this;

        String encpass = md5(password);

        int randomNum = 1001 + (int)(Math.random() * ((9999 - 1001) + 1));
        String otp = Integer.toString(randomNum);
        String status = "false";
        dtp.execute(name, email, number, encpass,otp,status);
    }

    public static String md5(String input) {
        String md5 = null;
        if(null == input) return null;

        try {
            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }
    @Override
    public void sendEmail(JSONObject output) {
        checkagain();
    }

    void checkagain()
    {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("SignUp Success")
                .customView(R.layout.verifyemail, wrapInScrollView)
                .positiveText("Verify")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText code = null;
                        View view = dialog.getCustomView();
                        code = (EditText) view.findViewById(R.id.verification_code);
                        String otp = code.getText().toString();
                        checkotpcode(otp);
                    }
                }).show();
    }

    void checkotpcode(String otp)
    {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Toast.makeText(Signup.this,"dsada",Toast.LENGTH_LONG).show();
        CheckOTP check = new CheckOTP(getApplicationContext());
        check.delegate2 = this;
        check.execute(email,otp);
    }

    @Override
    public void checkotp(String output) {
        if(output.equals("true"))
        {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText("Email verified successfully.")
                    .setConfirmText("OK")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            UpdateStatus updateStatus = new UpdateStatus(getApplicationContext());
                            updateStatus.delegate3 = Signup.this;
                            updateStatus.execute(email);
                        }
                    })
                    .show();
        }
        else
        {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("OTP incorrect.")
                    .setConfirmText("OK")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            checkagain();
                        }
                    })
                    .show();


        }
    }

    @Override
    public void statusUpdate(String output) {
        if(output.equals("success"))
        {
            editor.putString("USER_EMAIL",email);
            editor.putBoolean("IS_LOGGED_IN",true);
            editor.commit();
            Intent i = new Intent(getApplicationContext(),Dashboard.class);
            i.putExtra("USER_EMAIL",email);
            startActivity(i);
            finish();
        }
    }

}

class CheckIfRegistered extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate = null;
    public CheckIfRegistered(Context context) {
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("email",params[0])
                .add("phone_number",params[1])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/check/")
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
        Signup.pDialog.dismissWithAnimation();
        super.onPostExecute(res);
        String result = "";
        try {
            result = res.getString("flag");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error...")
                    .setContentText("Please Try Again")
                    .show();

        }

        delegate.processFinish(result);

    }
}

class DataTransmitPost extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate1;
    public DataTransmitPost(Context context) {
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("name",params[0])
                .add("email",params[1])
                .add("phone_number",params[2])
                .add("password",params[3])
                .add("otpcode",params[4])
                .add("status",params[5])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/register/")
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
//        Log.d("gfgdfgd",res.toString());
        String result = "";
        try {
            result = res.getString("flag");
        }
        catch (JSONException e)
        {
            e.printStackTrace();

            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }
        if(result.equals("true")) {
            delegate1.sendEmail(res);
        }
        Signup.pDialog.dismissWithAnimation();
    }
}

class CheckOTP extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate2 = null;
    public CheckOTP(Context context) {
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("email",params[0])
                .add("otpcode",params[1])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/checkotp/")
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
            result = res.getString("flag");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .title("Network Error")
                    .content("Please Try Again")
                    .positiveText("OK");
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
        delegate2.checkotp(result);
        Signup.pDialog.dismissWithAnimation();
    }
}

class UpdateStatus extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate3 = null;
    public UpdateStatus(Context context) {
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("email",params[0])
                .add("status","true")
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/updatestatus/")
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
            result = res.getString("flag");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .title("Network Error")
                    .content("Please Try Again")
                    .positiveText("OK");
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
        delegate3.statusUpdate(result);
    }
}

interface AsyncResponse {
    void processFinish(String output);
    void sendEmail(JSONObject output);
    void checkotp(String output);
    void statusUpdate(String output);
}
