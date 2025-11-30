package com.example.cis183_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity
{
    TextView tv_j_welcome;
    Button btn_j_easyMode;
    Button btn_j_intermediateMode;
    Button btn_j_profile;
    Button btn_j_leaderboard;
    Button btn_j_signOut;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        tv_j_welcome = findViewById(R.id.tv_v_home_welcome);
        btn_j_easyMode = findViewById(R.id.btn_v_home_easyMode);
        btn_j_intermediateMode = findViewById(R.id.btn_v_home_intermediateMode);
        btn_j_profile = findViewById(R.id.btn_v_home_profile);
        btn_j_leaderboard = findViewById(R.id.btn_v_home_leaderboard);
        btn_j_signOut = findViewById(R.id.btn_v_home_signOut);

        welcomeMessage();
        buttonClickListener();
    }

    private void welcomeMessage()
    {
        tv_j_welcome.setText("Welcome, " + SessionData.getSignedInUser().getUsername());
    }


    private void buttonClickListener()
    {
        btn_j_signOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SessionData.setSignedInUser(null);
                startActivity(new Intent(HomePage.this, MainActivity.class));
            }
        });

        btn_j_profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(HomePage.this, Profile.class));
            }
        });

        btn_j_leaderboard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(HomePage.this, Leaderboard.class));
            }
        });

        btn_j_easyMode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        btn_j_intermediateMode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }

}