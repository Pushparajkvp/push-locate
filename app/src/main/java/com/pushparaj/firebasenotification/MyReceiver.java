package com.pushparaj.firebasenotification;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class MyReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("myPushpaTag","Started Listener");
        if(isMyServiceRunning(LocationService.class,context)){
            Log.i("myPushpaTag","already running");
            return;
        }
        Log.i("myPushpaTag","started");
        Intent i = new Intent(context.getApplicationContext(),LocationService.class);
        startWakefulService(context,i);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
