package com.example.focushq;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class PostsDetailsActivity extends AppCompatActivity {

    //the post to display
    Post post;

    //view objects
    TextView tvUsername;
    TextView tvDescription;
    TextView tvLocationName;
    ImageView ivProfileImage;
    ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_details);

        tvUsername = findViewById(R.id.tvUsername);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocationName = findViewById(R.id.tvLocationName);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivImage = findViewById(R.id.ivImage);

        post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        Log.i("PostsDetailsActivity", "Showing Post Details!");

        tvUsername.setText(post.getUser().getUsername());
        tvDescription.setText(post.getDescription());
        tvLocationName.setText(post.getLocationName());

        ParseFile pic = post.getProfileImage();
        if (pic != null) {
            Glide.with(this).load(pic.getUrl()).circleCrop().into(ivProfileImage);
        }

        ParseFile image = post.getImage();
        if(image != null){
            //upload the image
            Glide.with(this)
                    .load(post.getImage()
                     .getUrl())
                    .into(ivImage);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar.
        Log.d("PostsDetailsActivity","in inflating menu");
        getMenuInflater().inflate(R.menu.menu_back_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_back:
                // Handle this selection
                ParseUser.logOut();
                Log.i("PostsDetailsActivity", "user taken back to home screen");
                //go back to home screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}