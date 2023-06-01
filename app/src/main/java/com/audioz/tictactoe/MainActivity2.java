package com.audioz.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity2 extends AppCompatActivity {

    //SharedPreference
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    //Buttons
    ImageButton btnleave;
    Button localbtn,multibtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Buttons
        btnleave = findViewById(R.id.btnleave);

//        botbtn = findViewById(R.id.botbtn);
        localbtn = findViewById(R.id.localbtn);
        multibtn = findViewById(R.id.multibtn);

        //onClicks
        btnleave.setOnClickListener(view -> finish());

        localbtn.setOnClickListener(view -> {
            Intent i = new Intent(this, localgame.class);
            startActivity(i);
        });
        multibtn.setOnClickListener(view -> {
            Intent i = new Intent(this, login.class);
            startActivity(i);
        });

    }
}