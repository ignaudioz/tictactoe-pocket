package com.audioz.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Room extends AppCompatActivity {
    // TicTacToe boardgame interface.
    private boardOnlineGame onlineGameboard;
    // TextView
    private TextView playerturn;
    // ImageView
    private ImageView pfpImageView;
    // Buttons
    private Button backbutton,leavebtn;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private DatabaseReference mRoom;
    private ValueEventListener lRoom;
    // Strings
    private String roomName,guest,currentPlayer,role="host";

    //ProgressDialog
    private ProgressDialog pg;
    private AlertDialog.Builder ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        dataBase = FirebaseDatabase.getInstance("https://tic-tac-toe-pocket-default-rtdb.europe-west1.firebasedatabase.app/");

        mAuth = FirebaseAuth.getInstance();

        currentPlayer = mAuth.getCurrentUser().getDisplayName();

        //TicTacToe interface.
        onlineGameboard = findViewById(R.id.onlineGameboard);
        //TextView
        playerturn = findViewById(R.id.playerturn);
        // ImageView
        pfpImageView = findViewById(R.id.pfpImageView);
        //Buttons
        backbutton = findViewById(R.id.backbutton);
        leavebtn = findViewById(R.id.leavebtn);

        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");
        // Setting reference.
        mRoom = dataBase.getReference("Rooms/"+roomName+"/players");

        Bitmap defaultAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.default_profile);

        // Hiding buttons before setup.
        backbutton.setVisibility(View.GONE);

        // backbutton removing listeners.
        backbutton.setOnClickListener(view -> {
            endGame();
            finish();
        });

        leavebtn.setOnClickListener(view -> {
            if(backbutton.getVisibility()==View.VISIBLE){
                endGame();
                finish();
            }
            new AlertDialog.Builder(Room.this)
                    .setTitle("Exit prompt")
                            .setMessage("Are you sure you wanna leave? ;(")
                            .setCancelable(true)

                            .setPositiveButton("exit", (dialogInterface, i) -> {
                                if(!currentPlayer.equals(roomName))
                                    onlineGameboard.leavegame("player2");
                                else
                                    onlineGameboard.leavegame("player1");

                                Room.this.finish(); // just in-case
                            })
                            .setNegativeButton("cancel", (dialogInterface, i)->{})//Do nothing.
                    .show();
                });



        if(!currentPlayer.equals(roomName)) {
            // Setting up alert dialog in-case the host leaves.
            ad = new AlertDialog.Builder(Room.this)
                    .setTitle("Player left!")
                    .setMessage("The host ("+roomName+") left the game.")
                    .setPositiveButton("ok", (dialogInterface, i) -> {
                        endGame();
                    });

            role = "guest";
            String[] names = {roomName,currentPlayer};
            Toast.makeText(Room.this,"Connected!",Toast.LENGTH_SHORT).show();
            // Calling game-setup to involve Visual objects from root activity/this activity.
            onlineGameboard.setUpGame(backbutton,playerturn,pfpImageView,names,role,ad,defaultAvatar);
        }else
        {
            // Setting up progress dialog.
            pg = new ProgressDialog(this);
            pg.setTitle("Wait");
            pg.setMessage("Waiting for an opponent to arrive");
            pg.setCancelable(false);
            pg.setButton(DialogInterface.BUTTON_NEGATIVE,"Leave?",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   mRoom.removeEventListener(lRoom); // removing listener so it won't crash.
                   endGame(); // ending game.
                }
            });
            pg.show();
            // Setting up alert-dialog in-case the guest leaves.
            ad = new AlertDialog.Builder(Room.this)
                    .setTitle("Player left!")
                    .setMessage("The guest ("+guest+") left the game.")
                    .setPositiveButton("ok", (dialogInterface, i) -> {
                        endGame();
                    });


            mRoom.addListenerForSingleValueEvent(lRoom = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild("player2")) {
                        guest = snapshot.child("player2/name").getValue(String.class); // Getting guest's player-name.
                        // Settings player turns name so it won't be blank.
                        String[] names = {roomName, guest};
//                        playerturn.setText(names[0] + "'s Turn");

//                        pfpImageView.setImageURI(mAuth.getCurrentUser().getPhotoUrl());

                        pg.cancel();
                        Toast.makeText(Room.this,"Found a match!",Toast.LENGTH_SHORT).show();
                        // Calling game-setup to involve Visual objects from root activity/this activity.
                        onlineGameboard.setUpGame(backbutton,playerturn,pfpImageView,names,role,ad,defaultAvatar);
                        mRoom.removeEventListener(this);
                    }
                    // else do nothing since it's reacting to player1 node.
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Room.this,"Error:"+error.toString(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void endGame(){
//        mRoom.removeEventListener(lRoom);
//        mRoom.getParent().removeValue(); // removing room node
        dataBase.getReference("Rooms/"+roomName).removeValue(); // removing Room node.
        finish();
    }
}