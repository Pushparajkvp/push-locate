package com.pushparaj.firebasenotification;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class StartPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
    }

    public void newUser(View view) {
        Intent i = new Intent(StartPage.this,Register.class);
        startActivity(i);
    }

    public void existingUser(View view) {
        Intent i = new Intent(StartPage.this,SignIn.class);
        startActivity(i);
    }
}
