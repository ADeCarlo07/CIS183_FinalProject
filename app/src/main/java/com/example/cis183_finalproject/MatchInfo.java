package com.example.cis183_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MatchInfo extends AppCompatActivity
{
    TextView tv_j_result;
    TextView tv_j_difficulty;
    TextView tv_j_time;
    ListView lv_j_moves;
    Button btn_j_back;
    DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_match_into);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        tv_j_result = findViewById(R.id.tv_v_matchInfo_result);
        tv_j_difficulty = findViewById(R.id.tv_v_matchInfo_difficulty);
        tv_j_time = findViewById(R.id.tv_v_matchInfo_time);
        btn_j_back = findViewById(R.id.btn_v_matchInfo_back);
        lv_j_moves = findViewById(R.id.lv_v_matchInfo_moves);
    }

    private void fillOutTextboxes()
    {

    }

    private void buttonClickListener()
    {
        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MatchInfo.this, Profile.class));
            }
        });

        lv_j_moves.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Student studentSelected = listOfStudents.get(position);
                SessionData.setSelectedStudent(studentSelected);
                SessionData.fillOutStudentData = true;
                startActivity(new Intent(MainActivity.this, StudentDetails.class));
            }
        });
    }

}