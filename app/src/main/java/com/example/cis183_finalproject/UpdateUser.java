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

public class UpdateUser extends AppCompatActivity
{
    TextView tv_j_username;
    TextView tv_j_error;
    EditText et_j_fname;
    EditText et_j_lname;
    EditText et_j_email;
    Button btn_j_back;
    Button btn_j_update;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        tv_j_username = findViewById(R.id.tv_v_update_username);
        tv_j_error = findViewById(R.id.tv_v_update_error);
        et_j_fname = findViewById(R.id.et_v_update_fname);
        et_j_lname = findViewById(R.id.et_v_update_lname);
        et_j_email = findViewById(R.id.et_v_update_email);
        btn_j_update = findViewById(R.id.btn_v_update_update);
        btn_j_back = findViewById(R.id.btn_v_update_back);

        fillOutTextboxes();
        buttonClickListeners();
    }

    private void fillOutTextboxes()
    {
        if (SessionData.getSignedInUser() != null)
        {
            tv_j_username.setText("Update " + SessionData.getSignedInUser().getUsername());
            et_j_fname.setText(SessionData.getSignedInUser().getFname());
            et_j_lname.setText(SessionData.getSignedInUser().getLname());
            et_j_email.setText(SessionData.getSignedInUser().getEmail());
        }

    }

    private void buttonClickListeners()
    {
        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(UpdateUser.this, Profile.class));
            }
        });

        btn_j_update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (et_j_fname.getText().toString().isEmpty() || et_j_lname.getText().toString().isEmpty() || et_j_email.getText().toString().isEmpty())
                {
                    tv_j_error.setVisibility(View.VISIBLE);
                    tv_j_error.setText("Please fill out all feilds.");
                }
                else
                {
                    if (SessionData.getSignedInUser() != null)
                    {
                        User u = new User();
                        u.setUsername(SessionData.getSignedInUser().getUsername());
                        u.setFname(et_j_fname.getText().toString());
                        u.setLname(et_j_lname.getText().toString());
                        u.setEmail(et_j_email.getText().toString());
                        dbHelper.updateUserData(u);

                        //Username wont change, update session data and get new data for the user
                        SessionData.setSignedInUser(dbHelper.getAllUserDataGivenUsername(SessionData.getSignedInUser().getUsername()));

                        startActivity(new Intent(UpdateUser.this, Profile.class));
                    }

                }
            }
        });
    }

}