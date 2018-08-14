package com.mapsindoors.searchmapdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;

/***
 ---
 title: Create a Search Experience with MapsIndoors - Part 2
 ---

 This is part 2 of the tutorial of creating a simple search experience using MapsIndoors. [In Part 1 we created the search Fragment](searchmapdemosearchfragment). Now we will create the "main" controller displaying the map and eventually the selected location.

 Start by creating a Fragment
 ***/
public class SearchMapFragment extends Fragment
//
{

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap mGoogleMap;


    /***
     Add other needed views for this example
     ***/
    SupportMapFragment mMapFragment;
    Button searchButton;
    Location locationToSelect = null;

    /***
     A listener to report the click on the search Button to the activity
     ***/
    private OnFragmentInteractionListener mListener;
    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //

    public SearchMapFragment()
    {
        // Required empty public constructor
    }

    public static SearchMapFragment newInstance() {
        return new SearchMapFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    //region FRAGMENT LIFECYCLE

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_map, container, false);
    }

    @Override
    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );

        setupView( view );
    }

    @Override
    public void onDestroyView()
    {
        if( mMapControl != null )
        {
            mMapControl.onDestroy();
        }

        super.onDestroyView();
    }

    private void setupView( View rootView) {

        FragmentManager fm = getChildFragmentManager();
        searchButton = rootView.findViewById(R.id.search_button);

        searchButton.setOnClickListener( v -> mListener.onSearchButtonClick() );

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);

        mMapFragment.getMapAsync( mOnMapReadyCallback );
    }

    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 13.0f ) );

            setupMapsIndoors();
        }
    };

    void setupMapsIndoors()
    {
        /***
         Setting the API key to the desired solution
         ***/
        if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key ) ) )
        {
            MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );
        }


        if( getActivity() == null )
        {
            return;
        }

        /***
         Instantiate the MapControl object
         ***/
        mMapControl = new MapControl( getActivity(), mMapFragment, mGoogleMap );
        /***
         * init the MapControl object which will sync data.
         * When the init is done, if the 'locationToSelect' is not null we call the 'mMapControl.selectLocation()' to select the desired location otherwise select a floor
         ***/
        mMapControl.init( miError -> {

            if( miError == null )
            {

                Activity context = getActivity();
                if( context != null )
                {
                    context.runOnUiThread(() -> {

                        mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );

                        if( locationToSelect != null )
                        {
                            mMapControl.selectLocation( locationToSelect );
                            locationToSelect = null;
                        }
                        else
                        {
                            mMapControl.selectFloor( 1 );
                        }
                    });
                }
            }
        });
        //
    }

    public interface OnFragmentInteractionListener {
        void onSearchButtonClick();
    }
    /***
     A public method to select a location
     ***/
    public void selectLocation(Location loc){
        locationToSelect = loc;
    }
    //
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }
}
