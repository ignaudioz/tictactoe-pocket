package com.audioz.tictactoe;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class boardGameLogic {
    // Gameboard array
    private int[][] gameboard;

   private Button resetbtn,backbtn;
   private TextView currentPlayer;
   private ImageView imageturn;
   private String[] playerNames= {"",""};
    private int player = 1; // '1' for X, '2' for O.
    // 1st element = row, 2nd element = column, 3nd element = line winning type.
    // if 3nd element equals 1 then Winning type is Horizontal, if 2 then Vertical, if 3 Negative Diagonal, if 4 Positive Diagonal.
    // Default value[{-1,-1,-1}] means tie.
    private int[] WinType = new int[] {-1, -1, -1};
    private int playcount = 0;

    boardGameLogic() {
        this.gameboard = new int[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                this.gameboard[r][c] = 0;
            }
        }
    }
    //Sets.
    public void setPlayer(int player){
        this.player = player;
    }
    public boolean updateGameboard(int row, int col){
        if(this.gameboard[row-1][col-1] == 0){
            this.gameboard[row-1][col-1] = this.player;
            // if the player turn was 1, change text view to player2's name and avatar else vice-versa.
            if(this.player==1){
                currentPlayer.setText(playerNames[1] + "'s Turn");
                imageturn.setImageResource(R.drawable.circle);
            }else{
                currentPlayer.setText(playerNames[0] + "'s Turn");
                imageturn.setImageResource(R.drawable.xicon);
            }

            this.playcount++;

            return true;
        }
        return false;
    }
    public void setUpGame(Button resetbtn, Button backbtn, TextView currentPlayer, ImageView imageturn, String[] names){
        this.resetbtn = resetbtn;
        this.backbtn = backbtn;
        this.currentPlayer = currentPlayer;
        this.playerNames = names;
        this.imageturn = imageturn;
    }
    public void resetGame(){
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                this.gameboard[r][c] = 0;
            }
        }
        this.playcount=0;
        this.player = 1;
        resetbtn.setVisibility(View.GONE);
        backbtn.setVisibility(View.GONE);
        currentPlayer.setText(playerNames[this.player-1] + "'s Turn");
    }
    public boolean getWinner(){
        // Avoiding checking for winner before 5 plays from both players combined to avoid unnecessary checks.
        if(this.playcount<5){return false;}
        boolean winner = false;

        // Horizontal Check .
        for(int r=0;r<3;r++){
            if(gameboard[r][0] != 0 &&
                    gameboard[r][0] == gameboard[r][1] && gameboard[r][0] == gameboard[r][2]){
                this.WinType = new int[]{r, 0, 1};
                winner = true;
            }
        }

        // Vertical Check.
        for(int c=0;c<3;c++){
            if(gameboard[0][c] != 0 &&
                    gameboard[0][c] == gameboard[1][c] && gameboard[0][c] == gameboard[2][c]){
                this.WinType = new int[]{0, c, 2};
                winner = true;
            }
        }

        // Checks diagonal from first top left cell to bottom right cell. ~~ Negative Diagonal.
        if(this.gameboard[0][0]==this.gameboard[1][1] && this.gameboard[0][0]==this.gameboard[2][2]){
            winner=true;
            this.WinType = new int[]{0,0,3};
        }

        // Checks diagonal from first left bottom cell to top right cell. ~~ Positive Diagonal.
        if(this.gameboard[2][0]==this.gameboard[1][1] && this.gameboard[2][0]==this.gameboard[0][2]){
            winner=true;
            this.WinType = new int[]{0,2,4};
        }

        /* if there's no winner and the play count is 9,
        * return true to avoid game input and count the game as a tie.*/
        if(!winner && this.playcount==9){
            this.WinType = new int[]{-1,-1,-1};
            resetbtn.setVisibility(View.VISIBLE);
            backbtn.setVisibility(View.VISIBLE);
            currentPlayer.setText(R.string.tieGame);
            return true;
        }
        if(winner){
            resetbtn.setVisibility(View.VISIBLE);
            backbtn.setVisibility(View.VISIBLE);
            /* if player1==1 since last play is the winner play:
            then put ~other~ player name as a winner and imageturn as 'X'-avatar, else:
            vice-versa.
            */
            if(this.player==1){
                imageturn.setImageResource(R.drawable.xicon);
            }else{
                imageturn.setImageResource(R.drawable.circle);
            }
            currentPlayer.setText(playerNames[this.player-1]+ " Won!!");
//            currentPlayer.setText(playerNames[this.player-1]+ " Won!!");
//            imageturn.setImageResource(R.drawable.xicon);
            return true;
        }
        return winner;
    }
    //Gets.
    public int[][] getGameboard() {
        return this.gameboard;
    }
    public int getPlayer(){
        return this.player;
    }

    public int[] getWinType() {
        return WinType;
    }
}

