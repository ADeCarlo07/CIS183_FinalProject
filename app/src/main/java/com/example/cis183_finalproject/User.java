package com.example.cis183_finalproject;

public class User
{
    private String username;
    private String fname;
    private String lname;
    private String email;
    private int numPoints;

    public User()
    {

    }

    public User(String u, String f, String l, String e, int n)
    {
        username = u;
        fname = f;
        lname = l;
        email = e;
        numPoints = n;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getFname()
    {
        return fname;
    }

    public void setFname(String fname)
    {
        this.fname = fname;
    }

    public String getLname()
    {
        return lname;
    }

    public void setLname(String lname)
    {
        this.lname = lname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public int getNumPoints()
    {
        return numPoints;
    }

    public void setNumPoints(int numPoints)
    {
        this.numPoints = numPoints;
    }
}
