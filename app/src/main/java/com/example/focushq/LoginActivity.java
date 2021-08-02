package com.example.focushq;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //checking if user is already signed in
        if(ParseUser.getCurrentUser() != null){
            //Query users get all the current users from the app
            queryUsers();
            //user is already signed in show main activity
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username,password);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick sign up button");
                ParseUser user = new ParseUser();
                //sets core user properties
                user.setUsername(etUsername.getText().toString());
                user.setPassword(etPassword.getText().toString());
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                Log.d(TAG, "username: " + username + " password: " + password);
                //invoke sign up in background
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Hooray! Let them use the app now.
                            Log.d(TAG,"Sign up success!");
                            //Query users get all the current users from the app
                            queryUsers();
                            goMainActivity();
                            Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            Log.i(TAG,"error creating new user " + e);
                            return;
                        }
                    }
                });
            }
        });
        updateUsers();
    }

    public void queryUsers(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.include(User.USERNAME_KEY);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                Log.i(TAG,"objects list size: " + objects.size());
                File file = new File(getFilesDir(),"currentUsers.txt");
                List<String> names = new ArrayList<>();
                for(ParseUser obj: objects){
                    names.add(obj.getUsername());
                }
                try {
                    FileUtils.writeLines(file,names);
                    Log.i(TAG,"usernames written: " + names.toString());
                } catch (IOException ioException) {
                    Log.i(TAG, "error: " + ioException.getLocalizedMessage());
                }
            }
        });
    }

    private void updateUsers(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"updated user list!");
                queryUsers();
            }
        },0,5, TimeUnit.MINUTES);
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    //TODO: better error handling
                    Log.e(TAG, "issue with login", e);
                    Toast.makeText(LoginActivity.this,"Issue with login!", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*navigate to the main activity if the user has signed in properly */
                Log.i(TAG,"Login Success!");
                //Query users get all the current users from the app
                queryUsers();
                goMainActivity();
                Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //function will start an intent to the main activity
    public void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}