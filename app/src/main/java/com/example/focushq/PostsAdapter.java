package com.example.focushq;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;

    //constructor
    public PostsAdapter(Context context, List<Post> posts){
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating post
        View view = LayoutInflater.from(context).inflate(R.layout.item_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        //shows an item in that position to the user
        Post post = posts.get(position);
        //binds the data of the post into that view
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tvUsername;
        private TextView tvCreatedAt;
        private TextView tvDescription;
        private TextView tvLocationName;
        private ImageView ivImage;
        private ImageView ivProfileImage;
        private ImageButton ivReply;
        private ImageButton ivLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivLike = itemView.findViewById(R.id.ivLike);
            itemView.setOnClickListener(this);
        }

        //updates views to display the given post
        public void bind(Post post) {
            //bind the post data to the view elements
            tvDescription.setText(post.getDescription());
            tvUsername.setText(post.getUser().getUsername());
            tvLocationName.setText(post.getLocationName());

            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);
            tvCreatedAt.setText(timeAgo);

            ParseFile image = post.getImage();
            if (image != null) {
                Log.d(TAG, "loaded post image");
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }

            ParseFile profileImage = post.getProfileImage();
            if(profileImage != null){
                Log.d(TAG, "loaded profile pic");
                Glide.with(context).load(profileImage.getUrl()).circleCrop().into(ivProfileImage);
            }

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "like button clicked");
                    ivLike.setBackground(ContextCompat.getDrawable(context,R.drawable.ufi_heart_active));
                }
            });

            ivReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "reply button clicked");
                    //go to a reply view
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        Fragment fragment = new RepliesActivity(posts.get(pos));
                        AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
                        FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.flContainer,fragment).commit();
                    }
                }
            });

        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if(pos != RecyclerView.NO_POSITION) {
                Post post = posts.get(pos);
                //create intent for the new activity
                Intent intent = new Intent(context, PostsDetailsActivity.class);
                //serialize the movie using parceler, use its short name as a key
                intent.putExtra(Post.class.getSimpleName(), Parcels.wrap(post));
                //show the activity
                Log.d(TAG, "showing the activity!");
                context.startActivity(intent);
            }
        }
    }
}
