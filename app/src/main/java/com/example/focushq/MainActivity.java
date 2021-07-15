package com.example.focushq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.focushq.fragments.ComposeFragment;
import com.example.focushq.fragments.PostsFragment;
import com.example.focushq.fragments.ProfileFragment;
import com.example.focushq.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        // do something here
                        Log.d(TAG, "home!");
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_compose:
                        // do something here
                        Log.d(TAG, "compose!");
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_search:
                        // do something here
                        Log.d(TAG, "search!");
                        fragment = new SearchFragment();
                        break;
                    case R.id.action_profile:
                        Log.d(TAG, "profile!");
                        fragment = new ProfileFragment();
                        // do something here
                        break;
                    default: break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
               return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }
}