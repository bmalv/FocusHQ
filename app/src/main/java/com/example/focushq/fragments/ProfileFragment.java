package com.example.focushq.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.focushq.Post;
import com.example.focushq.PostsAdapter;
import com.example.focushq.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * TODO: find a way to inherit from PostsFragment without it messing up grid view
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private RecyclerView rvPosts;
    private PostsAdapter adapter;
    private List<Post> postsList;
    private TextView tvUsername;
    private ImageView ivProfileImage;

    private MenuItem logoutItem;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);
        tvUsername = view.findViewById(R.id.tvUsername);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
//        logoutItem = view.findViewById(R.id.action_logout);

        postsList = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), postsList);

        tvUsername.setText(ParseUser.getCurrentUser().getUsername());
        //TODO: access the profile image and set it
        ParseFile profileImage = ParseUser.getCurrentUser().getParseFile("profileImage");
        if(profileImage != null){
            Log.d("ProfileFragment", "loaded profile pic");
            Glide.with(getContext())
                    .load(profileImage.getUrl())
                    .circleCrop()
                    .into(ivProfileImage);
        }


        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        queryPosts();
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        //want to get the author information of the posts
        query.include(Post.USER_KEY);
        query.whereEqualTo(Post.USER_KEY, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder(Post.CREATED_AT_KEY);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                //not null iterate through posts
                for(Post post: posts){
                    //prints in the log cat the post along with user associated with the post
                    Log.i(TAG,"Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }
                postsList.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

}