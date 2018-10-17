package com.pushparaj.firebasenotification;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

import org.xml.sax.helpers.LocatorImpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class Register extends AppCompatActivity {

    EditText Email,pass,DisplayName;
    Toolbar mToolbar;

    FirebaseAuth mAuth;
    DatabaseReference myDatabaseReference;
    StorageReference mStorageRef;
    DatabaseReference mRef;

    //SharedPreferences sharedpreferences;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Fields
        Email = (EditText)findViewById(R.id.editEmail);
        pass = (EditText)findViewById(R.id.editPass);
        mToolbar = (Toolbar)findViewById(R.id.toolbar_id);
        DisplayName = (EditText)findViewById(R.id.editDisplayName);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        myDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();



        //sharedpreferences = getSharedPreferences("MyPref",MODE_PRIVATE);;
        //Progress Dialogue
        progressDialog = new ProgressDialog(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Push Locate");
        }
    }

    public void RegisterClicked(View view) {
        //Check Connection
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus()!=null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        if (activeNetwork == null || (!activeNetwork.isConnectedOrConnecting())) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            return;
        }
        //Fields Validation
        final String email = Email.getText().toString();
        final String password = pass.getText().toString();
        final String displayName = DisplayName.getText().toString();
        if (email.equals("") || password.equals("") || displayName.equals("")) {
            Toast.makeText(this, "Please fill up the details", Toast.LENGTH_LONG).show();
            return;
        }
        //Progress Dialog Start
        progressDialog.setTitle("Registering");
        progressDialog.setMessage("Registering you as a new user...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        //Firebase Authentication Process in Different Thread and Call Back

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("testerPush","in OnCOmplete");
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(Register.this, "Register Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user == null) {
                            Toast.makeText(Register.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            return;
                        }
                        FirebaseDatabase.getInstance().goOnline();
                        final HashMap<String, Object> update_map = new HashMap<>();
                        //update_map.put("token",FirebaseInstanceId.getInstance().getToken());
                        update_map.put("name", displayName);
                        update_map.put("status", "default status");
                        update_map.put("image", "https://firebasestorage.googleapis.com/v0/b/notification-d3bd5.appspot.com/o/Users%2Fearth.jpg?alt=media&token=445bb537-bf53-4cf4-951f-811d5fd87f0a");
                        update_map.put("lastseen",System.currentTimeMillis());
                        update_map.put("tumb_image", "https://firebasestorage.googleapis.com/v0/b/notification-d3bd5.appspot.com/o/Users_thumb%2Fearth.png?alt=media&token=6f00fe88-e90b-41e8-bd35-df4f9f4754f9");
                        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                        mRef.keepSynced(true);
                        mRef.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i("testerPush","in update complete");
                                    progressDialog.dismiss();
                                    //Toast.makeText(Register.this,"Yes",Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(Register.this, MainPage.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        user.delete();
                                    }
                                }

                            }
                        });


                    }
                });
    }
}


