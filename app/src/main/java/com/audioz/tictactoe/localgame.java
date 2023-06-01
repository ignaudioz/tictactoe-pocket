package com.audioz.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class localgame extends AppCompatActivity {
   private boardGame TicTacToe;
   private TextView currPlayer;
   private ImageView imageturn;
   private Button resetbtn,backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localgame);

        TicTacToe = findViewById(R.id.oBoard);
        currPlayer = findViewById(R.id.currPlayer);
        resetbtn = findViewById(R.id.resetbtn);
        backbtn = findViewById(R.id.backbtn);
        imageturn = findViewById(R.id.imageturn);

        // Settings player turns name so it won't be blank.
        String[] names = {"X player","O player"};
        currPlayer.setText(names[0] + "'s Turn");

        // Hiding buttons before setup.
        resetbtn.setVisibility(View.GONE);
        backbtn.setVisibility(View.GONE);

        // Calling game-setup to involve Visual objects from root activity/this activity.
        TicTacToe.setUpGame(resetbtn,backbtn,currPlayer,imageturn,names);

        backbtn.setOnClickListener(view -> {
            finish();
        });

        resetbtn.setOnClickListener(view -> {
            TicTacToe.resetGame();
            TicTacToe.invalidate();
        });
    }
}