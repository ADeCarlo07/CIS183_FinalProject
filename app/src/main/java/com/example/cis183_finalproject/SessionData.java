package com.example.cis183_finalproject;

public class SessionData
{
    private static User signedInUser;
    private static Match selectedMatch;

    public static User getSignedInUser()
    {
        return signedInUser;
    }

    public static void setSignedInUser(User signedInUser)
    {
        SessionData.signedInUser = signedInUser;
    }

    public static Match getSelectedMatch()
    {
        return selectedMatch;
    }

    public static void setSelectedMatch(Match match)
    {
        SessionData.selectedMatch = match;
    }


}
