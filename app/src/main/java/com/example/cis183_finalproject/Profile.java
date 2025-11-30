package com.example.cis183_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class Profile extends AppCompatActivity
{
    TextView tv_j_username;
    TextView tv_j_fname;
    TextView tv_j_lname;
    TextView tv_j_email;
    TextView tv_j_points;
    TextView tv_j_avgTime;
    ListView lv_j_matches;
    Button btn_j_back;
    Button btn_j_update;
    Button btn_j_delete;
    DatabaseHelper dbHelper;

    ArrayList<Match> listOfMatches;
    MatchesListAdapter matchesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        tv_j_username = findViewById(R.id.tv_v_profile_username);
        tv_j_fname = findViewById(R.id.tv_v_profile_fname);
        tv_j_lname = findViewById(R.id.tv_v_profile_lname);
        tv_j_email = findViewById(R.id.tv_v_profile_email);
        tv_j_points = findViewById(R.id.tv_v_profile_points);
        tv_j_avgTime = findViewById(R.id.tv_v_profile_time);
        lv_j_matches = findViewById(R.id.lv_v_profile_matchHistory);
        btn_j_back = findViewById(R.id.btn_v_profile_back);
        btn_j_update = findViewById(R.id.btn_v_profile_update);
        btn_j_delete = findViewById(R.id.btn_v_profile_delete);

        fillOutMatchesList();
        fillOutUserInformation();
        buttonClickListeners();
    }

    private void fillOutMatchesList()
    {
        listOfMatches = dbHelper.getAllMatchesGivenUsername(SessionData.getSignedInUser().getUsername());

        matchesListAdapter = new MatchesListAdapter(this, listOfMatches);
        lv_j_matches.setAdapter(matchesListAdapter);
    }

    private void fillOutUserInformation()
    {
        tv_j_username.setText(SessionData.getSignedInUser().getUsername());
        tv_j_fname.setText(SessionData.getSignedInUser().getFname());
        tv_j_lname.setText(SessionData.getSignedInUser().getLname());
        tv_j_email.setText(SessionData.getSignedInUser().getEmail());
        tv_j_points.setText(String.valueOf(SessionData.getSignedInUser().getNumPoints()));

        int avgTime = dbHelper.getUsersAverageTimePerMatchGivenUsername(SessionData.getSignedInUser().getUsername());

        tv_j_avgTime.setText(avgTime + " Seconds");
    }

    private void buttonClickListeners()
    {
        btn_j_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        btn_j_update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(Profile.this, HomePage.class));
            }
        });
    }

}