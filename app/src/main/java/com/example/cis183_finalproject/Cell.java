package com.example.cis183_finalproject;

import android.animation.ObjectAnimator;

public class Cell
{
    private int row;
    private int col;
    private boolean isDark;
    private Piece piece;

    public Cell(int r, int c, boolean d)
    {
        row = r;
        col = c;
        isDark = d;
        piece = null;
    }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isDark() { return isDark; }
    public Piece getPiece() { return piece; }

    public void placePiece(Piece p)
    {
        piece = p;
        piece.setCell(this);
    }




    public void removePiece()
    {
        piece = null;
    }

    public boolean containsPiece()
    {
        if (piece != null)
        {
            return true;
        }
        else
        {
            return false;
        }

    }
}
