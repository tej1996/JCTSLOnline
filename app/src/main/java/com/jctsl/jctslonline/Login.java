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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.processbutton.iml.ActionProcessButton;

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
import static com.jctsl.jctslonline.Signup.md5;

public class Login extends AppCompatActivity implements AsyncResponse{

    private ActionProcessButton loginButton;
    private EditText emailText,passwordText;
    private TextView signup,forgotpass;
    private CheckBox showpass;
    MaterialDialog dialog;
    Button sendotp;
    View view;
    int otpcode;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static SweetAlertDialog pDialog;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        loginButton = (ActionProcessButton) findViewById(R.id.btnSignIn);

        emailText = (EditText) findViewById(R.id.login_email);

        passwordText = (EditText) findViewById(R.id.login_password);
        signup = (TextView) findViewById(R.id.txtview_signup);
        forgotpass = (TextView) findViewById(R.id.txtview_forgotpass);
        showpass = (CheckBox) findViewById(R.id.chkbox_showpass);

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

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        //signup
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Signup.class);
                startActivity(i);
                finish();
            }
        });

        //forgotpassword
        boolean wrapInScrollView = true;
        dialog  =   new MaterialDialog.Builder(Login.this)
                .customView(R.layout.forgotpass, wrapInScrollView)
                .positiveText("Submit")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        //   View view = dialog.getCustomView();
                        view = dialog.getCustomView();
                        EditText otp = (EditText) view.findViewById(R.id.otp);
                        EditText emailid = (EditText) view.findViewById(R.id.customer_email);
                        EditText newpass = (EditText) view.findViewById(R.id.newpassword);


                        String email = emailid.getText().toString();

                        if(otp.getText().toString().equals(Integer.toString(otpcode))) {
                            UpdatePassword updatePassword = new UpdatePassword(Login.this);
                            String newpass1 = md5(newpass.getText().toString());
                            if(newpass.getText().toString().length()<8){
                                newpass.setError("Minimum 8 characters");
                                Toast.makeText(getApplicationContext(),"Password (minimum 8 characters), try again!",Toast.LENGTH_LONG).show();
                            }else{
                                updatePassword.execute(email,newpass1);
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"OTP is incorrect",Toast.LENGTH_LONG).show();
                        }

                    }
                }).build();


        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                dialog.show();

                view = dialog.getCustomView();
                sendotp = (Button) view.findViewById(R.id.sendotp);
                sendotp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view = dialog.getCustomView();
                        EditText emailid = (EditText) view.findViewById(R.id.customer_email);
                        String email = emailid.getText().toString();

                        sendotp(email);
                    }
                });

            }
        });

        pref = getSharedPreferences("LoginPref", 0);
        editor = pref.edit();

    }

    public void login() {

        if (!validate()) {
          //  onLoginFailed();
            return;
        }

        pDialog = new SweetAlertDialog(Login.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Authenticating....");
        pDialog.setCancelable(false);
        pDialog.show();

        email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        AuthenticateUser(email,password);


    }

    public void AuthenticateUser(String email, String password)
    {
        CheckUserLogin check = new CheckUserLogin(getApplicationContext());
        check.delegate = this;
        String encpass = md5(password);

        check.execute(email,encpass);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            emailText.requestFocus();
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 8) {
            passwordText.setError("Minimum 8 characters");
            passwordText.requestFocus();
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
    void sendotp(String email)
    {
        SendForgotOTP sendForgotOTP = new SendForgotOTP(getApplicationContext());
        sendForgotOTP.delegate = this;
        int randomNum = 1001 + (int)(Math.random() * ((9999 - 1001) + 1));
        otpcode = randomNum;
        String otp = Integer.toString(randomNum);
        sendForgotOTP.execute(email,otp);
    }


    @Override
    public void processFinish(String output) {
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
        else if(output.equals("verify"))
        {
            checkagain();
        }
        else if(output.equals("You have entered wrong password"))
        {
            new SweetAlertDialog(Login.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("Wrong Password!")
                    .show();
        }
        else if(output.equals("You are not registered"))
        {
            new SweetAlertDialog(Login.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("You are not registered!")
                    .show();
        }


    }

    @Override
    public void sendEmail(JSONObject output) {
    }

    @Override
    public void checkotp(String output) {

        if(output.equals("true"))
        {
            UpdateStatus1 updateStatus = new UpdateStatus1(getApplicationContext());
            updateStatus.delegate3 = this;
            updateStatus.execute(email);

        }
        else
        {
            Toast.makeText(getApplicationContext(),"Not Correct",Toast.LENGTH_LONG).show();
            checkagain();
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

    void checkagain()
    {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(Login.this)
                .title("SignUp Success")
                .customView(R.layout.verifyemail, wrapInScrollView)
                .positiveText("Verify")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText code = null;
                        View view = dialog.getCustomView();
                        try {
                            code = (EditText) view.findViewById(R.id.verification_code);
                            String otp = code.getText().toString();
                            checkotpcode(otp);
                        }
                        catch (NullPointerException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }).show();


    }
    void checkotpcode(String otp)
    {
        CheckOTPLogin check = new CheckOTPLogin(Login.this);
        check.delegate2 = this;
        check.execute(email,otp);
    }

}

class UpdatePassword extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate = null;
    public UpdatePassword(Context context) {
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
                .add("email",params[0])
                .add("password",params[1])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/updatepassword/")
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
            result = res.getString("flag");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }

        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success!")
                .setContentText("Password changed Successfully!")
                .show();

    }
}

class CheckUserLogin extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate = null;
    public CheckUserLogin(Context context) {
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("email",params[0])
                .add("password",params[1])
                .build();

        Request request = new Request.Builder()
                .url("http://tejasv.pythonanywhere.com/checklogin/")
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
        delegate.processFinish(result);

        Login.pDialog.dismiss();
    }
}

class SendForgotOTP extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate = null;
    public SendForgotOTP(Context context) {
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
                .url("http://tejasv.pythonanywhere.com/sendforgototp/")
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

            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }
        delegate.processFinish(result);

        //Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

    }
}

class CheckOTPLogin extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate2 = null;
    public CheckOTPLogin(Context context) {
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
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }
        delegate2.checkotp(result);

    }
}
class UpdateStatus1 extends AsyncTask<String,String,JSONObject>
{
    private Context context;
    public AsyncResponse delegate3 = null;
    public UpdateStatus1(Context context) {
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
        catch (Exception e)
        {
            e.printStackTrace();
            new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Network Error")
                    .setContentText("Please Try Again!")
                    .show();
        }
        delegate3.statusUpdate(result);

    }
}