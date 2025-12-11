package com.example.cis183_finalproject;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String database_name = "CheckersInfo.db";
    private static final String users_table_name = "Users";
    private static final String matches_table_name = "Matches";
    private static final String moves_table_name = "Moves";
    private static final String difficulties_table_name = "Difficulties";

    public DatabaseHelper(Context c)
    {
        //we will use this to create the database
        //it accepts: the context, the name of the database, factory (leave null), and version number
        //If your database becomes corrupt or the information in the database is wrong
        //change the version number
        //super is used to call the functionality of the base class SQLiteOpenHelper and
        //then executes the extended (DatabaseHelper)
        super(c, database_name, null, 9);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //this is where we will create the tables in our database
        //Create table in the database
        //execute the sql statement on the database that was passed to the function called db
        db.execSQL("CREATE TABLE " + users_table_name + " (username varchar(50) primary key not null, fname varchar(50), lname varchar(50), email varchar (50), numPoints int);");
        db.execSQL("CREATE TABLE " + difficulties_table_name + " (difficultyId integer primary key autoincrement not null, pointsPerWin int, difficultyName varchar(50));");
        db.execSQL("CREATE TABLE " + matches_table_name + " (username varchar(50), matchId integer primary key autoincrement not null, result varchar(50), difficultyId int, time int, foreign key (username) references " + users_table_name + " (username), foreign key (difficultyId) references " + difficulties_table_name + " (difficultyId));");
        db.execSQL("CREATE TABLE " + moves_table_name + " (moveId integer primary key autoincrement not null, matchId int, turnNumber int, toSquareRowU int, toSquareColU int, fromSquareRowU int, fromSquareColU int, toSquareRowB int, toSquareColB int, fromSquareRowB int, fromSquareColB int, foreign key (matchId) references " + matches_table_name + " (matchId))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //delete the tables in the db if they exist
        db.execSQL("DROP TABLE IF EXISTS " + users_table_name + ";");
        db.execSQL("DROP TABLE IF EXISTS " + difficulties_table_name + ";");
        db.execSQL("DROP TABLE IF EXISTS " + matches_table_name + ";");
        db.execSQL("DROP TABLE IF EXISTS " + moves_table_name + ";");


        //recreate the tables
        onCreate(db);
    }

    private void initDifficulties()
    {
        if (countRecordsFromTable(difficulties_table_name) == 0)
        {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("INSERT INTO " + difficulties_table_name + " (difficultyId, pointsPerWin, difficultyName) VALUES (1, 1, 'Easy');");
            db.execSQL("INSERT INTO " + difficulties_table_name + " (difficultyId, pointsPerWin, difficultyName) VALUES (2, 2, 'Intermediate');");


            db.close();
        }
    }


    private void initUsers()
    {
        //this will do its own error checking
        //we only want to add the data if nothing is currently in the table
        //this wil ensure we do not add the data more than once.
        if(countRecordsFromTable(users_table_name) == 0)
        {
            //get a writeable version of the database
            //we need a writeable version because we are going to write to the database
            SQLiteDatabase db = this.getWritableDatabase();

            //insert dummy data into the user table if there is nothing in the table
            //we do not want to do this more than once so it needs to be surrounded with the if
            //statement above.
            db.execSQL("INSERT INTO " + users_table_name + " (username, fname, lname, email, numPoints) VALUES ('1231233yuh', 'Allyhoop', 'Gangsta', 'baller123@yahoo.com', 13);");
            db.execSQL("INSERT INTO " + users_table_name + " (username, fname, lname, email, numPoints) VALUES ('userNAMEEEE', 'Master', 'Chief', 'UNSC117@gmail.com', 17);");
            db.execSQL("INSERT INTO " + users_table_name + " (username, fname, lname, email, numPoints) VALUES ('thisIsAUsername', 'Doug', 'Dude', 'ddude@richcompany.gov', 20);");
            db.execSQL("INSERT INTO " + users_table_name + " (username, fname, lname, email, numPoints) VALUES ('probUsername', 'Craig', 'List', 'craig.list@icloud.com', 4);");
            db.execSQL("INSERT INTO " + users_table_name + " (username, fname, lname, email, numPoints) VALUES ('psmith', 'Paul', 'Smith', 'psmith@school.edu', 6);");


            //close the database
            db.close();

        }
    }

    private void initMatches()
    {
        if(countRecordsFromTable(matches_table_name) == 0)
        {
            //get a writeable version of the database
            //we need a writeable version because we are going to write to the database
            SQLiteDatabase db = this.getWritableDatabase();

            //insert dummy data into the user table if there is nothing in the table
            //we do not want to do this more than once so it needs to be surrounded with the if
            //statement above.
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('1231233yuh', 1, 'Won', 1, 130);");
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('1231233yuh', 2, 'Lost', 2, 32);");
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('userNAMEEEE', 3, 'Lost', 2, 250);");
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('thisIsAUsername', 4, 'Won', 1, 312);");
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('probUsername', 5, 'Won', 1, 231);");
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('probUsername', 6, 'Lost', 1, 111);");
            db.execSQL("INSERT INTO " + matches_table_name + " (username, matchId, result, difficultyId, time) VALUES ('psmith', 7, 'Won', 2, 121);");


            //close the database
            db.close();

        }
        //db.execSQL("CREATE TABLE " + matches_table_name + " (username varchar(50), matchId primary key autoincrement not null, result varchar(50), difficultyId int, time int, foreign key (username) references " + users_table_name + " (username), foreign key (difficultyId) references " + difficulties_table_name + " (difficultyId));");
    }

    private void initMoves()
    {
        if(countRecordsFromTable(moves_table_name) == 0)
        {
            SQLiteDatabase db = this.getWritableDatabase();


            //Match 1
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (1, 1, 2, 3, 1, 2, 5, 4, 6, 5);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (1, 2, 3, 4, 2, 3, 4, 5, 5, 6);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (1, 3, 4, 5, 3, 4, 3, 6, 4, 7);");

            //Match 2
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (2, 1, 2, 3, 1, 2, 5, 4, 6, 5);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (2, 2, 3, 4, 2, 3, 4, 3, 5, 4);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (2, 3, 4, 3, 3, 2, 2, 5, 3, 6);");

            //Match 3
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (3, 1, 5, 1, 4, 0, 1, 6, 0, 7);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (3, 2, 6, 2, 5, 1, 2, 5, 1, 6);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (3, 3, 7, 3, 6, 2, 3, 4, 2, 5);");

            //Match 4
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (4, 1, 3, 4, 2, 3, 4, 5, 5, 6);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (4, 2, 4, 5, 3, 4, 3, 6, 4, 7);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (4, 3, 5, 6, 4, 5, 2, 7, 3, 6);");

            //Match 5
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (5, 1, 1, 2, 0, 1, 6, 5, 7, 6);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (5, 2, 2, 3, 1, 2, 5, 4, 6, 5);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (5, 3, 3, 4, 2, 3, 4, 3, 5, 4);");

            //Match 6
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (6, 1, 4, 1, 3, 0, 2, 6, 1, 7);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (6, 2, 5, 2, 4, 1, 1, 5, 0, 6);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (6, 3, 6, 3, 5, 2, 0, 4, 1, 5);");

            //Match 7
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (7, 1, 7, 1, 6, 0, 1, 7, 0, 6);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (7, 2, 6, 2, 7, 1, 2, 6, 1, 5);");
            db.execSQL("INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES (7, 3, 5, 3, 6, 2, 3, 5, 2, 4);");

            db.close();

        }
    }



    public void initAllTables()
    {

        initUsers();
        initDifficulties();
        initMatches();
        initMoves();

    }

    public int countRecordsFromTable(String tableName)
    {
        //get an instance of the a readable database
        //we only need readable because we are not adding anything to the database with this action
        SQLiteDatabase db = this.getReadableDatabase();

        //count the number of entries in the table that was passed to the function
        //this is a built-in function
        int numRows = (int) DatabaseUtils.queryNumEntries(db, tableName);

        //whenever you open the database you need to close it
        db.close();

        return numRows;
    }

    public boolean isUsernameValid(String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String checkUsername = "SELECT count(username) FROM " + users_table_name + " WHERE username = '" + username + "';";

        Cursor cursor = db.rawQuery(checkUsername, null);

        cursor.moveToFirst();

        int count = cursor.getInt(0);

        cursor.close();
        db.close();

        if (count != 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void addUserToDatabase(User u)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        String addUser = "INSERT INTO " + users_table_name + " (username, fname, lname, email, numPoints) VALUES ('" + u.getUsername() + "', '" + u.getFname() + "', '" + u.getLname() + "', '" + u.getEmail() + "', " + u.getNumPoints() + ");";

        db.execSQL(addUser);

        db.close();
    }

    public User getAllUserDataGivenUsername(String username)
    {

        User selectedUser = new User();
        boolean isUserExisting;

        SQLiteDatabase db = this.getReadableDatabase();

        String checkUsername = "SELECT count(username) FROM " + users_table_name + " WHERE username = '" + username + "';";

        Cursor cursor = db.rawQuery(checkUsername, null);

        cursor.moveToFirst();

        int count = cursor.getInt(0);


        if (count != 0)
        {
            isUserExisting = true;
        }
        else
        {
            isUserExisting = false;
        }

        cursor = null;

        String getInfo = "SELECT * FROM " + users_table_name + " WHERE username = '" + username + "';";

        if (isUserExisting)
        {
            cursor = db.rawQuery(getInfo, null);

            if (cursor != null)
            {

                cursor.moveToFirst();

                selectedUser.setUsername(cursor.getString(0));
                selectedUser.setFname(cursor.getString(1));
                selectedUser.setLname(cursor.getString(2));
                selectedUser.setEmail(cursor.getString(3));
                selectedUser.setNumPoints(cursor.getInt(4));




                if (SessionData.cantEditOrDeleteAccount)
                {
                    SessionData.setSelectedUser(selectedUser);
                }
                else
                {
                    SessionData.setSignedInUser(selectedUser);
                }
            }
            else
            {

                SessionData.setSignedInUser(null);
            }

            if (cursor != null)
            {
                cursor.close();
            }
            db.close();


            return selectedUser;
        }

        return null;
    }

    public ArrayList<User> leaderboardRanking()
    {
        calculateMatchPoints();
        ArrayList<User> listOfUsers = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String getList = "SELECT username, numPoints FROM " + users_table_name + " ORDER BY numPoints DESC;";

        Cursor cursor = db.rawQuery(getList, null);

        if (cursor.moveToFirst())
        {
            //we need a loop for this because we do not know how many students there is
            do
            {
                //returned to us by the query
                String username = cursor.getString(0);
                int numPoints = cursor.getInt(1);

                User user = new User();
                user.setUsername(username);
                user.setNumPoints(numPoints);

                listOfUsers.add(user);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listOfUsers;

    }

    public String getDifficultyNameGivenId(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String difficultyName = "";

        String getName = "SELECT difficultyName FROM " + difficulties_table_name + " WHERE difficultyId = " + id + ";";

        Cursor cursor = db.rawQuery(getName, null);

        if (cursor != null)
        {
            cursor.moveToFirst();
            difficultyName = cursor.getString(0);
        }

        if (cursor != null)
        {
            cursor.close();
        }
        db.close();

        return difficultyName;
    }

    public int getUsersAverageTimePerMatchGivenUsername(String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int averageTime = 0;

        String getAverageTime = "SELECT AVG(time) AS time FROM " + matches_table_name + " WHERE username = '" + username + "';";

        Cursor cursor = db.rawQuery(getAverageTime, null);

        if (cursor != null)
        {
            cursor.moveToFirst();
            averageTime = cursor.getInt(0);
        }

        if (cursor != null)
        {
            cursor.close();
        }
        db.close();

        return averageTime;
    }

    public ArrayList<Match> getAllMatchesGivenUsername(String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Match> listOfMatches = new ArrayList<>();

        String selectStatement = "SELECT result, difficultyId, matchId FROM " + matches_table_name + " WHERE username = '" + username + "';";

        Cursor cursor = db.rawQuery(selectStatement, null);

        if (cursor.moveToFirst())
        {
            //we need a loop for this because we do not know how many students there is
            do
            {
                //returned to us by the query
                String result = cursor.getString(0);
                int difficultyId = cursor.getInt(1);
                int matchId = cursor.getInt(2);

                Match match = new Match();
                match.setResult(result);
                match.setDifficultyId(difficultyId);
                match.setId(matchId);

                listOfMatches.add(match);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listOfMatches;
    }

    public void setAllMatchDataGivenId(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectStatement = "SELECT username, matchId, result, difficultyId, time FROM " + matches_table_name + " WHERE matchId = " + id + ";";

        Cursor cursor = db.rawQuery(selectStatement, null);

        if (cursor.moveToFirst())
        {
            //returned to us by the query
            String username = cursor.getString(0);
            int matchId = cursor.getInt(1);
            String result = cursor.getString(2);
            int difficultyId = cursor.getInt(3);
            int time = cursor.getInt(4);

            Match match = new Match();
            match.setUsername(username);
            match.setId(matchId);
            match.setResult(result);
            match.setDifficultyId(difficultyId);
            match.setTime(time);

            SessionData.setSelectedMatch(match);
        }

        cursor.close();
        db.close();
    }

    public ArrayList<Move> getAllMovesGivenMatchId(int matchId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Move> listOfMoves = new ArrayList<>();

        String selectStatement = "SELECT turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB, moveId FROM " + moves_table_name + " WHERE matchId = " + matchId + ";";

        Cursor cursor = db.rawQuery(selectStatement, null);

        if (cursor.moveToFirst())
        {
            //we need a loop for this because we do not know how many users there is
            do
            {
                //returned to us by the query
                int turnNumber = cursor.getInt(0);
                int toSquareRowU = cursor.getInt(1);
                int toSquareColU = cursor.getInt(2);
                int fromSquareRowU = cursor.getInt(3);
                int fromSquareColU = cursor.getInt(4);
                int toSquareRowB = cursor.getInt(5);
                int toSquareColB = cursor.getInt(6);
                int fromSquareRowB = cursor.getInt(7);
                int fromSquareColB = cursor.getInt(8);
                int moveId = cursor.getInt(9);

                Move move = new Move();
                move.setTurnNumber(turnNumber);
                move.setToSquareRowU(toSquareRowU);
                move.setToSquareColU(toSquareColU);
                move.setFromSquareRowU(fromSquareRowU);
                move.setFromSquareColU(fromSquareColU);
                move.setToSquareRowB(toSquareRowB);
                move.setToSquareColB(toSquareColB);
                move.setFromSquareRowB(fromSquareRowB);
                move.setFromSquareColB(fromSquareColB);
                move.setMoveId(moveId);

                listOfMoves.add(move);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listOfMoves;
    }

    public void updateUserData(User user)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String updateStatement = "UPDATE " + users_table_name + " SET fname = '" + user.getFname() + "', lname = '" + user.getLname() + "', email = '" + user.getEmail() +  "' WHERE username = '" + user.getUsername() +"';";
        db.execSQL(updateStatement);

        db.close();
    }

    public void deleteUserGivenUsername(String username)
    {
        boolean usernameExists = isUsernameValid(username);

        if (usernameExists)
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String deleteStatement = "DELETE FROM " + users_table_name + " WHERE username = '" + username + "';";
            db.execSQL(deleteStatement);
            db.close();

            ArrayList<Match> matches = new ArrayList<>();
            matches = getAllMatchesGivenUsername(username);

            for (Match m : matches)
            {
                SQLiteDatabase db02 = this.getWritableDatabase();
                int id = m.getId();
                String moveDeleteStatement = "DELETE FROM " + moves_table_name + " WHERE matchId =" + id + ";";
                db02.execSQL(moveDeleteStatement);
                db02.close();

                SQLiteDatabase db01 = this.getWritableDatabase();
                String matchDeleteStatement = "DELETE FROM " + matches_table_name + " WHERE username ='" + username + "';";
                db01.execSQL(matchDeleteStatement);
                db01.close();

            }


        }

    }

    public void addNewMatchToDBGivenUsername(String username, Match match, ArrayList<Move> moves)
    {
        boolean usernameExists = isUsernameValid(username);

        SQLiteDatabase db = this.getWritableDatabase();
        String createStatement = "INSERT INTO " + matches_table_name + " (username, result, difficultyId, time) VALUES('" + username + "', '" + match.getResult() + "', " + match.getDifficultyId() + ", " + match.getTime() + ");";

        db.execSQL(createStatement);

        //get last inserted row id
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);

        cursor.moveToFirst();

        int curMatchId = cursor.getInt(0);
        cursor.close();

        for(Move move : moves)
        {
            String createMoves = "INSERT INTO " + moves_table_name + " (matchId, turnNumber, toSquareRowU, toSquareColU, fromSquareRowU, fromSquareColU, toSquareRowB, toSquareColB, fromSquareRowB, fromSquareColB) VALUES(" + curMatchId + ", " + move.getTurnNumber() + ", " + move.getToSquareRowU() + ", " + move.getToSquareColU() + ", " + move.getFromSquareRowU() + ", " + move.getFromSquareColU() + ", " + move.getToSquareRowB() + ", " + move.getToSquareColB() + ", " + move.getFromSquareRowB() + ", " + move.getFromSquareColB() + ");";
            db.execSQL(createMoves);
        }

        calculateMatchPoints();

        db.close();


    }

    public void calculateMatchPoints()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase dbWriteable = this.getWritableDatabase();
        ArrayList<User> listOfUsers = new ArrayList<>();
        ArrayList<Match> listOfMatches = new ArrayList<>();

        int totalPoints = 0;

        String selectAllUsers = "SELECT username FROM " + users_table_name;

        Cursor cursor = db.rawQuery(selectAllUsers, null);

        if (cursor.moveToFirst())
        {
            //we need a loop for this because we do not know how many users there is
            do
            {
                //returned to us by the query
                String username = cursor.getString(0);

                User user = new User();
                user.setUsername(username);

                listOfUsers.add(user);
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        for (User user : listOfUsers)
        {
            String getAllDifficultyIdFromMatches = "SELECT difficultyId, result FROM " + matches_table_name + " WHERE username = '" + user.getUsername() + "';";

            Cursor otherCursor = db.rawQuery(getAllDifficultyIdFromMatches, null);

            if (otherCursor.moveToFirst())
            {
                //we need a loop for this because we do not know how many users there is
                do
                {
                    //returned to us by the query
                    int difficultyId = otherCursor.getInt(0);
                    String result = otherCursor.getString(1);

                    Match match = new Match();

                    match.setDifficultyId(difficultyId);
                    match.setResult(result);

                    listOfMatches.add(match);
                }
                while (otherCursor.moveToNext());
            }
            otherCursor.close();

            for (Match match : listOfMatches)
            {
                String getPointsPerMatch = "SELECT pointsPerWin FROM " + difficulties_table_name + " WHERE difficultyId = " + match.getDifficultyId() + ";";

                Cursor anotherCursor = db.rawQuery(getPointsPerMatch, null);

                if (anotherCursor.moveToFirst())
                {
                    int pointPerWin = anotherCursor.getInt(0);

                    if (match.getResult().equals("Won"))
                    {
                        totalPoints += pointPerWin;
                    }

                }
                anotherCursor.close();
            }


            if (user.getUsername().equals(SessionData.getSignedInUser().getUsername()))
            {
                SessionData.getSignedInUser().setNumPoints(totalPoints);
            }
            String updateNumberOfPoints = "UPDATE " + users_table_name + " SET numPoints = " + totalPoints + " WHERE username = '" + user.getUsername() + "';";
            dbWriteable.execSQL(updateNumberOfPoints);


            listOfMatches = new ArrayList<>();
            totalPoints = 0;

        }

        dbWriteable.close();
        db.close();
    }



}