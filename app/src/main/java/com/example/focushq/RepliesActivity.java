package com.example.focushq;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focushq.fragments.ReplyFragment;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class RepliesActivity extends Fragment {

    private RecyclerView rvReplies;
    private ReplyAdapter replyAdapter;
    private Post post;
    private List<String> replyList;
    private ImageButton ivReply;

    public RepliesActivity(){}

    public RepliesActivity(Post post){
        this.post = post;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_replies,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReplies = view.findViewById(R.id.rvReply);
        ivReply = view.findViewById(R.id.ivReply);

        replyList = post.getReplies();
        replyAdapter = new ReplyAdapter(getContext(),replyList,post);

        rvReplies.setAdapter(replyAdapter);
        rvReplies.setLayoutManager(new LinearLayoutManager(getContext()));

        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("RepliesActivity", "reply button clicked");
                Fragment fragment = new ReplyFragment(post);
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContainer,fragment).commit();
            }
        });

//        replyList.addAll(post.getReplies());
        Log.i("RepliesActivity","update the reply adapter");
        replyAdapter.notifyDataSetChanged();
    }
}
