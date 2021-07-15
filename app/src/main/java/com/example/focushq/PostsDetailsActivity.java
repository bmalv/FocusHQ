package com.example.focushq;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

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
}