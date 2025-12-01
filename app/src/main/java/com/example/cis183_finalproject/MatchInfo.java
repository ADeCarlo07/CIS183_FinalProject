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

import java.util.ArrayList;

public class MatchInfo extends AppCompatActivity
{
    TextView tv_j_result;
    TextView tv_j_difficulty;
    TextView tv_j_time;
    ListView lv_j_moves;
    Button btn_j_back;
    DatabaseHelper dbHelper;
    ArrayList<Move> listOfMoves;
    MovesListAdapter movesListAdapter;
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

        fillOutMovesList();
        fillOutTextboxes();
        buttonClickListener();
    }
    private void fillOutMovesList()
    {
        listOfMoves = dbHelper.getAllMovesGivenMatchId(SessionData.getSelectedMatch().getId());

        movesListAdapter = new MovesListAdapter(this, listOfMoves);
        lv_j_moves.setAdapter(movesListAdapter);
    }
    private void fillOutTextboxes()
    {
        dbHelper.setAllMatchDataGivenId(SessionData.getSelectedMatch().getId());

        tv_j_result.setText(SessionData.getSelectedMatch().getResult());

        String difficultyName = dbHelper.getDifficultyNameGivenId(SessionData.getSelectedMatch().getDifficultyId());
        tv_j_difficulty.setText(difficultyName);

        tv_j_time.setText(String.valueOf(SessionData.getSelectedMatch().getTime()));
    }

    private void buttonClickListener()
    {
        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SessionData.setSelectedMatch(null);
                startActivity(new Intent(MatchInfo.this, Profile.class));
            }
        });


    }

}