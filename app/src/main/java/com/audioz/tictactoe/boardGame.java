package com.audioz.tictactoe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;



public class boardGame extends View{
    // Color attributes
    private final int boardColor,Xcolor,Ocolor, winnerColor;
    // Paint
    private final Paint paint = new Paint();
    // SingleGameLogic class
    private final boardGameLogic game;
    private boolean winnerCheck = false;
    // Setting the clickable box size; default value equals width/3.
    private int cellSize = getWidth()/3;
    //Stroke width for board's lines.
    private final int strokeWidth=16;

    public boardGame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Creating logic class / adding game logic to the board.
        game = new boardGameLogic();

        // Creating an array that contains all style attributes related to the game.
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.boardGame,0,0);

        try {
            boardColor = a.getInteger(R.styleable.boardGame_boardColor,0);
            Xcolor = a.getInteger(R.styleable.boardGame_Xcolor,0);
            Ocolor = a.getInteger(R.styleable.boardGame_Ocolor,0);
            winnerColor = a.getInteger(R.styleable.boardGame_winnerColor,0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int width,int height){
        super.onMeasure(width,height);

        int dimension = Math.min(getMeasuredWidth(),getMeasuredHeight());
        cellSize = dimension/3; // gap between each column

        /* set board-size for widthXwidth e.g: 80x80. */
        setMeasuredDimension(dimension,dimension);

    }

    @Override
    // Default draw on spawn/creation.
    protected void onDraw(Canvas canvas){
        paint.setStyle(Paint.Style.STROKE); // setting painting style.
        paint.setAntiAlias(true); // disable smoothing lines.

        drawGameBroad(canvas);
        drawMarkers(canvas);

        if(winnerCheck){
            paint.setColor(winnerColor); // Setting winner line color.
            drawWinningLine(canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event){

        // Checks if there is an onTouchEvent on action down (Finger tapped).
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            // Getting 'X' and 'Y' values of the event in the board.
            float x = event.getX();
            float y = event.getY();
            /* Calculation column and row position by dividing the positions
            * of 'X' and 'Y' by the cellsize(clickable box size) then rounding the number up.*/
            int col = (int) Math.ceil(x/cellSize);
            int row = (int) Math.ceil(y/cellSize);

            // Stop updating game board if there is a winner.
            if(!winnerCheck) {
                // if position is free, update the array with the user selection.
                if (game.updateGameboard(row, col)) {
                    // Checks if there is a winner after a players turn.
                    winnerCheck = game.getWinner();
                    /*-- Updating the players turn. --*/
                    //if previous player was 'X' a/k/a '1' switch the turn to 'O' player a/k/a '2'.
                    if (game.getPlayer() == 1) {
                        game.setPlayer(game.getPlayer() + 1);
                    }
                    //if previous player was 'O' a/k/a '2' switch the turn to 'X' player a/k/a '1'.
                    else {
                        game.setPlayer(game.getPlayer() - 1);
                    }
                }

                // Re-drawing the board according to the game's logic array.
                invalidate();
            }

            /* returns true if there is a touch event
            in the gameboard. */
            return true;
        }

        /* returns false if there isn't a touch event
        in the gameboard. */
        return false;
    }

    private void drawGameBroad(Canvas canvas) {
        paint.setColor(boardColor); // paint color for line's color.
        paint.setStrokeWidth(strokeWidth); // paint color for line's color.
        /* 'C' for column.
        Drawing line at X:(1/3 of Screen-width multiplied by the column number) Y:(Start of the canvas);
        to X:(Same position) Y:(Bottom of canvas {canvas width or height}).
        */
        for(int c=1;c<3;c++) {
            int XstartEnd=cellSize*c;
            canvas.drawLine(XstartEnd,
                    0,
                    XstartEnd,
                    canvas.getWidth(),
                    paint);
        }
        /* 'R' for rows.
        Drawing line at X:(Start of canvas) Y:(1/3 of Screen-width multiplied by the column number);
        to X:(End of canvas {canvas width or height}) Y:(Same position).
        */
        for(int r=1;r<3;r++) {
            canvas.drawLine(0,
                    cellSize*r,
                    canvas.getWidth(),
                    cellSize*r,
                    paint);
        }
    }


    private void drawMarkers(Canvas canvas){
        /* 'r' for row
        * 'c' for column */
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                /* '1' in the array equals 'X'
                * '2' in the array equals 'O'*/
                if(game.getGameboard()[r][c] == 1){
                    drawX(canvas,r,c);
                }
                if(game.getGameboard()[r][c] == 2){
                    drawO(canvas,r,c);
                }
            }
        }
    }

    public void setUpGame(Button resetbtn, Button backbtn, TextView currentPlayer, ImageView imageturn, String[] playerNames){
        game.setUpGame(resetbtn, backbtn, currentPlayer, imageturn, playerNames);
    }
    public void resetGame(){ // Reset game. (public so foreign view can access it.)
        game.resetGame();
        winnerCheck = false;
    }

    private void drawX(Canvas canvas, int row, int col){ // Draws 'X'
        paint.setColor(Xcolor); // setting X player color.

        /* a value for fixing the diagonal lines positions by adding and subtracting
        * to the starting points and end points.*/
        float fixpos = (float) (cellSize * 0.2);

        // This function draws diagonal line a/k/a negative diagonal using StartX,EndX,StatY and EndY positions that are given.
        canvas.drawLine(
                (float) (col*cellSize + fixpos), // Starts 'X' at the start of the selected cell added with "fixpos" to make the 'X' fit right (horizontally wise).
                (float) (row*cellSize + fixpos), // Starts 'Y' at the selected cell added with "fixpos" to make the 'Y' fit right (vertically wise).
                (float) ((col+1)*cellSize - fixpos), // Ends 'X' at the right start of the next cell and fixing its position by subtracting "fixpos" from it ( to fix it horizontally wise).
                (float) ((row+1)*cellSize - fixpos), // Ends 'Y' at the top start of the next cell(the under-cell) and fixing its position by subtracting "fixpos" from it ( to fix it vertically wise).
                paint); // using our selected paint.

        // This function draws the opposite diagonal line a/k/a positive diagonal using StartX,EndX,StatY and EndY positions that are given.
        canvas.drawLine(
                (float) ((col+1)*cellSize - fixpos),// Starts 'X' at the right start of the next cell and fixing its position by subtracting "fixpos" from it ( to fix it horizontally wise).
                (float) (row*cellSize + fixpos), // Starts 'Y' at the selected cell added with "fixpos" to make the 'Y' fit right (vertically wise).
                (float) (col*cellSize + fixpos), // Ends 'X' at the start of the selected cell added with "fixpos" to make the 'X' fit right (horizontally wise).
                (float) ((row+1)*cellSize - fixpos),// Ends 'Y' at the top start of the next cell(the under-cell) and fixing its position by subtracting "fixpos" from it ( to fix it vertically wise).
                paint);// using our selected paint.
    }

    private void drawO(Canvas canvas, int row, int col){ // Draws 'O'
        paint.setColor(Ocolor); // setting O player color.

        /* a value for fixing the Oval a/k/a 'O' positions by adding and subtracting
         * to the starting points and end points.*/
        float fixpos = (float) (cellSize * 0.2);

        // This function draws an Oval a/k/a the 'O' using Left,Right,Top and Bottom positions that are given.
        canvas.drawOval(
                (float) (col*cellSize + fixpos),// Starts 'Left' position at the start of the selected cell added with "fixpos" to make the 'O' fit right (horizontally wise).
                (float) (row*cellSize + fixpos),// Starts 'Top' position at the selected cell added with "fixpos" to make the 'O' fit right (vertically wise).
                (float) ((col+1)*cellSize - fixpos),// Ends 'Right' position at the right start of the next cell and fixing its position by subtracting "fixpos" from it ( to fix it horizontally wise).
                (float) ((row+1)*cellSize - fixpos),// Ends 'Bottom' position at the top start of the next cell(the under-cell) and fixing its position by subtracting "fixpos" from it ( to fix it vertically wise).
                paint);// using our selected paint.
    }

    private void drawWinningLine(Canvas canvas){
        int row= game.getWinType()[0];
        int col= game.getWinType()[1];

        switch(game.getWinType()[2]){
            case 1:
                drawHorizontalWin(canvas,row,col);
                break;
            case 2:
                drawVerticalWin(canvas,row,col);
                break;
            case 3:
                drawDiagonalNegWin(canvas);
                break;
            case 4:
                drawDiagonalPosWin(canvas);
                break;
            default:

        }
    }

    private void drawHorizontalWin(Canvas canvas,int row, int col){ // Draws Horizontal Winning line.

        /* a value for fixing the "winning line" positions by adding
         * to the 'Y' starting points and end points.*/
        float fixpos = (float) (cellSize/2);

        canvas.drawLine(
                (col), // Starts 'X' pos at column place/start of the cell.

                /* Starts 'Y' pos at row place plus fixpos value to
                avoid drawing on the black line.( drawing the line at the middle of the cell instead of the start(blacklines)*/
                (float) (row*cellSize + fixpos),
                (float) (cellSize*3), // Ends 'X' at the end of the board (horizontally wise) aka till 3 cells.
                (float) (row*cellSize + fixpos), // Ends 'Y' sames as start 'Y', since we want a straight line.
                paint); // using our selected paint // called in onDraw function.
    }

    private void drawVerticalWin(Canvas canvas,int row, int col){ // Draws Vertical Winning line.
        /* a value for fixing the "winning line" positions by adding
         * to the 'X' starting points and end points.*/
        float fixpos = (float) (cellSize/2);

        canvas.drawLine(
                /* Starts 'X' pos at column place plus fixpos value to
                avoid drawing on the black line.( drawing the line at the middle of the cell instead of the start(blacklines)*/
                (float) (col*cellSize + fixpos),
                (row), // Starts 'Y' pos at row place/Drawing at the top of the cell
                (float) (col*cellSize + fixpos), // Ends 'X' same as start 'X', since we want a straight line.
                (float) (cellSize*3), // Ends 'Y' at the end of the board (vertically wise) aka till 3 cells.
                paint); // using our selected paint // called in onDraw function.
    }

    private void drawDiagonalNegWin(Canvas canvas){ //Draws Negative-Diagonal winning line.

        canvas.drawLine(
                0, // Starts 'X' at the start of the canvas/board.
                0, // Starts 'Y' at the start/top of the canvas/board.
                (float) cellSize*3, // Ends 'X' at the end of the canvas/board.
                (float) cellSize*3, // Ends 'Y' at the bottom of the canvas/board.
                paint); // using our selected paint // called in onDraw function.
    }

    private void drawDiagonalPosWin(Canvas canvas){ // Draws Positive-Diagonal winning line.

        canvas.drawLine(
                (float) cellSize*3, // Starts 'X' at the end of the canvas/board.
                0, // Starts 'Y' at the top/start of the canvas/board.
                0, // Stops 'X' at the start of the canvas/board.
                (float) cellSize*3, // Stops 'Y' at the end of the canvas/board.
                paint); // using our selected paint // called in onDraw function.
    }
}
