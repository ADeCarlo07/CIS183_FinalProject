package com.example.cis183_finalproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MatchesListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<Match> listOfMatches;
    TextView tv_j_result;
    TextView tv_j_difficulty;
    TextView tv_j_mode;

    DatabaseHelper dbHelper;
    public MatchesListAdapter(Context c, ArrayList<Match> lm)
    {
        context = c;
        listOfMatches = lm;
    }

    @Override
    public int getCount()
    {
        return listOfMatches.size();
    }

    @Override
    public Object getItem(int position)
    {
        return listOfMatches.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        if (view == null)
        {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);
            //I want the cell to match the size of the constraint layout too
            view = mInflater.inflate(R.layout.matches_cell, parent, false);
        }
        dbHelper = new DatabaseHelper(context);

        //Find the GUI elements in our custom cell
        tv_j_result = view.findViewById(R.id.tv_v_matches_cell_result);
        tv_j_difficulty = view.findViewById(R.id.tv_v_matches_cell_difficulty);
        tv_j_mode = view.findViewById(R.id.tv_v_matches_cell_mode);



        //Get data for this pet from the listOfPets
        //we can access different elements based off the i value that is passed to this function
        Match match = listOfMatches.get(position);
        String difficultyName = dbHelper.getDifficultyNameGivenId(match.getDifficultyId());
        if (!difficultyName.isEmpty())
        {
            tv_j_difficulty.setText(difficultyName);
            tv_j_result.setText(match.getResult());

            if (tv_j_result.getText().toString().equals("Won"))
            {
                view.setBackgroundColor(Color.parseColor("#f7e8ca"));
                tv_j_mode.setTextColor(Color.parseColor("#744E52"));
                tv_j_result.setTextColor(Color.parseColor("#744E52"));
                tv_j_difficulty.setTextColor(Color.parseColor("#744E52"));
            }
        }


        return view;
    }
}
