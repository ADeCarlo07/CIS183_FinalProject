package com.example.cis183_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class Leaderboard extends AppCompatActivity
{

    Button btn_j_back;
    ListView lv_j_leaderboard;
    DatabaseHelper dbHelper;
    LeaderboardListAdapter leaderboardListAdapter;
    ArrayList<User> listOfUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        lv_j_leaderboard = findViewById(R.id.lv_v_leaderboard_leaderboardList);
        btn_j_back = findViewById(R.id.btn_v_leaderboard_back);

        fillOutUserList();
        buttonClickListener();
    }

    private void fillOutUserList()
    {
        listOfUsers = dbHelper.leaderboardRanking();

        leaderboardListAdapter = new LeaderboardListAdapter(this, listOfUsers);
        lv_j_leaderboard.setAdapter(leaderboardListAdapter);
    }


    private void buttonClickListener()
    {
        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(Leaderboard.this, HomePage.class));
            }
        });

        lv_j_leaderboard.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SessionData.cantEditOrDeleteAccount = true;
                User userSelected = listOfUsers.get(position);
                SessionData.setSelectedUser(userSelected);
                startActivity(new Intent(Leaderboard.this, Profile.class));
            }
        });
    }
}