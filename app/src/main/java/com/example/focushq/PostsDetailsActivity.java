package com.example.focushq;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.focushq.fragments.ProfileFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostsDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    //the post to display
    Post post;

    //view objects
    TextView tvUsername;
    TextView tvDescription;
    TextView tvLocationName;
    ImageView ivProfileImage;
    ImageView ivImage;
    MapView mvMap;

    //sdk client variable
    PlacesClient placesClient;
    String placeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_details);

        //initialize the SDK
        Places.initialize(getApplicationContext(), "com.google.android.geo.API_KEY");
        //create a new PlacesClient instance
        placesClient = Places.createClient(this);

        tvUsername = findViewById(R.id.tvUsername);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocationName = findViewById(R.id.tvLocationName);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivImage = findViewById(R.id.ivImage);

        initGoogleMaps(savedInstanceState);

        post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        Log.i("PostsDetailsActivity", "Showing Post Details!");

        tvUsername.setText(post.getUser().getUsername());
        tvDescription.setText(post.getDescription());
        tvLocationName.setText(post.getLocationName());

        setPlaceID();

        ParseFile pic = post.getProfileImage();
        if (pic != null) {
            Glide.with(this).load(pic.getUrl()).circleCrop().into(ivProfileImage);
        }

        ParseFile image = post.getImage();
        if(image != null){
            //upload the image
            Glide.with(this)
                    .load(post.getImage()
                     .getUrl())
                    .into(ivImage);
        }

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("PostsDetailsActivity", "profile image was clicked");
                //let user travel to that users profile
                ParseUser author = post.getUser();
                Fragment fragment = new ProfileFragment(author);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.relativeLayout, fragment).commit();
            }
        });

    }

    //function makes a FetchPlaceRequest and sets the placeID
    //to the location name given by the user
    public void setPlaceID(){
        FetchPlaceRequest request  = new FetchPlaceRequest() {

            @NonNull
            @Override
            public String getPlaceId() {
                //returns the ID of the place to be requested
                return tvLocationName.getText().toString();
            }

            @NonNull
            @Override
            public List<Place.Field> getPlaceFields() {
                //Returns the Place.Field list to be requested.
                return null;
            }

            @Nullable
            @Override
            public AutocompleteSessionToken getSessionToken() {
                //Returns the AutocompleteSessionToken used for sessionizing
                //multiple instances ofFindAutocompletePredictionsRequest.
                return null;
            }

            @Nullable
            @Override
            public CancellationToken getCancellationToken() {
                //Returns the CancellationToken used by PlacesClient to
                // cancel any queued requests.
                return null;
            }
        };

        if(request == null){
            Log.i("PostsDetailsActivity", "request is null");
        }else{
            Log.i("PostsDetailsActivity", "request place ID: " + request.getPlaceId());
            placeID = request.getPlaceId();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar.
        Log.d("PostsDetailsActivity","in inflating menu");
        getMenuInflater().inflate(R.menu.menu_back_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_back:
                Log.i("PostsDetailsActivity", "user taken back to home screen");
                //go back to home screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initGoogleMaps(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mvMap = findViewById(R.id.mvMap);
        mvMap.onCreate(mapViewBundle);
        mvMap.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mvMap.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mvMap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mvMap.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Geocoder location = new Geocoder(this);
        try {
            List<Address> response = location.getFromLocationName(placeID, 1);
            Log.i("PostsDetailsActivity", "list from location name: " + response.toString());
            if(response.size() == 1){
                Address addy = response.get(0);
                LatLng position = new LatLng(addy.getLatitude(),addy.getLongitude());

                String snip = "Address: " + addy.getAddressLine(0);
                googleMap.addMarker(new MarkerOptions().position(position).title(placeID).snippet(snip));
                LatLngBounds bounds = new LatLngBounds(position,position);
                LatLng center = bounds.getCenter();
                googleMap.setLatLngBoundsForCameraTarget(bounds);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center,15));

                //if user clicks the marker on the map
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(PostsDetailsActivity.this));
                        marker.showInfoWindow();
                        if(marker.isInfoWindowShown()){
                            Log.i("PostsDetailsActivity", "showing info window");
                        }
                        return true;
                    }
                });

            }else{
                Log.i("PostsDetailsActivity", "no address found");
            }
        } catch (IOException e) {
            Log.i("PostsDetailsActivity", "error getting from location name");
        }
    }



    @Override
    public void onPause() {
        mvMap.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mvMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mvMap.onLowMemory();
    }
}