package com.audioz.tictactoe;

import androidx.annotation.NonNull;

public class playerPos {
    // made that shit public since i don't really give a fuck tbh + less writing :~}
    public int row,col;
    public playerPos(){}
    // Made it so it will be easier to-pull last player selection from firebase.
    public playerPos(int row,int col){
        this.row = row;
        this.col = col;
    }

}
