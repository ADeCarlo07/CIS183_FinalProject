package com.example.cis183_finalproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LeaderboardListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<User> listOfUsers;
    TextView tv_j_username;
    TextView tv_j_rank;
    TextView tv_j_numPoints;

    DatabaseHelper dbHelper;
    public LeaderboardListAdapter(Context c, ArrayList<User> lu)
    {
        context = c;
        listOfUsers = lu;
    }

    @Override
    public int getCount()
    {
        return listOfUsers.size();
    }

    @Override
    public Object getItem(int position)
    {
        return listOfUsers.get(position);
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
            view = mInflater.inflate(R.layout.leaderboard_cell, parent, false);
        }
        dbHelper = new DatabaseHelper(context);

        //Find the GUI elements in our custom cell
        tv_j_username = view.findViewById(R.id.tv_v_leaderboard_cell_username);
        tv_j_rank = view.findViewById(R.id.tv_v_leaderboard_cell_rank);
        tv_j_numPoints = view.findViewById(R.id.tv_v_leaderboard_cell_totalPoints);

        view.setBackgroundColor(Color.parseColor("#744E52"));

        int rank = position + 1;

        if (position % 2 == 0)
        {
            view.setBackgroundColor(Color.parseColor("#482B2E"));
        }

        //we can access different elements based off the i value that is passed to this function
        User user = listOfUsers.get(position);
        tv_j_rank.setText("#" + rank);
        tv_j_username.setText(user.getUsername());
        tv_j_numPoints.setText(String.valueOf(user.getNumPoints()));


        return view;
    }
}
