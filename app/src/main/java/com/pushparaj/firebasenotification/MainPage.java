package com.pushparaj.firebasenotification;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.util.HashMap;


public class MainPage extends AppCompatActivity {

    EditText editEmail,editPass;
    FirebaseAuth mAuth;
    Toolbar mToolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    PagerAdapter pagerAdapter;
    ProgressDialog pd;
    AlarmManager alarms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Fields
        editEmail = (EditText)findViewById(R.id.editEmail);
        editPass = (EditText)findViewById(R.id.editPass);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_id);
        //Toolbar Setup
        setSupportActionBar(mToolbar);
        pd=new ProgressDialog(this);
        //Firebase
        mAuth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Push Locate");
        }
        //Tab Layout
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            pd.setMessage("Please Wait While Loading.");
            pd.setTitle("Loading");
            pd.show();
            return;
        }


    }

    private boolean checkAlarm() {
        Intent intent = new Intent(this, MyReceiver.class);//the same as up
        intent.setAction("com.pushparaj.firebasenotification.alarm");//the same as up
        return (PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance()==null || FirebaseAuth.getInstance().getCurrentUser()==null){
            sendToStartPage();
            finish();
            return;
        }
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        FirebaseDatabase.getInstance().goOnline();
        //Alarm Manager
        removeAlarm();
        removeService();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        removeAlarm();
        removeService();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled,Please enable it")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==100){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                viewPager = (ViewPager)findViewById(R.id.view_pager);
                pagerAdapter = new PagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(pagerAdapter);
                viewPager.setOffscreenPageLimit(3);
                tabLayout = (TabLayout)findViewById(R.id.main_tab);
                tabLayout.setupWithViewPager(viewPager);
                pd.dismiss();
            }
        }
    }
    private void sendToStartPage() {
        Intent i = new Intent(MainPage.this,StartPage.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_page_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_log_out){
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("token");
            mRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                    }else{
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(MainPage.this,"Check Internet",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            });
            mAuth.signOut();
            removeAlarm();
            removeService();
            sendToStartPage();
        }else if(item.getItemId() == R.id.menu_account_settings){
            Intent i = new Intent(MainPage.this,AccountSettings.class);
            startActivity(i);
        }else if(item.getItemId()==R.id.toolbar_button){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            sendNotificationToAll();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to send an SOS alert to all your friends?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        return true;
    }

    private void removeService() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                stopService(new Intent(MainPage.this,LocationService.class));
                Log.i("myPushpaTag","removed Service");
            }
        }
    }

    private void removeAlarm() {
        if(checkAlarm()) {
            Intent intent = new Intent(MainPage.this, MyReceiver.class);//the same as up
            intent.setAction("com.pushparaj.firebasenotification.alarm");//the same as up
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainPage.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);//the same as up
            alarms.cancel(pendingIntent);//important
            pendingIntent.cancel();
            Log.i("myPushpaTag","removed Alarm");
        }
    }

    private void sendNotificationToAll() {
        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("Users/"+user_id);
        nameRef.keepSynced(true);
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String name= dataSnapshot.child("name").getValue().toString();
                DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Friends/"+user_id);
                friendsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("notification");
                        HashMap<String,Object> update = new HashMap<String,Object>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            String key = mRef.child(ds.getKey()).push().getKey();
                            update.put(ds.getKey()+"/"+key+"/mess",name+" sent an SOS alert!");
                        }
                        mRef.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MainPage.this,"SOS alert Sent!",Toast.LENGTH_LONG).show();
                                }else{
                                    try {
                                        throw task.getException();
                                    } catch (Exception e) {
                                        Toast.makeText(MainPage.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().goOffline();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStop() {
        if(!checkAlarm()){
            Intent i = new Intent(this, MyReceiver.class);
            i.setAction("com.pushparaj.firebasenotification.alarm");
            PendingIntent recurringLl24 = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            Log.i("myPushpaTag","AlarmCreated");
            alarms.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1000*60, recurringLl24);
        }
        super.onStop();
    }
}
