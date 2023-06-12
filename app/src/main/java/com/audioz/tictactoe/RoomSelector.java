package com.audioz.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomSelector extends AppCompatActivity {
    // textView
    TextView activeRoomtxtview;
    // Buttons
    private Button setupRoom,logoutbtn;
    //ImageButton
    private ImageButton leavesetup;
    // ListView
    private ListView listView;
    // Strings
    private String username,roomName,pfp;
    private List<String> roomList;

    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private DatabaseReference mRoom,mRoomS;
    private ProgressDialog pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selector);

        //Progressbar
        pg = new ProgressDialog(this);

        dataBase = FirebaseDatabase.getInstance("https://tic-tac-toe-pocket-default-rtdb.europe-west1.firebasedatabase.app/");

        mAuth = FirebaseAuth.getInstance();

        // getting value of: /Users/${userId}/username ~~ value

        username = mAuth.getCurrentUser().getDisplayName();
        pfp = String.valueOf(mAuth.getCurrentUser().getPhotoUrl());
        // TextView
        activeRoomtxtview = findViewById(R.id.activeRoomtxtview);
        // Button.
        setupRoom = findViewById(R.id.setupRoom);
        logoutbtn = findViewById(R.id.logoutbtn);
        //ImageButton
        leavesetup = findViewById(R.id.leavesetup);
        // listView.
        listView = findViewById(R.id.listView);
        // Arraylist that will contain all existing rooms.
        roomList = new ArrayList<>();
        // leave multiplayer setup button.
        leavesetup.setOnClickListener(view -> {
            finish();
        });
        // logout from account button.
        logoutbtn.setOnClickListener(view -> {
            mAuth.signOut();
            finish();
        });

        // create-room button click listener.
        setupRoom.setOnClickListener(view -> {
            pg.setTitle("Creating room");
            pg.setMessage("Creating your room");
            pg.show();
            setupRoom.setEnabled(false);
            roomName = username;
            mRoom = dataBase.getReference("Rooms/"+roomName+"/players/player1"); // Adding under Rooms an object that is named player1 because we are the host.
            createRoomEventListener();
            mRoom.child("name").setValue(username); // setting player1's value to username for convenient.
            mRoom.child("pfp").setValue(pfp);
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Checking if a room is occupied, if so do not join it and alert the player.
                roomName = roomList.get(i);
                if (roomName.contains("FULL")) {
                    Toast.makeText(RoomSelector.this, "The room is full.", Toast.LENGTH_SHORT);
                } else {
                    // Avoding crash.
                    roomName.replace(" - FULL","");
                    mRoom = dataBase.getReference("Rooms/" + roomName + "/players/player2");

                    pg.setTitle("Joining room");
                    pg.setMessage("Loading the room");
                    pg.show();

                    createRoomEventListener();
                    mRoom.child("name").setValue(username); // setting player2's value to username for convenient.
                    mRoom.child("pfp").setValue(pfp);
                }
            }
        });
        createRoomsListener();
    }

    private void createRoomEventListener() {
       mRoom.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               setupRoom.setEnabled(true);
               pg.cancel();
               Intent i = new Intent(RoomSelector.this,Room.class);
               i.putExtra("roomName",roomName);
               startActivity(i);
               mRoom.removeEventListener(this);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {
               setupRoom.setEnabled(true);
               Toast.makeText(RoomSelector.this,"Error!: "+error.toString(),Toast.LENGTH_SHORT).show();
           }
       });
    }
    private void createRoomsListener() {
        mRoomS = dataBase.getReference("Rooms");
        mRoomS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear(); // clearing roomList so it won't interrupt with adding new rooms.
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for(DataSnapshot i: rooms){
                    if(i.hasChild("players/player2"))
                        roomList.add(i.getKey()+" - FULL");
                    else
                        roomList.add(i.getKey());
                }

                /* if there is active room
                enable visibility for listview and hide textview
                else vice-versa.*/

                if(roomList.size()>0){
                    activeRoomtxtview.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
                else{
                    listView.setVisibility(View.GONE);
                    activeRoomtxtview.setVisibility(View.VISIBLE);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(RoomSelector.this,
                        android.R.layout.simple_list_item_1,roomList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error
            }
        });
    }
}