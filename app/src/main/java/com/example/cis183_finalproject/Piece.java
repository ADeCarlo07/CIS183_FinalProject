package com.example.cis183_finalproject;

public class Piece
{
    private String color;
    private boolean isCrowned;
    private Cell cell;

    public Piece(String c)
    {
        color = c;
        isCrowned = false;
        cell = null;
    }

    public String getColor()
    {
        return color;
    }

    public boolean isCrowned()
    {
        return isCrowned;
    }

    public void makeCrowned()
    {
        isCrowned = true;
    }

    public Cell getCell()
    {
        return cell;
    }

    public void setCell(Cell c)
    {
        cell = c;
    }

}
