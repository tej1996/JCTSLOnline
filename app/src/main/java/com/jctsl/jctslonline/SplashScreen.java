package com.jctsl.jctslonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jctsl.jctslonline.ConductorDept.ConductorDashboard;
import com.jctsl.jctslonline.ConductorDept.ConductorLogin;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences pref,prefcond;
    public static UserLocation gps;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_login:
                    Intent login = new Intent(getApplicationContext(),Login.class);
                    startActivity(login);
                    finish();
                    return true;
                case R.id.navigation_signup:
                    Intent signup = new Intent(getApplicationContext(),Signup.class);
                    startActivity(signup);
                    finish();
                    return true;
                case R.id.navigation_conlogin:
                    Intent conlogin = new Intent(getApplicationContext(),ConductorLogin.class);
                    startActivity(conlogin);
                    finish();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();


        gps = new UserLocation(this);


        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }

        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }

        pref = getApplicationContext().getSharedPreferences("LoginPref",0);
        prefcond = getApplicationContext().getSharedPreferences("LoginCondPref",0);
        boolean is_logged_in =  pref.getBoolean("IS_LOGGED_IN", false);
        boolean is_cond_logged_in =  prefcond.getBoolean("IS_COND_LOGGED_IN", false);
        String email = pref.getString("USER_EMAIL",null);
        String conductor_username = pref.getString("CONDUCTOR_USERNAME",null);

        if(is_logged_in)
        {
            Intent i = new Intent(SplashScreen.this,Dashboard.class);
            i.putExtra("USER_EMAIL",email);
            startActivity(i);
            finish();
        }else if(is_cond_logged_in){
            Intent i = new Intent(SplashScreen.this,ConductorDashboard.class);
            i.putExtra("CONDUCTOR_USERNAME",conductor_username);
            startActivity(i);
            finish();
        }

        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.anim);
                navigation.startAnimation(fadeInAnimation);
                navigation.setVisibility(View.VISIBLE);
                // close this activity
            }
        }, 2500);


    }

}
