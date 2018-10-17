package com.pushparaj.firebasenotification;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AllUserProfile extends AppCompatActivity {
    CircleImageView circleImageView;
    TextView name,status,txt_friends;
    String user_id,friendship,name_txt,status_txt,image;
    Button request,decline;
    DatabaseReference firebaseRequest;
    ProgressDialog progressDialog;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user_profile);
        circleImageView = (CircleImageView)findViewById(R.id.circular_image);
        name = (TextView)findViewById(R.id.txt_display_name);
        status = (TextView)findViewById(R.id.txt_status);
        request = (Button)findViewById(R.id.send_request);
        decline = (Button)findViewById(R.id.decline_request);
        txt_friends=(TextView)findViewById(R.id.txt_friends);
        decline.setVisibility(View.INVISIBLE);
        decline.setEnabled(false);
        friendship="";
        user_id=getIntent().getExtras().getString("user_id");
        name_txt =getIntent().getExtras().getString("name");
        status_txt = getIntent().getExtras().getString("status");
        image=getIntent().getExtras().getString("pic");
        name.setText(name_txt);
        status.setText(status_txt);
        Picasso.with(circleImageView.getContext()).load(image).placeholder(R.drawable.hp).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(circleImageView.getContext()).load(image).placeholder(R.drawable.hp).into(circleImageView);
            }
        });


        //Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();


        //Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser.getUid().equals(user_id)){
            request.setEnabled(false);
            request.setVisibility(View.INVISIBLE);
            txt_friends.setText("Your Profile");
        }
        //firebase friends
        firebaseRequest = FirebaseDatabase.getInstance().getReference().child("Requests");
        firebaseRequest.keepSynced(true);
        firebaseRequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(currentUser.getUid()).child(user_id).child("request_type").getValue()!= null){
                        if(dataSnapshot.child(currentUser.getUid()).child(user_id).child("request_type").getValue().equals("sent")) {
                            friendship = "Request_Sent";
                            //Toast.makeText(AllUserProfile.this, friendship, Toast.LENGTH_LONG).show();
                        }else if(dataSnapshot.child(currentUser.getUid()).child(user_id).child("request_type").getValue().toString().equals("got")){
                            friendship = "Request_Received";
                           // Toast.makeText(AllUserProfile.this,friendship,Toast.LENGTH_LONG).show();
                        }
                    }
                    if(friendship.equals("Request_Sent")){
                        request.setText("Cancel Request");
                        progressDialog.dismiss();
                    }else if(friendship.equals("Request_Received")){
                        request.setText("Accept");
                        decline.setEnabled(true);
                        decline.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    }else {

                        firebaseRequest = FirebaseDatabase.getInstance().getReference().child("Friends");
                        firebaseRequest.keepSynced(true);
                        firebaseRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(currentUser.getUid()).hasChild(user_id)) {
                                    friendship = "Friends";
                                    //Toast.makeText(AllUserProfile.this, friendship, Toast.LENGTH_LONG).show();
                                } else {
                                    friendship = "Not_Friends";
                                   // Toast.makeText(AllUserProfile.this, friendship, Toast.LENGTH_LONG).show();
                                }
                                if (friendship.equals("Friends")) {
                                    request.setText("UnFriend");
                                } else if (friendship.equals("Not_Friends") || friendship.equals("")) {
                                    request.setText("Send Request");
                                }
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                progressDialog.dismiss();
                                Toast.makeText(AllUserProfile.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(AllUserProfile.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    public void SendRequest(View view) {
        //Toast.makeText(AllUserProfile.this,friendship,Toast.LENGTH_LONG).show();
        //Check Connection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork == null || (!activeNetwork.isConnectedOrConnecting())){
            Toast.makeText(this,"Please check your internet connection",Toast.LENGTH_LONG).show();
            return;
        }
        request.setEnabled(false);
        if(friendship.equals("Not_Friends")||friendship.equals("")){
            //firebase
            firebaseRequest = FirebaseDatabase.getInstance().getReference("Requests");
            firebaseRequest.keepSynced(true);
            HashMap<String, Object> update = new HashMap<>();
            update.put(currentUser.getUid()+"/"+user_id+"/"+"/request_type","sent");
            update.put(user_id+"/"+currentUser.getUid()+"/"+"/request_type","got");
            firebaseRequest.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(AllUserProfile.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }else{
                        firebaseRequest = FirebaseDatabase.getInstance().getReference().child("Users");
                        firebaseRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child(user_id).hasChild("token")) {
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody formBody = new FormBody.Builder()
                                            .add("token", dataSnapshot.child(user_id).child("token").getValue().toString())
                                            .add("mess", dataSnapshot.child(currentUser.getUid()).child("name").getValue().toString() + " Sent You A Friend Request")
                                            .build();
                                    Request request = new Request.Builder().post(formBody).url("https://pushparajkvp.000webhostapp.com/firebase/noResponse/sendNotification.php").build();
                                    client.newCall(request).enqueue(new okhttp3.Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.i("pushpa", "Failure in okHttp");
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            Log.i("pushpa", response.toString() + " okHttp");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        friendship="Request_Sent";
                        request.setText("Cancel Request");
                        request.setEnabled(true);
                        Toast.makeText(AllUserProfile.this,"Friend Request Sent!",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
        if(friendship.equals("Request_Sent")){
            firebaseRequest = FirebaseDatabase.getInstance().getReference();
            firebaseRequest.keepSynced(true);
            HashMap<String,Object> update = new HashMap<String,Object>();
            update.put("Requests/"+currentUser.getUid()+"/"+user_id+"/request_type",null);
            update.put("Requests/"+user_id+"/"+currentUser.getUid()+"/request_type",null);
            firebaseRequest.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(AllUserProfile.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }else{
                        friendship="Not_Friends";
                        request.setEnabled(true);
                        request.setText("Send Request");
                    }
                }
            });
        }
        if(friendship.equals("Request_Received")){
            //Toast.makeText(AllUserProfile.this,"Came inside",Toast.LENGTH_LONG).show();
            firebaseRequest = FirebaseDatabase.getInstance().getReference();
            firebaseRequest.keepSynced(true);
            HashMap<String,Object> update = new HashMap<String,Object>();
            update.put("Requests/"+currentUser.getUid()+"/"+user_id+"/request_type",null);
            update.put("Requests/"+user_id+"/"+currentUser.getUid()+"/request_type",null);
            update.put("Friends/"+currentUser.getUid()+"/"+user_id+"/date",String.valueOf(new Timestamp(System.currentTimeMillis())));
            update.put("Friends/"+user_id+"/"+currentUser.getUid()+"/date",String.valueOf(new Timestamp(System.currentTimeMillis())));
            firebaseRequest.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(AllUserProfile.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }else{
                        firebaseRequest = FirebaseDatabase.getInstance().getReference().child("Users");
                        firebaseRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child(user_id).hasChild("token")) {
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody formBody = new FormBody.Builder()
                                            .add("token", dataSnapshot.child(user_id).child("token").getValue().toString())
                                            .add("mess", dataSnapshot.child(currentUser.getUid()).child("name").getValue().toString() + " Accepted Your Friend Request")
                                            .build();
                                    Request request = new Request.Builder().post(formBody).url("https://pushparajkvp.000webhostapp.com/firebase/noResponse/sendNotification.php").build();
                                    client.newCall(request).enqueue(new okhttp3.Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.i("pushpa", "Failure in okHttp");
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            Log.i("pushpa", response.toString() + " okHttp");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        friendship="Friends";
                        request.setEnabled(true);
                        request.setText("UnFriend");
                        decline.setEnabled(false);
                        decline.setVisibility(View.INVISIBLE);
                        Toast.makeText(AllUserProfile.this,"Friend Request Accepted!",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
        if(friendship.equals("Friends")){
            firebaseRequest= FirebaseDatabase.getInstance().getReference();
            firebaseRequest.keepSynced(true);
            HashMap<String,Object> update = new HashMap<String,Object>();
            update.put("Friends/"+currentUser.getUid()+"/"+user_id,null);
            update.put("Friends/"+user_id+"/"+currentUser.getUid(),null);
            firebaseRequest.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(AllUserProfile.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }else{
                        firebaseRequest = FirebaseDatabase.getInstance().getReference().child("Users");
                        firebaseRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child(user_id).hasChild("token")) {
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody formBody = new FormBody.Builder()
                                            .add("token", dataSnapshot.child(user_id).child("token").getValue().toString())
                                            .add("mess", dataSnapshot.child(currentUser.getUid()).child("name").getValue().toString() + " UnFriended You")
                                            .build();
                                    Request request = new Request.Builder().post(formBody).url("https://pushparajkvp.000webhostapp.com/firebase/noResponse/sendNotification.php").build();
                                    client.newCall(request).enqueue(new okhttp3.Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.i("pushpa", "Failure in okHttp");
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            Log.i("pushpa", response.toString() + " okHttp");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        friendship="Not_Friends";
                        request.setEnabled(true);
                        request.setText("Send Request");
                        decline.setEnabled(false);
                        decline.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

    }

    public void Decline(View view) {
        firebaseRequest = FirebaseDatabase.getInstance().getReference();
        firebaseRequest.keepSynced(true);
        HashMap<String,Object> update = new HashMap<String,Object>();
        update.put("Requests/"+currentUser.getUid()+"/"+user_id+"/request_type",null);
        update.put("Requests/"+user_id+"/"+currentUser.getUid()+"/request_type",null);
        firebaseRequest.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(AllUserProfile.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }else{
                    firebaseRequest = FirebaseDatabase.getInstance().getReference().child("Users");
                    firebaseRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(user_id).hasChild("token")) {
                                OkHttpClient client = new OkHttpClient();
                                RequestBody formBody = new FormBody.Builder()
                                        .add("token", dataSnapshot.child(user_id).child("token").getValue().toString())
                                        .add("mess", dataSnapshot.child(currentUser.getUid()).child("name").getValue().toString() + " Declined Your Frined Request")
                                        .build();
                                Request request = new Request.Builder().post(formBody).url("https://pushparajkvp.000webhostapp.com/firebase/noResponse/sendNotification.php").build();
                                client.newCall(request).enqueue(new okhttp3.Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.i("pushpa", "Failure in okHttp");
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        Log.i("pushpa", response.toString() + " okHttp");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    friendship="Not_Friends";
                    request.setEnabled(true);
                    request.setText("Send Request");
                    decline.setEnabled(false);
                    decline.setVisibility(View.INVISIBLE);
                }
            }
        });


    }
}
