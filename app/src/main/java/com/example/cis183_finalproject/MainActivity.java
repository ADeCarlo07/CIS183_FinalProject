package com.example.cis183_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity
{

    Button btn_j_signUp;
    Button btn_j_signIn;
    EditText et_j_username;
    TextView tv_j_error;

    DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);


        //initialize all of the tables with dummy data
        //there is logic in this function to ensure this is not done more than once.
        dbHelper.initAllTables();

        btn_j_signIn = findViewById(R.id.btn_v_signIn);
        btn_j_signUp = findViewById(R.id.btn_v_signUp);
        et_j_username = findViewById(R.id.et_v_username);
        tv_j_error = findViewById(R.id.tv_v_error);



        buttonClickListener();
    }


    private void buttonClickListener()
    {
        btn_j_signIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String username = et_j_username.getText().toString();

                if (username.isEmpty())
                {
                    tv_j_error.setVisibility(View.VISIBLE);
                    tv_j_error.setText("Please enter a username.");
                }
                else
                {
                    boolean isValid = dbHelper.isUsernameValid(username);

                    if (isValid)
                    {
                        if (tv_j_error.getVisibility() == View.VISIBLE)
                        {
                            tv_j_error.setVisibility(View.INVISIBLE);
                        }
                        dbHelper.getAllUserDataGivenUsername(username);
                        startActivity(new Intent(MainActivity.this, HomePage.class));
                    }
                    else
                    {
                        tv_j_error.setVisibility(View.VISIBLE);
                        tv_j_error.setText("Username not found, please try again!");
                    }
                }
            }
        });

        btn_j_signUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }
        });
    }

}