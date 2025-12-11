package com.example.cis183_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

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

    ConstraintLayout cons_j_alert;
    Button btn_j_yes;
    Button btn_j_no;

    ArrayList<Match> listOfMatches;
    MatchesListAdapter matchesListAdapter;

    boolean deleteAlertActive = false;

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
        dbHelper.calculateMatchPoints();

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
        btn_j_no = findViewById(R.id.btn_v_profile_no);
        btn_j_yes = findViewById(R.id.btn_v_profile_yes);
        cons_j_alert = findViewById(R.id.cons_v_profile_alert);



        fillOutMatchesList();
        fillOutUserInformation();
        buttonClickListeners();

        if (SessionData.cantEditOrDeleteAccount)
        {
            Log.d("SIGNED IN USER: ", SessionData.getSignedInUser().getUsername());
            if (!SessionData.getSelectedUser().getUsername().equals(SessionData.getSignedInUser().getUsername()))
            {
                btn_j_delete.setVisibility(View.INVISIBLE);
                btn_j_update.setVisibility(View.INVISIBLE);
            }

        }
    }

    private void fillOutMatchesList() {
        if (!SessionData.cantEditOrDeleteAccount)
        {
            if (SessionData.getSignedInUser() != null)
            {
                listOfMatches = dbHelper.getAllMatchesGivenUsername(SessionData.getSignedInUser().getUsername());
                matchesListAdapter = new MatchesListAdapter(this, listOfMatches);
                lv_j_matches.setAdapter(matchesListAdapter);
            }

        }
        else
        {
            if (SessionData.getSelectedUser() != null)
            {
                listOfMatches = dbHelper.getAllMatchesGivenUsername(SessionData.getSelectedUser().getUsername());
                matchesListAdapter = new MatchesListAdapter(this, listOfMatches);
                lv_j_matches.setAdapter(matchesListAdapter);
            }

        }

    }

    private void fillOutUserInformation()
    {
        if (!SessionData.cantEditOrDeleteAccount)
        {
            if (SessionData.getSignedInUser() != null)
            {
                tv_j_username.setText(SessionData.getSignedInUser().getUsername());
                tv_j_fname.setText(SessionData.getSignedInUser().getFname());
                tv_j_lname.setText(SessionData.getSignedInUser().getLname());
                tv_j_email.setText(SessionData.getSignedInUser().getEmail());
                tv_j_points.setText(String.valueOf(SessionData.getSignedInUser().getNumPoints()));

                int avgTime = dbHelper.getUsersAverageTimePerMatchGivenUsername(SessionData.getSignedInUser().getUsername());

                tv_j_avgTime.setText(avgTime + " Seconds");
            }

        }
        else
        {

            if (SessionData.getSelectedUser() != null)
            {
                dbHelper.getAllUserDataGivenUsername(SessionData.getSelectedUser().getUsername());

                tv_j_username.setText(SessionData.getSelectedUser().getUsername());
                tv_j_fname.setText(SessionData.getSelectedUser().getFname());
                tv_j_lname.setText(SessionData.getSelectedUser().getLname());
                tv_j_email.setText(SessionData.getSelectedUser().getEmail());
                tv_j_points.setText(String.valueOf(SessionData.getSelectedUser().getNumPoints()));

                int avgTime = dbHelper.getUsersAverageTimePerMatchGivenUsername(SessionData.getSelectedUser().getUsername());

                tv_j_avgTime.setText(avgTime + " Seconds");
            }

        }

    }

    private void buttonClickListeners()
    {
        btn_j_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cons_j_alert.setVisibility(View.VISIBLE);
                deleteAlertActive = true;
            }
        });

        btn_j_no.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteAlertActive = false;
                cons_j_alert.setVisibility(View.INVISIBLE);
            }
        });

        btn_j_yes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteAlertActive = false;
                cons_j_alert.setVisibility(View.INVISIBLE);

                if (SessionData.getSignedInUser() != null)
                {
                    dbHelper.deleteUserGivenUsername(SessionData.getSignedInUser().getUsername());
                    startActivity(new Intent(Profile.this, MainActivity.class));
                }

            }
        });

        btn_j_update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!deleteAlertActive)
                {
                    startActivity(new Intent(Profile.this, UpdateUser.class));
                }

            }
        });

        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!deleteAlertActive)
                {
                    if (SessionData.cantEditOrDeleteAccount)
                    {
                        startActivity(new Intent(Profile.this, Leaderboard.class));
                        SessionData.cantEditOrDeleteAccount = false;
                    }
                    else
                    {
                        startActivity(new Intent(Profile.this, HomePage.class));
                        SessionData.cantEditOrDeleteAccount = false;
                    }
                }


            }
        });

        lv_j_matches.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!deleteAlertActive)
                {
                    Match matchSelected = listOfMatches.get(position);
                    SessionData.setSelectedMatch(matchSelected);
                    Log.d("SessionData ", " Current matchId " + SessionData.getSelectedMatch().getId());
                    startActivity(new Intent(Profile.this, MatchInfo.class));
                }


            }
        });
    }

}