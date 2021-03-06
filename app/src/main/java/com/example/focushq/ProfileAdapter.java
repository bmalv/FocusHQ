package com.example.focushq;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.focushq.fragments.ProfileFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>{

    public static final String TAG = "ProfileAdapter";

    private Context context;
    private List<ParseUser> users;

    public ProfileAdapter(Context context, List<ParseUser> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView ivProfileImage;
        private TextView tvUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG,"profile was clicked");
            ParseUser author = users.get(0);
            Fragment fragment = new ProfileFragment(author);
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();

        }

        public void bind(ParseUser user) {
            tvUsername.setText(user.getUsername());
            ParseFile profileImage = user.getParseFile("profileImage");
            if(profileImage != null){
                Log.d(TAG, "loaded profile pic");
                Glide.with(context).load(profileImage.getUrl()).circleCrop().into(ivProfileImage);
            }
        }
    }
}
