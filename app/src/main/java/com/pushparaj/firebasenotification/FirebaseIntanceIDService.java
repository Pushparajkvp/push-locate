package com.pushparaj.firebasenotification;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FirebaseIntanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        final String Token = FirebaseInstanceId.getInstance().getToken();
        if(FirebaseAuth.getInstance()!=null && FirebaseAuth.getInstance().getCurrentUser()!=null && FirebaseAuth.getInstance().getCurrentUser().getUid()!=null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            Map<String, Object> updates = new HashMap<String, Object>();
            updates.put("token", Token);
            mRef.updateChildren(updates);
        }
    }

   /* private void register(String token) {
        //Checking Connection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork == null || (!activeNetwork.isConnectedOrConnecting())){
            register(token);
        }

        //Getting the email from shared preference
        SharedPreferences preferences = getSharedPreferences("MyPref",MODE_PRIVATE);;
        String email = preferences.getString("email", "DEFAULT");
        if(email.equals("DEFAULT"))
            return;

        //Php File Call
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                                .add("token",token)
                                .add("email",email)
                                .build();
        Request request = new Request.Builder().post(formBody).url("https://pushparajkvp.000webhostapp.com/firebase/noResponse/tokenRefresh.php").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("push","Failure in okHttp");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }*/
}
