package com.mapsindoors.showuserLocation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;

/***
 ---
 title: Show the Blue Dot with MapsIndoors - Part 2
 ---

 This is part 2 of the tutorial of managing a blue dot on the map. [In Part 1 we created the position provider](showuserlocationdemopositionprovider). Now we will create a Fragment displaying a map that shows the users (mock) location.

 Create a class `ShowUserLocationFragment` that inherits from `Fragment`.
 ***/
public class ShowUserLocationFragment extends Fragment {

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    GoogleMap mGoogleMap;
    MapControl mMapControl;

    /***
     Add a map fragment
     ***/
    SupportMapFragment mMapFragment;

    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //

    public ShowUserLocationFragment()
    {
        // Required empty public constructor
    }

    @NonNull
    public static ShowUserLocationFragment newInstance()
    {
        return new ShowUserLocationFragment();
    }


    //region FRAGMENT LIFECYCLE
    @Override
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_map, container, false );
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
        /***
         In the 'onDestroyView' method, we need to free the MapsIndoors PositionProvider
         ***/
        MapsIndoors.setPositionProvider( null );
        //

        super.onDestroyView();
    }
    //endregion


    private void setupView( View rootView )
    {
        FragmentManager fm = getChildFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById( R.id.mapfragment );

        mMapFragment.getMapAsync( mOnMapReadyCallback );
    }

    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 13.0f ) );

            setupMapsIndoors();
        }
    };

    void setupMapsIndoors()
    {
        /***
         Set the API key to the MI solution
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
         Instantiate the mapControl object
         ***/
        mMapControl = new MapControl( getActivity() );
        mMapControl.setGoogleMap(mGoogleMap, mMapFragment.getView());

        /***
         * Create an instance of the 'DemoPositionProvider' that we defined previously
         ***/
        DemoPositionProvider demoPositionProvider = new DemoPositionProvider();

        /***
         * Assign the `DemoPositionProvider` instance to the `MapsIndoors.positionProvider` by calling the 'MapsIndoors.setPositionProvider'
         ***/
        MapsIndoors.setPositionProvider( demoPositionProvider );

        /***
         * Tell the mapControl to show the users location
         ***/
        mMapControl.showUserPosition( true );

        /***
         Init the mapControl object
         ***/
        mMapControl.init( miError -> {

            if( miError == null )
            {
                final Activity context = getActivity();
                if( context != null )
                {
                    mMapControl.selectFloor( 1 );
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );

                    /***
                     * Start positioning
                     ***/
                    demoPositionProvider.startPositioning(null);
                }
            }
        });
    }
}
