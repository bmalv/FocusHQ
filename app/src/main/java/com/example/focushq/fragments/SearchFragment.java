package com.example.focushq.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.focushq.Post;
import com.example.focushq.PostsAdapter;
import com.example.focushq.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 0;
    private final String TAG = "SearchFragment";

    private PlacesClient placesClient;
    private RecyclerView rvResults;
    private PostsAdapter adapter;
    private List<Post> postsList;
    private String locationName;
    private Button btnLocationSearch;
    private Button btnUserSearch;
    private AutoCompleteTextView tvAuto;
    private AutocompleteSupportFragment autocompleteFragment;
    private Button btnSearch;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvResults = view.findViewById(R.id.rvResults);
        postsList = new ArrayList<>();
        adapter = new PostsAdapter(getContext(),postsList);
        btnLocationSearch = view.findViewById(R.id.btnLocationSearch);
        btnUserSearch = view.findViewById(R.id.btnUserSearch);
        btnSearch = view.findViewById(R.id.btnSearch);

        //initialize the SDK
        Places.initialize(getContext().getApplicationContext(), getResources().getString(R.string.MY_KEY));
        //create a new PlacesClient instance
        placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Initialize the Autocomplete text view
        tvAuto = (AutoCompleteTextView) view.findViewById(R.id.autoUsers);

        //set search bars visibility
        autocompleteFragment.getView().setVisibility(View.GONE);
        tvAuto.setVisibility(View.GONE);
        btnSearch.setVisibility(View.GONE);

        btnLocationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Location Search Mode!");
                Toast.makeText(getContext(), "Location Search Mode!", Toast.LENGTH_LONG).show();
                tvAuto.setVisibility(View.GONE);
                btnSearch.setVisibility(View.GONE);
                autocompleteFragment.getView().setVisibility(View.VISIBLE);
                setLocationSearch();
            }
        });

        btnUserSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: set the visibility of the autocomplete view
                tvAuto.setVisibility(View.VISIBLE);
                btnSearch.setVisibility(View.VISIBLE);
                autocompleteFragment.getView().setVisibility(View.GONE);
                postsList.clear();
                adapter.notifyDataSetChanged();
                Log.i(TAG, "User Search Mode!");
                Toast.makeText(getContext(), "User Search Mode!", Toast.LENGTH_LONG).show();
                String[] usernames = getResources().getStringArray(R.array.usernames_array);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, usernames);
                tvAuto.setAdapter(adapter);

                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: handle searching for the user
                        Log.i(TAG,"search for user!");
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLocationSearch(){

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
                FetchPlaceRequest request = new FetchPlaceRequest() {
                    @NonNull
                    @Override
                    public String getPlaceId() {
                        return place.getId();
                    }

                    @NonNull
                    @Override
                    public List<Place.Field> getPlaceFields() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public AutocompleteSessionToken getSessionToken() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public CancellationToken getCancellationToken() {
                        return null;
                    }
                };

                locationName = place.getName();
                Log.i(TAG, "set location name: " + locationName);
                rvResults.setAdapter(adapter);
                rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
                postsList.clear();
                adapter.notifyDataSetChanged();
                queryPosts();
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void queryPosts() {
        Log.i(TAG,"in queryPosts");
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        //want to get the author information of the posts
        query.include(Post.USER_KEY);
        //only include posts with the same location name
        query.whereEqualTo(Post.LOCATION_NAME_KEY, locationName);
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