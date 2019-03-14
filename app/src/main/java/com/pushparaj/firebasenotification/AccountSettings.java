package com.pushparaj.firebasenotification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettings extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView txtStatus,txtDisplayName;
    Toolbar mToolbar;
    ProgressDialog mProgress;
    //Firebase
    DatabaseReference mRef;
    StorageReference mStorageRef;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        //fields
        circleImageView = (CircleImageView)findViewById(R.id.circular_image);
        txtStatus = (TextView)findViewById(R.id.txt_status);
        txtDisplayName=(TextView)findViewById(R.id.txt_display_name);

        //Toolbar setup
        mToolbar = (Toolbar)findViewById(R.id.toolbar_id);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Account Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mProgress = new ProgressDialog(this);

        //firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mRef.keepSynced(true);
        // Read from the database
        mProgress.setMessage("Please Wait");
        mProgress.setTitle("Loading");
        mProgress.show();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String)dataSnapshot.child("name").getValue();
                String status = (String) dataSnapshot.child("status").getValue();
                final String image = (String) dataSnapshot.child("image").getValue();
                String thumb_image =(String) dataSnapshot.child("tumb_image").getValue();
                txtDisplayName.setText(name);
                txtStatus.setText(status);
                assert image != null;
                if(!image.equals("default"))
                Picasso.with(AccountSettings.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.hp).into(circleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(AccountSettings.this).load(image).placeholder(R.drawable.hp).into(circleImageView);
                    }
                });
                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError error) {
               Toast.makeText(AccountSettings.this,error.getMessage(),Toast.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        });

    }

    public void ChangePicClicked(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        Uri uri = Uri.parse(path);
        i.setDataAndType(uri,"image/*");
        startActivityForResult(i,100);
    }

    public void ChangeStatusClicked(View view) {
        Intent i = new Intent(AccountSettings.this,ChangeStatus.class);
        i.putExtra("status",txtStatus.getText().toString());
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 100){
            CropImage.activity(data.getData())
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();
                //Checking Connectivity
                ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if(networkInfo == null || (!networkInfo.isConnectedOrConnecting())){
                    Toast.makeText(AccountSettings.this,"Please Check Your Connection",Toast.LENGTH_LONG).show();
                    return;
                }
                mProgress.setMessage("Updating Your Picture");
                mProgress.setTitle("Updating..");
                mProgress.setCancelable(false);
                mProgress.show();
                final StorageReference mStorageRefNormal= mStorageRef.child("Users").child(user.getUid()+".jpg");
                mStorageRefNormal.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task_image) {
                        if(task_image.isSuccessful()){
                            File path = new File(resultUri.getPath());
                            Bitmap thumbBitmap = new Compressor(AccountSettings.this)
                                    .setMaxHeight(200)
                                    .setMaxWidth(200)
                                    .setQuality(30)
                                    .compressToBitmap(path);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                            byte[] data = baos.toByteArray();
                            mStorageRef =FirebaseStorage.getInstance().getReference().child("Users_thumb").child(user.getUid()+".jpg");
                            mStorageRef.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        String thumb_download_link = mStorageRefNormal.getDownloadUrl().toString();
                                        final String image_download_link =  mStorageRef.getDownloadUrl().toString();
                                        Map<String,Object> update_map = new HashMap<>();
                                        update_map.put("image",image_download_link);
                                        update_map.put("tumb_image",thumb_download_link);
                                        mRef.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(AccountSettings.this,"Image Update Success",Toast.LENGTH_LONG).show();
                                                    mProgress.dismiss();
                                                }else{
                                                    try {
                                                        throw task.getException();
                                                    } catch (Exception e) {
                                                        mProgress.dismiss();
                                                        Toast.makeText(AccountSettings.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
                                        });
                                    }else{
                                        try {
                                            throw task.getException();
                                        } catch (Exception e) {
                                            mProgress.dismiss();
                                            Toast.makeText(AccountSettings.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                        }else {
                            try {
                                throw task_image.getException();
                            } catch (Exception e) {
                                mProgress.dismiss();
                                Toast.makeText(AccountSettings.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                try {
                    throw  error;
                } catch (Exception e) {
                    Toast.makeText(AccountSettings.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
