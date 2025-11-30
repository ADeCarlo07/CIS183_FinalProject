package com.example.cis183_finalproject;

public class Move
{
    private int moveId;
    private int matchId;
    private int turnNumber;
    private int toSquareRowU;
    private int toSquareColU;
    private int fromSquareRowU;
    private int fromSquareColU;
    private int toSquareRowB;
    private int toSquareColB;
    private int fromSquareRowB;
    private int fromSquareColB;

    public int getMoveId()
    {
        return moveId;
    }

    public void setMoveId(int moveId)
    {
        this.moveId = moveId;
    }

    public int getMatchId()
    {
        return matchId;
    }

    public void setMatchId(int matchId)
    {
        this.matchId = matchId;
    }

    public int getTurnNumber()
    {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber)
    {
        this.turnNumber = turnNumber;
    }

    public int getToSquareRowU()
    {
        return toSquareRowU;
    }

    public void setToSquareRowU(int toSquareRowU)
    {
        this.toSquareRowU = toSquareRowU;
    }

    public int getToSquareColU()
    {
        return toSquareColU;
    }

    public void setToSquareColU(int toSquareColU)
    {
        this.toSquareColU = toSquareColU;
    }

    public int getFromSquareRowU()
    {
        return fromSquareRowU;
    }

    public void setFromSquareRowU(int fromSquareRowU)
    {
        this.fromSquareRowU = fromSquareRowU;
    }

    public int getFromSquareColU()
    {
        return fromSquareColU;
    }

    public void setFromSquareColU(int fromSquareColU)
    {
        this.fromSquareColU = fromSquareColU;
    }

    public int getToSquareRowB()
    {
        return toSquareRowB;
    }

    public void setToSquareRowB(int toSquareRowB)
    {
        this.toSquareRowB = toSquareRowB;
    }

    public int getToSquareColB()
    {
        return toSquareColB;
    }

    public void setToSquareColB(int toSquareColB)
    {
        this.toSquareColB = toSquareColB;
    }

    public int getFromSquareRowB()
    {
        return fromSquareRowB;
    }

    public void setFromSquareRowB(int fromSquareRowB)
    {
        this.fromSquareRowB = fromSquareRowB;
    }

    public int getFromSquareColB()
    {
        return fromSquareColB;
    }

    public void setFromSquareColB(int fromSquareColB)
    {
        this.fromSquareColB = fromSquareColB;
    }
}
