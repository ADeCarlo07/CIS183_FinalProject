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

public class SignUp extends AppCompatActivity
{
    EditText et_j_username;
    EditText et_j_fname;
    EditText et_j_lname;
    EditText et_j_email;
    Button btn_j_back;
    Button btn_j_signUp;

    TextView tv_j_error;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        et_j_username = findViewById(R.id.et_v_signUp_username);
        et_j_fname = findViewById(R.id.et_v_signUp_fname);
        et_j_lname = findViewById(R.id.et_v_signUp_lname);
        et_j_email = findViewById(R.id.et_v_signUp_email);
        btn_j_back = findViewById(R.id.btn_v_signUp_back);
        btn_j_signUp = findViewById(R.id.btn_v_signUp_signUp);
        tv_j_error = findViewById(R.id.tv_v_signUp_error);

        buttonClickListener();
    }

    private void buttonClickListener()
    {
        btn_j_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SessionData.setSignedInUser(null);
                startActivity(new Intent(SignUp.this, MainActivity.class));
            }
        });
        btn_j_signUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (et_j_username.getText().toString().isEmpty() || et_j_fname.getText().toString().isEmpty() || et_j_lname.getText().toString().isEmpty() || et_j_email.getText().toString().isEmpty() )
                {
                    tv_j_error.setVisibility(View.VISIBLE);
                    tv_j_error.setText("Please fill out all feilds.");
                }
                else
                {


                    String username = et_j_username.getText().toString();
                    String fname = et_j_fname.getText().toString();
                    String lname = et_j_lname.getText().toString();
                    String email = et_j_email.getText().toString();

                    if (dbHelper.isUsernameValid(username))
                    {
                        tv_j_error.setVisibility(View.VISIBLE);
                        tv_j_error.setText("Username is already in use.");
                    }
                    else
                    {
                        if (tv_j_error.getVisibility() == View.VISIBLE)
                        {
                            tv_j_error.setVisibility(View.INVISIBLE);

                        }


                        User u = new User();

                        u.setUsername(username);
                        u.setFname(fname);
                        u.setLname(lname);
                        u.setEmail(email);
                        u.setNumPoints(0);

                        dbHelper.addUserToDatabase(u);

                        startActivity(new Intent(SignUp.this, MainActivity.class));
                    }


                }
            }
        });
    }
}