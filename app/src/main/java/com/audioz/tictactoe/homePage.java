package com.audioz.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class homePage extends AppCompatActivity {

    //SharedPreference
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    //Switch
    Switch themeswitch;
    boolean thememode;
    //Buttons
    Button exitbtn,startbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        //SharedPreference
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        thememode = sharedPreferences.getBoolean("theme", false);
        //Switch
        themeswitch = findViewById(R.id.themeswitch);
        //Buttons
        exitbtn = findViewById(R.id.exitbtn);
        startbtn = findViewById(R.id.startbtn);

        if(thememode){
            themeswitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
            themeswitch.setText("\uD83C\uDF19");
        }else{
            themeswitch.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
            themeswitch.setText("☀️");
        }

        // if default theme is nightmode, thememode = true;
        thememode = AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES;

        themeswitch.setOnClickListener(view -> {
            if(thememode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                themeswitch.setText("☀️");
                editor.putBoolean("theme", false);
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                themeswitch.setText("\uD83C\uDF19");
                editor.putBoolean("theme", true);
            }
            thememode = !thememode;
            editor.apply();
        });

        exitbtn.setOnClickListener(view -> new AlertDialog.Builder(homePage.this)
                        .setTitle("Exit prompt")
                        .setMessage("Are you sure you wanna leave? ;(")
                        .setCancelable(true)

                        .setPositiveButton("exit", (dialogInterface, i) -> finish())
                        .setNegativeButton("cancel", (dialogInterface, i) -> Toast.makeText(homePage.this, "Nice ;)",
                                        Toast.LENGTH_SHORT).show())
                        .show()
        );

        startbtn.setOnClickListener(view -> {
            Intent i =new Intent(homePage.this, gameSelection.class);
            startActivity(i);
        });
    }
}