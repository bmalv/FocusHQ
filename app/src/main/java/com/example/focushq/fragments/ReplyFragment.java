package com.example.focushq.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.focushq.Post;
import com.example.focushq.PostsDetailsActivity;
import com.example.focushq.R;
import com.example.focushq.RepliesActivity;
import com.example.focushq.Reply;
import com.example.focushq.ReplyAdapter;
import com.example.focushq.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ReplyFragment extends Fragment {

    public static final String TAG = "ReplyFragment";

    //post belonging to that reply
    private Post post;
    private ImageView ivProfileImage;
    private TextView tvUsername;
    private EditText etComment;
    private ImageButton btnPublish;

    public ReplyFragment(){}

    public ReplyFragment(Post post){
        this.post = post;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_reply,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvUsername = view.findViewById(R.id.tvUsername);
        etComment = view.findViewById(R.id.etComment);
        btnPublish = view.findViewById(R.id.btnPublish);

        ParseUser currentUser = ParseUser.getCurrentUser();

        ParseFile image = currentUser.getParseFile(User.PROFILE_IMAGE_KEY);
        if(image != null){
            Glide.with(getContext()).load(image.getUrl()).circleCrop().into(ivProfileImage);
        }

        tvUsername.setText(currentUser.getUsername());

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "publish button clicked!");
                String comment = etComment.getText().toString();

                //comment can not be empty
                if(comment.isEmpty()){
                    Toast.makeText(getContext(),"Comment can not be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Reply reply = new Reply();
                reply.setReply(comment);
                reply.setUser(ParseUser.getCurrentUser());
                reply.setPost(post);
                reply.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null){
                            if(e != null){
                                Log.e(TAG, "Error while saving", e);
                            }
                            Log.i(TAG, "Reply save was successful!");
                            etComment.setText("");
                        }
                    }
                });
                goToReplyDetails();
            }
        });
    }

    private void goToReplyDetails(){
        Log.i(TAG,"going to replies activity");
        Fragment fragment = new RepliesActivity(post);
        AppCompatActivity activity = (AppCompatActivity) getContext();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContainer,fragment).commit();
    }

}
