package com.pushparaj.firebasenotification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {

    Toolbar mToolbar;
    EditText Email,pass;

    SharedPreferences sharedpreferences;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //Fields
        Email = (EditText)findViewById(R.id.editEmail);
        pass = (EditText)findViewById(R.id.editPass);
        mToolbar=(Toolbar)findViewById(R.id.toolbar_id);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        sharedpreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        //Progress Dialog
        progressDialog = new ProgressDialog(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Push Locate");
        }
    }


    public void SignInClicked(View view) {
        //Check Connection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus()!=null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        if(activeNetwork == null || (!activeNetwork.isConnectedOrConnecting())){
            Toast.makeText(this,"Please check your internet connection",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            return;
        }
        //Input Vlidation
        final String email = Email.getText().toString();
        final String password = pass.getText().toString();
        if(email.equals("") || password.equals("")){
            Toast.makeText(this,"Please fill up the details",Toast.LENGTH_LONG).show();
            return;
        }
        //Progress Dialog Start
        progressDialog.setTitle("Signing In");
        progressDialog.setMessage("Validating Your Credentials...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        //Firebase Sign in
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            try{
                                throw  task.getException();
                            } catch (Exception e) {
                                Toast.makeText(SignIn.this, "Sign Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("email",email);
                        editor.apply();

                        String token = FirebaseInstanceId.getInstance().getToken();
                        Log.i("push",token+" This is Email "+email);
                        OkHttpClient client = new OkHttpClient();
                        RequestBody formBody = new FormBody.Builder()
                                .add("email",email)
                                .add("token",token)
                                .build();
                        final Request request = new Request.Builder()
                                .post(formBody)
                                .url("https://pushparajkvp.000webhostapp.com/firebase/noResponse/tokenRefresh.php")
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i("push","Failure in okHttp");
                                call.cancel();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.i("push","done "+response.body().string());
                            }
                        });
                        progressDialog.dismiss();
                        Intent i = new Intent(SignIn.this,MainPage.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                });
    }
}
