package com.audioz.tictactoe;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class boardOnlineLogic {
    // Gameboard array
    private int[][] gameboard;

    // FireBase database.
    private FirebaseDatabase dataBase;
    // Firebase Database references.
    private DatabaseReference mPos,mPlaycount,mPlayer,mWinner,mPfp;
    private FirebaseStorage storage; // firebase storage for profile pictures.
    private StorageReference sRef,sAvatars;
    // ValueEventListeners.
    private ValueEventListener lPos,lPlaycount,lPlayer,lWinner;
    private Button backbtn;
    private TextView currentPlayer;
    private ImageView currentAvatar;
    private String[] playerNames= {"",""};
    private Bitmap[] playerImages= new Bitmap[2];
    private String[] playerpfp;

    // 1st element = row, 2nd element = column, 3nd element = line winning type.
    // row and column needed to know where to start drawing the line (this is only relative if the win is NOT diagonal).
    // if 3nd element equals 1 then Winning type is Horizontal, if 2 then Vertical, if 3 Negative Diagonal, if 4 Positive Diagonal.
    // Default value[{-1,-1,-1}] which also means tie.
    // I have to create a separate class for it because of firebase limitation..
    private winType winKind = new winType(-1, -1, -1);
    private double playcount = 0; // play-count so we can reduce tie checking;
    private String role,serveRole;

    boardOnlineLogic() {
        // Resetting board-array on start, even thought it's already zeros on create smh..
        this.gameboard = new int[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                this.gameboard[r][c] = 0;
            }
        }
    }

    //Sets.
    public void setPlayer(String player){
        mPlayer.setValue(player);
    }
    public boolean updateGameboard(int row, int col){
        // checking if player action is legal, by matching server-current player to client player.
        if(!serveRole.equals(role))
            return false;

        // Stop updating game board if there is a winner.
        if(!getWinner()) {
            if(this.gameboard[row-1][col-1] == 0) {
                //Updating playcount to the server.
                this.playcount +=1;
                mPlaycount.setValue(this.playcount);
                // tinkering with bullshit so it will work..
                // fuck this bitchass database may yo mama die tmr google founders..
                playerPos oPos = new playerPos(row - 1, col - 1);
                mPos.setValue(oPos); // updating user action in the server.

                return true;
            }
        }
        return false;
    }

    public void setUpGame(Button backbtn, TextView currentPlayer, ImageView currentAvatar, String[] names, String role, FirebaseDatabase inst){
        this.backbtn = backbtn;
        this.currentPlayer = currentPlayer;
        this.currentAvatar = currentAvatar;
        this.playerNames = names;
        this.role = role;

        dataBase =  inst;

        // CAN'T SAVE TWO DIMINSONAL ARRAY :(((((((((((((((
        // all this work around for this shit thing :(((
        String roomName = playerNames[0];

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://tic-tac-toe-pocket.appspot.com"); // getting Storage instance. specifying instance just in-case..
        sRef = storage.getReference(); // Storage reference to get/uplaod images.
        // last ones player's position listener.
        mPos = dataBase.getReference("Rooms/"+roomName+"/Board/pos");
        // Using play-count for avoiding checking for a winner before 5 plays from both players combined to avoid unnecessary checks.
        mPlaycount = dataBase.getReference("Rooms/"+roomName+"/Board/playcount");
        // Winner listener.
        mWinner = dataBase.getReference("Rooms/"+roomName+"/Board/winner");
        // CurrentPlayer data location/reference.
        mPlayer = dataBase.getReference("Rooms/"+roomName+"/Board/currentPlayer");

        mPfp = dataBase.getReference("Rooms/"+roomName+"/players/");

        mPfp.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                playerpfp = new String[]{dataSnapshot.child("player1/pfp").getValue(String.class),
                        dataSnapshot.child("player2/pfp").getValue(String.class)};
                final long ONE_MEGABYTE = 1024 * 1024;


                // Getting images only once since it won't change.
                // Host avatar.
                if (playerpfp[0] != null) {
                    StorageReference player1img = sRef.child(playerpfp[0]);
                    player1img.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            currentAvatar.setImageBitmap(temp);
                            playerImages[0] = temp;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors, fall back to default pfp.
                            currentAvatar.setImageResource(R.drawable.default_profile);
                            // Leave playersImages[0] as null.
                        }
                    });
                }

                if (playerpfp[1] != null) {
                    StorageReference player2img = sRef.child(playerpfp[1]);
                    player2img.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                currentAvatar.setImageBitmap(temp);
                            playerImages[1] = temp;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Do nothing, leave playersImages[1] as null.
                        }
                    });
                }
            }
        });

        // new position in board listener.
        mPos.addValueEventListener(lPos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.getValue(playerPos.class)==null)) {
                    // Made it so it will be easier to-pull last player selection from firebase.
                    playerPos newPos = dataSnapshot.getValue(playerPos.class);
                    if(serveRole.equals("host")){
                        gameboard[newPos.row][newPos.col] = 1;
                        currentPlayer.setText("(guest) "+playerNames[1] + "'s Turn");
                        if(playerImages[1]==null) // handle no pfp.
                            currentAvatar.setImageResource(R.drawable.default_profile);
                        else
                            currentAvatar.setImageBitmap(playerImages[1]);
                    }
                    else{
                        gameboard[newPos.row][newPos.col] = 2;
                        currentPlayer.setText("(host) "+playerNames[0] + "'s Turn");
                        if(playerImages[0]==null) // handle no pfp.
                            currentAvatar.setImageResource(R.drawable.default_profile);
                        else
                            currentAvatar.setImageBitmap(playerImages[0]);
                    }
                }
                // else do nothing since it runs on start..
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Do nothing ig :/
            }
        });


        mPlaycount.setValue(playcount); // setting default value in to the server (0);
        mPlaycount.addValueEventListener(lPlaycount = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playcount = snapshot.getValue(double.class); // updating play-count in client.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Do nothing ig :/
            }
        });

        // is there a winner listener.
        mWinner.addValueEventListener(lWinner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               winKind = snapshot.getValue(winType.class); // updating winkind/type in client and alerting a win both in ui and backend.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Do nothing ig :/
            }
        });
        mWinner.setValue(winKind); // setting default value in to the server (false);

        // Current player/role listener.
        mPlayer.addValueEventListener(lPlayer=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                serveRole = dataSnapshot.getValue(String.class); // updating server-role client to identify which player's turn is it. updates both in ui&backend.
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Do nothing ig :/
            }
        });
        /* Host will always start first!
         * (kinda obvious like it's his room he can do what the fuck he wants, like actually this guy is the fucking host..
         * whoever disagree with this one is an actual retard smh */

        mPlayer.setValue("host"); // setting default value in to the server ("host");
        currentPlayer.setText("(host) "+names[0] + "'s Turn");
    }

    public void removeAllListeners(){
            /*  Removing all listeners so when one user is going back which removes the room node
            the players board activity won't crash following that remove.
            stuuppiaaad
            */
        mPos.removeEventListener(lPos);
        mPlayer.removeEventListener(lPlayer);
        mPlaycount.removeEventListener(lPlaycount);
        mWinner.removeEventListener(lWinner);
        // Removing values
        mPos.removeValue();
        mPlayer.removeValue();
        mPlaycount.removeValue();
        mWinner.removeValue();
    }

    public boolean getWinner(){
        boolean winner = false; // isWinner state.
        // Avoiding checking for winner before 5 plays from both players combined to avoid unnecessary checks.
        if(this.playcount<5){return false;}

        // Horizontal Check .
        for(int r=0;r<3;r++){
            if(gameboard[r][0] != 0 &&
                    gameboard[r][0] == gameboard[r][1] && gameboard[r][0] == gameboard[r][2]){
                this.winKind = new winType(r, 0, 1);
                winner = true;
            }
        }

        // Vertical Check.
        for(int c=0;c<3;c++){
            if(gameboard[0][c] != 0 &&
                    gameboard[0][c] == gameboard[1][c] && gameboard[0][c] == gameboard[2][c]){
                this.winKind = new winType(0, c, 2);
                winner = true;
            }
        }

        // Checks diagonal from first top left cell to bottom right cell. ~~ Negative Diagonal.
        if(this.gameboard[0][0]==this.gameboard[1][1] && this.gameboard[0][0]==this.gameboard[2][2]){
            winner=true;
            this.winKind = new winType(0,0,3);
        }

        // Checks diagonal from first left bottom cell to top right cell. ~~ Positive Diagonal.
        if(this.gameboard[2][0]==this.gameboard[1][1] && this.gameboard[2][0]==this.gameboard[0][2]){
            winner=true;
            this.winKind = new winType(0,2,4);
        }

        /* if there's no winner and the play count is 9,
         * return true to avoid game input and count the game as a tie.*/
        if(!winner && this.playcount==9){
            backbtn.setVisibility(View.VISIBLE);
            currentPlayer.setText(R.string.tieGame);
            winner=true;
        }
        if(winner){
            backbtn.setVisibility(View.VISIBLE);
            if(serveRole.equals("host"))
                currentPlayer.setText("(guest) "+playerNames[1]+ " Won!!");
            else
                currentPlayer.setText("(host) "+playerNames[0]+ " Won!!");

            mWinner.setValue(this.winKind);
        }
        return winner;
    }
    //Gets.
    public int[][] getGameboard() {
        return this.gameboard;
    } // returning gameboard array.
    public String getPlayer(){
        return this.serveRole;
    } // returning getPlayer

    public int[] getWinKind() {
        return new int[]{winKind.row,winKind.col,winKind.lineType}; // returning an array that contains:
        // 1st element = row, 2nd element = column, 3nd element = line winning type.
        // row and column needed to know where to start drawing the line (this is only relative if the win is NOT diagonal).
        // if 3nd element equals 1 then Winning type is Horizontal, if 2 then Vertical, if 3 Negative Diagonal, if 4 Positive Diagonal.
        // Default value[{-1,-1,-1}] which also means tie.
    }
}

