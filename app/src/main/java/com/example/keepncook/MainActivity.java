package com.example.keepncook;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.keepncook.dummy.DummyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ProductFragment.OnListFragmentInteractionListener {
    private FirebaseAuth mAuth;
    public static final String BROADCAST = "BROADCAST";
    private NotificationReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lunchScanPage();
            }
        });


        //Notification setup
        myReceiver = new NotificationReceiver();
        registerReceiver(myReceiver, new IntentFilter("com.example.keepncook.A_CUSTOM_ACTION"));

        startAlarm(true,true);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);


    }

    private void updateUI(FirebaseUser user) {
        if(user == null){
            Intent intentSignIn = new Intent(this, SigninActivity.class);
            startActivity(intentSignIn);
        } else {

        }
    }

    public void logout(View v){
        FirebaseAuth.getInstance().signOut();
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
    public void sendBroadcastToReceiver(View view){
        Log.i(BROADCAST,  "Sending Broadcast");
        //Intent intent = new Intent(this, MyReceiver.class);
        Intent myIntent = new Intent("com.example.keepncook.A_CUSTOM_ACTION");
        sendBroadcast(myIntent);

    }

    private void startAlarm(boolean isNotification, boolean isRepeat) {
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;
        PendingIntent pendingIntent;

        Calendar calendar= Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,19);
        calendar.set(Calendar.MINUTE,00);


        myIntent = new Intent("com.example.keepncook.A_CUSTOM_ACTION");
        pendingIntent = PendingIntent.getBroadcast(this,0,myIntent,0);


        if(!isRepeat)
            manager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime()+3000,pendingIntent);
        else
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pendingIntent);
    }

    public void lunchScanPage()
    {
        Intent intentScan = new Intent(this, ScanActivity.class);
        startActivity(intentScan);
    }
}
