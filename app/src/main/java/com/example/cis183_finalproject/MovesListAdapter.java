package com.example.cis183_finalproject;

import android.content.Context;
import android.graphics.Color;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MovesListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<Move> listOfMoves;
    TextView tv_j_turnNum;
    TextView tv_j_uTo;
    TextView tv_j_uFrom;
    TextView tv_j_bTo;
    TextView tv_j_bFrom;

    DatabaseHelper dbHelper;
    public MovesListAdapter(Context c, ArrayList<Move> lm)
    {
        context = c;
        listOfMoves = lm;
    }

    @Override
    public int getCount()
    {
        return listOfMoves.size();
    }

    @Override
    public Object getItem(int position)
    {
        return listOfMoves.get(position);
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
            view = mInflater.inflate(R.layout.moves_cell, parent, false);
        }
        dbHelper = new DatabaseHelper(context);

        //Find the GUI elements in our custom cell
        tv_j_turnNum = view.findViewById(R.id.tv_v_moves_cell_turn);
        tv_j_uTo = view.findViewById(R.id.tv__v_moves_cell_uTo);
        tv_j_uFrom = view.findViewById(R.id.tv__v_moves_cell_uFrom);
        tv_j_bTo = view.findViewById(R.id.tv__v_moves_cell_bTo);
        tv_j_bFrom = view.findViewById(R.id.tv__v_moves_cell_bFrom);


        //we can access different elements based off the i value that is passed to this function
        Move move = listOfMoves.get(position);


        int turn = position + 1;
        tv_j_turnNum.setText("Turn " + turn);

        tv_j_uFrom.setText(String.valueOf(getSquareNumber(move.getFromSquareRowU(), move.getFromSquareColU())));
        tv_j_uTo.setText(String.valueOf(getSquareNumber(move.getToSquareRowU(), move.getToSquareColU())));

        if (move.getFromSquareRowB() == -1 && move.getFromSquareColB() == -1 && move.getToSquareRowB() == -1 && move.getToSquareColB() == -1)
        {
            tv_j_bFrom.setText("Trapped");
            tv_j_bTo.setText("Trapped");
        }
        else
        {
            tv_j_bFrom.setText(String.valueOf(getSquareNumber(move.getFromSquareRowB(), move.getFromSquareColB())));
            tv_j_bTo.setText(String.valueOf(getSquareNumber(move.getToSquareRowB(), move.getToSquareColB())));
        }


        //debugging purposes
        Log.d("Move Info ", "bot From " + move.getFromSquareRowU() + ", " + move.getFromSquareColU());
        Log.d("Move Info ", "bot From " + move.getToSquareRowU() + ", " + move.getToSquareColU());

        return view;
    }


    //For more information, visit checkers move notation websites such as:
    //https://www.bobnewell.net/nucleus/checkers.php?itemid=289

    //***Its very important to note my dummy data, these were entered at random and no real
    //thought was put into the numbers. If they do not properly conform to accepted
    //conventions, that is why. When a normal match is played out it will look more acceptable.
    private int getSquareNumber(int row, int col)
    {



        int num = 0;
        for (int r = 0; r <= row; r++)
        {
            for (int c = 0; c < 8; c++)
            {
                if ((r + c) % 2 == 1)
                {
                    num++;

                }

                if (r == row && c == col)
                {
                    return num;
                }
            }
        }
        return -1;
    }
}
