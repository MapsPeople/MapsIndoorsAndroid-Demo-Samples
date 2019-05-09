package com.mapsindoors.locationdatasource;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.mapsindoors.DemoApplication;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.LocationDisplayRule;
import com.mapsindoors.mapssdk.MPLocationSource;
import com.mapsindoors.mapssdk.MPLocationSourceOnStatusChangedListener;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;

import java.util.HashSet;
import java.util.Set;

/***
 ---
 title: Creating your own Location Source - Part 2
 ---

 > Note! This document describes a pre-release feature. We reserve the right to change this feature and the corresponding interfaces without further notice. Any mentioned SDK versions are not intended for production use.

 This is part 2 of the tutorial of building a custom Location Source, representing locations of people. [In Part 1 we created the Location Source](locationdatasourcespeoplelocationdatasource). Now we will create a Fragment displaying a map that shows the mocked people locations on top of a MapsIndoors map.

 Create a class `LocationDataSourcesFragment` that extends `Fragment`.
 ***/

public class LocationDataSourcesFragment  extends Fragment {


    final String PEOPLE_TYPE = "People";
     final int POI_GROUP_ID = 1;

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap mGoogleMap;

    /***
     Add other needed views for this example
     ***/

    SupportMapFragment mMapFragment;

    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //

    Set<MPLocationSource> locationDataSources;
    MPLocationSource mpLocationSource;
    PeopleDataSource peopleDataSource;



    public LocationDataSourcesFragment()
    {
        // Required empty public constructor
    }

    @NonNull
    public static LocationDataSourcesFragment newInstance()
    {
        return new LocationDataSourcesFragment();
    }


    //region FRAGMENT LIFECYCLE
    @Override
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_show_location_details, container, false );
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
        if( mMapControl != null ) {
            MapsIndoors.removeLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );
            mMapControl.onDestroy();
        }

        super.onDestroyView();
    }
    //endregion


    /***
     Setup the needed views for this example
     ***/
    private void setupView( View rootView )
    {
        FragmentManager fm = getChildFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById( R.id.mapfragment );

        mMapFragment.getMapAsync( mOnMapReadyCallback );
    }

    /***
     Once the map is ready move the camera to the venue location and call the setupMapsIndoors
     ***/
    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 13.0f ) );

            setupMapsIndoors();
        }
    };

    /***
     Setup MapsIndoors
     ***/
    void setupMapsIndoors()
    {
        MapsIndoors.onApplicationTerminate();

        MapsIndoors.initialize( DemoApplication.getInstance(), getString( R.string.mi_api_key ) );

        // Your Google Maps API key
        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );

        if( getActivity() == null )
        {
            return;
        }

        setupLocationDataSources();
    }

    void setupLocationDataSources()
    {
        locationDataSources = new HashSet<>( 2 );

        mpLocationSource = MapsIndoors.getMapsIndoorsLocationSource();
        locationDataSources.add( mpLocationSource );

        peopleDataSource = new PeopleDataSource( PEOPLE_TYPE );
        locationDataSources.add( peopleDataSource );

        /***
         Set the location sources to `PeopleDataSource` and `MapsIndoorsLocationSource`
         ***/
        MapsIndoors.setLocationSources( locationDataSources.toArray( new MPLocationSource[0] ), error -> {

            final FragmentActivity context = getActivity();
            if( context != null ) {
                context.runOnUiThread( () -> {
                    if( error == null ) {
                        setupMapControl();
                    } else {
                        Toast.makeText( context, "Error occurred when setting the Datasources", Toast.LENGTH_SHORT ).show();
                    }
                } );
            }
        } );
    }

    void setupMapControl()
    {
        final Activity activityContext = getActivity();
        if( activityContext == null ) {
            return;
        }

        /***
         Instantiate and init the MapControl object which will sync data
         ***/
        mMapControl = new MapControl( activityContext );
        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );

        /***
         Setup a display setting that refers to the type of locations that your location source operates with.
         ***/
        final LocationDisplayRule peopleTypeDisplayRule = new LocationDisplayRule.Builder( PEOPLE_TYPE ).
                setBitmapDrawableIcon( R.drawable.generic_user ).
                setVisible( true ).
                setShowLabel( false ).
                setZoomLevelOn( 18 ).
                setLocationClusterId( POI_GROUP_ID ).
                setDisplayRank( 1 ).
                build();

        mMapControl.addDisplayRule( peopleTypeDisplayRule );

        MapsIndoors.addLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );

        /***
         Init the MapControl object which will sync data
         ***/
        mMapControl.init( null );
    }

    final MPLocationSourceOnStatusChangedListener locationSourceOnStatusChangedListener = new MPLocationSourceOnStatusChangedListener()
    {
        @Override
        public void onStatusChanged( @NonNull MPLocationSourceStatus status, int sourceId )
        {
            if( status == MPLocationSourceStatus.AVAILABLE ) {
                final Activity context = getActivity();
                if( context != null ) {
                    context.runOnUiThread( () -> {

                        /***
                         Select a floor and animate the camera to the venue position
                         ***/
                        mMapControl.selectFloor( 1 );
                        mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
                    } );
                }
            }
        }
    };

}
