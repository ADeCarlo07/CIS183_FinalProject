package com.example.cis183_finalproject;

public class Match
{
    private String username;
    private int id;
    String result;
    private int difficultyId;
    private int time;

    public int getTime()
    {
        return time;
    }

    public void setTime(int time)
    {
        this.time = time;
    }

    public int getDifficultyId()
    {
        return difficultyId;
    }

    public void setDifficultyId(int difficultyId)
    {
        this.difficultyId = difficultyId;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
