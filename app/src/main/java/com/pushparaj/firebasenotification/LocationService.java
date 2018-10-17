package com.pushparaj.firebasenotification;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    GoogleApiClient mGoogleApiClient;
    private static final String TAG = "myPushpaTag";
    private static final long INTERVAL = 1000*20;
    private static final long FASTEST_INTERVAL = 1000 * 10;

    LocationRequest mLocationRequest;
    OkHttpClient client = new OkHttpClient().newBuilder()
            .writeTimeout(0,TimeUnit.MINUTES)
            .readTimeout(0,TimeUnit.MINUTES)
            .connectTimeout(0, TimeUnit.MINUTES).build();
    PowerManager.WakeLock wl;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        if (!wfl.isHeld()) {
            wfl.acquire();
        }
        return START_STICKY;
    }
    WifiManager.WifiLock wfl;
    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myWakeLock");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        WifiManager wm = (WifiManager)this.getSystemService(WIFI_SERVICE);
        wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "WifiLock");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //start location
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onLocationChanged(Location location){
        wl.acquire();
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("myPushpaTag",String.valueOf(System.currentTimeMillis())+" "+String.valueOf(location.getLatitude())+" "+String.valueOf(location.getLongitude())+" "+current_user.getUid());
        Map<String,Object> updates = new HashMap<String,Object>();
        updates.put("lat",location.getLatitude());
        updates.put("long",location.getLongitude());
        updates.put("token", FirebaseInstanceId.getInstance().getToken());
        updates.put("lastseen",System.currentTimeMillis());
        JSONObject myobj = new JSONObject(updates);
        try {
            request("https://notification-d3bd5.firebaseio.com/Users/"+current_user.getUid()+".json",myobj.toString());
        } catch (IOException e) {
            wl.release();
            wfl.release();
            Log.i("myPushpaTag","io : "+e.getMessage());
            stopSelf();
        }
    }

    private void request(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();
        Log.i("myPushpaTag","Before response");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("myPushpaTag","Failed http :" +e.getMessage());
                wl.release();
                wfl.release();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("myPushpaTag","After response");
                Log.i("myPushpaTag",response.toString());
                response.body().close();
                wfl.release();
                wl.release();
            }
        });
    }


}
