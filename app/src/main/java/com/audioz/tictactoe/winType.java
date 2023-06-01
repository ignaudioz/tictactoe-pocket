package com.audioz.tictactoe;

public class winType {
    // made that shit public since i don't really give a fuck tbh + less writing :~}
    public int row,col,lineType;
    public winType(){}
    //pretty explanatory
    public winType(int row,int col,int lineType){
        this.row = row;
        this.col = col;
        this.lineType = lineType;
    }
}
