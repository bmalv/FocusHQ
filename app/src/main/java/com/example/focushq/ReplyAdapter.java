package com.example.focushq;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> {

    //post that is being replied to
    private Context context;
    private List<String> replyList;
    private Post post;

    public ReplyAdapter(Context context, List<String> replyList, Post post){
        this.context = context;
        this.replyList = replyList;
        this.post = post;
    }


    @NonNull
    @Override
    public ReplyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reply,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.ViewHolder holder, int position) {
        String reply = replyList.get(position);
        holder.bind(reply);
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfileImage;
        private TextView tvUsername;
        private EditText etComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            etComment = itemView.findViewById(R.id.etComment);
        }

        public void bind(String reply) {
            etComment.setText(reply);
            tvUsername.setText(ParseUser.getCurrentUser().getUsername());

            ParseFile image = ParseUser.getCurrentUser().getParseFile("profileImage");
            if(image != null){
                Log.d("ReplyAdapter", "loaded profile pic");
                Glide.with(context).load(image.getUrl()).circleCrop().into(ivProfileImage);
            }

            post.add("replies",reply);
            post.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.i("ReplyAdapter","success!");
                    }else{
                        Log.i("ReplyAdapter","error adding reply");
                    }
                }
            });
        }
    }
}
