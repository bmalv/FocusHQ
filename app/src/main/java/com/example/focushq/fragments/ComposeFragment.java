package com.example.focushq.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.focushq.Post;
import com.example.focushq.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public final static int PICK_PHOTO_CODE = 0;

    private Button btnCaptureImage;
    private ImageButton btnPublish;
    private EditText etDescription;
    private EditText etLocationName;
    private ImageSwitcher ivImage;
    private Button btnPrev;
    private Button btnNext;
    private File photoFile;
    public String photoFileName = "photo.jpg";

    //store images
    private ArrayList<Uri> imageUris;

    //position of selected image
    int index = 0;


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
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);

        photoFile = new File(photoFileName);
        imageUris = new ArrayList<>();

        //setting up image switcher
        ivImage.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getContext().getApplicationContext());
                return imageView;
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked show previous image if any
                if(index > 0){
                    index--;
                    //setting image switcher to previous image
                    ivImage.setImageURI(imageUris.get(index));
                }else{
                    //no previous images
                    Toast.makeText(getContext(), "No previous image!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked show next image if any
                if(index < imageUris.size()){
                    index++;
                    //setting image switcher to the next image
                    ivImage.setImageURI(imageUris.get(index));
                }else{
                    //there are no more images to show
                    Toast.makeText(getContext(), "No more images!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

//                //checking the photo
//                if(ivImage == null){
//                     /*if getDrawable is null means the user is
//                    trying to submit a post without having an image data*/
//                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                ParseUser currentUser = ParseUser.getCurrentUser();
                //save the post to Parse fields
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
        startActivityForResult(Intent.createChooser(intent,"Select Image(s)"), PICK_PHOTO_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {

        if(requestCode == PICK_PHOTO_CODE && resultCode == RESULT_OK){
            ClipData clipData = data.getClipData();
            if(clipData != null){
                //user picked multiple images
                //iterate through images picked
                for(int i = 0; i < clipData.getItemCount(); i++){
                    //grab image at index
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    //add image to list
                    imageUris.add(imageUri);
                }
                //set first image picked to image switcher
                ivImage.setImageURI(imageUris.get(0));
                index = 0;
            }else{
                //user picked a single image
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
                //set image to image switcher
                ivImage.setImageURI(imageUris.get(0));
                index = 0;
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    private void savePost(String description, String locationName, ParseUser currentUser) {
        Post post = new Post();
        post.setDescription(description);
        post.setLocationName(locationName);
      //  Log.d(TAG, "image uri: " + imageUris.get(0).getPath());
        File file = new File(getPath(imageUris.get(0)));
        ParseFile parseFile = new ParseFile(file);
        post.setImage(parseFile);
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


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }



}