package com.example.focushq.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.focushq.Post;
import com.example.focushq.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    private Button btnCaptureImage;
    private ImageButton btnPublish;
    private EditText etDescription;
    private EditText etLocationName;
    private ImageView ivImage;
    private File photoFile;
    public String photoFileName = "photo.jpg";
    private ArrayList<Object> photoPaths;


    public ComposeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etLocationName = view.findViewById(R.id.etLocationName);
        etDescription = view.findViewById(R.id.etDescription);
        btnPublish = view.findViewById(R.id.btnPublish);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        ivImage = view.findViewById(R.id.ivImage);

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"capture button clicked!");
               // onPickPhoto(v);
                if(ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    //request for permission since was not granted
                    ActivityCompat.requestPermissions((Activity) getContext(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            100);
                   return;
                }
                launchGalleryIntent();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"publish button clicked!");
                String description = etDescription.getText().toString();
                String locationName = etLocationName.getText().toString();

                //description can not be empty
                if(description.isEmpty()){
                    //we do not want the user to make a post
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(locationName.isEmpty()){
                    //we do not want the user to make a post
                    Toast.makeText(getContext(), "Location Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                //checking the photo
                if(ivImage.getDrawable() == null){
                     /*if getDrawable is null means the user is
                    trying to submit a post without having an image data*/
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, locationName, currentUser);
            }
        });
    }

    //function will open up the user's gallery
    public void launchGalleryIntent() {
        //permission was granted
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent will allow multiple images to be chosen
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //this will allow the user to pick any type of image
        intent.setType("image/*");
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();

            if(clipData != null){
                //user selected multiple images
                Log.d(TAG, "user selected multiple images!");
                for(int i = 0; i < clipData.getItemCount(); i++){
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    try {
                        InputStream is = getContext().getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap);
                    } catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }else{
                //only one image was selected
                Log.d(TAG, "user selected one image!");
                Uri imageUri = data.getData();
                try {
                    InputStream is = getContext().getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    bitmaps.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (final Bitmap b : bitmaps) {
//                        getContext().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ivImage.setImageBitmap(b);
//                            }
//                        });
//
//                        try {
//                            Thread.sleep(3000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
        }
    }


    private void savePost(String description, String locationName, ParseUser currentUser) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful!");
                etDescription.setText("");
                //0 means the empty resource id
                ivImage.setImageResource(0);
            }
        });
    }



}