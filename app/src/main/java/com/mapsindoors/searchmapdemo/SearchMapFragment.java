package com.mapsindoors.searchmapdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationSourceOnStatusChangedListener;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
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
    MPLocation locationToSelect = null;

    /***
     A listener to report the click on the search Button to the activity
     ***/
    private OnFragmentInteractionListener mListener;
    /***
     The Venue's coordinates
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //

    public SearchMapFragment()
    {
        // Required empty public constructor
    }

    @NonNull
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
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
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
            MapsIndoors.removeLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );

            mMapControl.onDestroy();
        }

        super.onDestroyView();
    }
    //endregion


    private void setupView( View rootView) {

        final FragmentManager fm = getChildFragmentManager();
        searchButton = rootView.findViewById( R.id.search_button );

        searchButton.setOnClickListener( v -> mListener.onSearchButtonClick() );
        searchButton.setEnabled( false );

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
        final Activity context = getActivity();

        if( (context == null) || (mMapFragment == null) || (mMapFragment.getView() == null) )
        {
            return;
        }

        /***
         Setting the API key to the desired solution
         ***/
        if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key ) ) )
        {
            MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );
        }

        /***
         Instantiate the MapControl object
         ***/
        mMapControl = new MapControl( context );
        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );

        // Enable the search button only once location data becomes available
        MapsIndoors.addLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );

        /***
         * init the MapControl object which will sync data.
         * When the init is done, if the 'locationToSelect' is not null we call the 'mMapControl.selectLocation()' to select the desired location otherwise select a floor
         ***/
        mMapControl.init( miError -> {

            if( miError == null )
            {
                final Activity _context = getActivity();
                if( _context != null )
                {
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
    public void selectLocation( MPLocation loc )
    {
        locationToSelect = loc;
    }
    //

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        if( context instanceof OnFragmentInteractionListener ) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    final MPLocationSourceOnStatusChangedListener locationSourceOnStatusChangedListener = ( status, sourceId ) -> {
        if( status == MPLocationSourceStatus.AVAILABLE ) {
            final Activity context = getActivity();
            if( context != null ) {
                context.runOnUiThread( () -> searchButton.setEnabled( true ) );
            }
        }
    };
}
