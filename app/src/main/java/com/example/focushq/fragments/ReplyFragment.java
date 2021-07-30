package com.example.focushq.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focushq.Post;
import com.example.focushq.R;
import com.example.focushq.Reply;
import com.example.focushq.ReplyAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ReplyFragment extends Fragment {

    private ReplyAdapter replyAdapter;
    private RecyclerView rvReply;
    private List<String> replies;
    //post belonging to that reply
    private Post post;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_reply,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReply = view.findViewById(R.id.rvReply);

        replies = new ArrayList<>();

        Reply reply = new Reply();
        post = reply.getPost();

        replyAdapter = new ReplyAdapter(getContext(),replies,post);

        //set the adapter
        rvReply.setAdapter(replyAdapter);
        rvReply.setLayoutManager(new LinearLayoutManager(getContext()));
        replies.addAll(post.getReplies());
        replyAdapter.notifyDataSetChanged();
    }
}
