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
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.focushq.Post;
import com.example.focushq.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    private ImageSwitcher ivImage;
    private Button btnPrev;
    private Button btnNext;
    public String photoFileName = "photo.jpg";
    private File file;
    private PlacesClient placesClient;
    private String locationName;
    private Place selectedPlace;

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

        etDescription = view.findViewById(R.id.etDescription);
        btnPublish = view.findViewById(R.id.btnPublish);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        ivImage = view.findViewById(R.id.ivImage);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);

        file = new File(photoFileName);

        imageUris = new ArrayList<>();

        //initialize the SDK
        Places.initialize(getContext().getApplicationContext(), getResources().getString(R.string.MY_KEY));
        //create a new PlacesClient instance
        placesClient = Places.createClient(getContext());

        setLocationSearch();

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
                if(imageUris.get(0) == null){
                     /*if getDrawable is null means the user is
                    trying to submit a post without having an image data*/
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap selectedImage = rotateBitmapOrientation(getPath(imageUris.get(0)));
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(selectedImage, 200);
                Log.d(TAG, "resized the bitmap!");
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(resizedFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // Write the bytes of the bitmap to file
                try {
                    fos.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file = resizedFile;

                ParseUser currentUser = ParseUser.getCurrentUser();
                //save the post to Parse fields
                savePost(description,currentUser);
                goToPosts();

            }
        });
    }

    //method go to post fragment
    public void goToPosts(){
        Fragment fragment = new PostsFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_compose, fragment).commit();
    }

    //function will open up the user's gallery
    public void launchGalleryIntent() {
        Log.i(TAG,"launched gallery intent");
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


    private void savePost(String description, ParseUser currentUser) {
        Post post = new Post();
        post.setDescription(description);
        post.setLocationName(locationName);
      //  File file = new File(getPath(imageUris.get(0)));
        ParseFile parseFile = new ParseFile(file);
        post.setImage(parseFile);
        post.setUser(currentUser);
        post.setLocationID(selectedPlace.getId());
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
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        Log.d(TAG,"rotated the bitmap!");
        // Return result
        return rotatedBitmap;
    }

    public static class BitmapScaler {
        // Scale and maintain aspect ratio given a desired width
        // BitmapScaler.scaleToFitWidth(bitmap, 100);
        public static Bitmap scaleToFitWidth(Bitmap b, int width)
        {
            float factor = width / (float) b.getWidth();
            return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
        }


        // Scale and maintain aspect ratio given a desired height
        // BitmapScaler.scaleToFitHeight(bitmap, 100);
        public static Bitmap scaleToFitHeight(Bitmap b, int height)
        {
            float factor = height / (float) b.getHeight();
            return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
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

    private void setLocationSearch(){
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setCountries("US");

        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(30.1572,-97.8191),
                new LatLng(30.4008,-97.7141)));

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + " place ID: " + place.getId());
                selectedPlace = place;
                locationName = place.getName();
            }
            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

}