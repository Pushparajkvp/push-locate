package com.pushparaj.firebasenotification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

public class ChangeStatus extends AppCompatActivity {

    Toolbar mToolbar;
    DatabaseReference mRef;
    FirebaseUser user;
    EditText edit_status;
    ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);
        //Fields
        edit_status = (EditText)findViewById(R.id.Change_Status_Edit_Status);
        //Toolbar Setup
        mToolbar = (Toolbar)findViewById(R.id.toolbar_id);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Status Update");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mProgress = new ProgressDialog(this);
        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        //Intent Extras and set to edit text
        edit_status.setText(getIntent().getExtras().getString("status"));

    }

    public void ChnageStatusClicked(View view) {
        //Checking Conection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info == null || (!info.isConnectedOrConnecting())){
            Toast.makeText(ChangeStatus.this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show();
            return;
        }
        mProgress.setMessage("Updating Status...");
        mProgress.setTitle("Update in Progess...");
        mProgress.setCancelable(false);
        mProgress.show();
        //Update status in Firebase
        mRef.child("status").setValue(edit_status.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ChangeStatus.this,"Status Update Success",Toast.LENGTH_LONG).show();
                    mRef = FirebaseDatabase.getInstance().getReference().child("Friends");
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.child(user.getUid()).getChildren()) {
                                mRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(ds.getKey()).child(user.getUid());
                                Map<String, Object> myHash = new HashMap<>();
                                myHash.put("status", edit_status.getText().toString());
                                mRef.updateChildren(myHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            try {
                                                throw task.getException();
                                            } catch (Exception e) {
                                                Toast.makeText(ChangeStatus.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ChangeStatus.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                        finish();
                }else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(ChangeStatus.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
