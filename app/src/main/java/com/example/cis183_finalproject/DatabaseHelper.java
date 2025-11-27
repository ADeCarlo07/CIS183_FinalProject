package com.example.cis183_finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String database_name = "CheckersInfo.db";
    private static final String users_table_name = "Users";
    private static final String matches_table_name = "Matches";
    private static final String moves_table_name = "Moves";
    private static final String difficulties_table_name = "Difficulties";

    public DatabaseHelper(Context c) {
        //we will use this to create the database
        //it accepts: the context, the name of the database, factory (leave null), and version number
        //If your database becomes corrupt or the information in the database is wrong
        //change the version number
        //super is used to call the functionality of the base class SQLiteOpenHelper and
        //then executes the extended (DatabaseHelper)
        super(c, database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //this is where we will create the tables in our database
        //Create table in the database
        //execute the sql statement on the database that was passed to the function called db
        db.execSQL("CREATE TABLE " + users_table_name + " (username varchar(50) primary key not null, fname varchar(50), lname varchar(50), email varchar (50), numPoints int);");
        db.execSQL("CREATE TABLE " + difficulties_table_name + " (difficultyId primary key autoincrement not null, pointsPerWin int, difficultyName varcar(50));");
        db.execSQL("CREATE TABLE " + matches_table_name + " (username varchar(50), matchId primary key autoincrement not null, result varchar(50), difficultyId int, time int, foreign key (username) references " + users_table_name + " (username), foreign key (difficultyId) references " + difficulties_table_name + " (difficultyId));");
        db.execSQL("CREATE TABLE " + moves_table_name + " (moveId primary key autoincrement not null, matchId int, turnNumber int, toSquareRowU int, toSquareColU int, fromSquareRowU int, fromSquareColU int, toSquareRowB int, toSquareColB int, fromSquareRowB int, fromSquareColB int, foreign key (matchId) references " + matches_table_name + " (matchId))");

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

}