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
import com.mapsindoors.mapssdk.OnResultReadyListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/***
 ---
 title: Creating your own Location Source - Part 2
 ---

 > Note! This document describes a pre-release feature. We reserve the right to change this feature and the corresponding interfaces without further notice. Any mentioned SDK versions are not intended for production use.

 This is part 2 of the tutorial of building a custom Location Source, representing locations of people. [In Part 1 we created the Location Source](locationdatasourcespeoplelocationdatasource). Now we will create a Fragment displaying a map that shows the mocked people locations on top of a MapsIndoors map.

 Create a class `LocationDataSourcesFragment` that extends `Fragment`.
 ***/
public class LocationDataSourcesFragment extends Fragment
{
    public static final String POI_TYPE_1               = "PoiType1";
    public static final String POI_TYPE_2               = "PoiType2";
    public static final String POI_TYPE_AVAILABLE       = "LocationAvailable";
    public static final String POI_TYPE_NOT_AVAILABLE   = "LocationNotAvailable";

    public static final int    POI_GROUP_ID_1   = 1;
    public static final int    POI_GROUP_ID_2   = 2;
    public static final int    POI_GROUP_ID_3   = 3;

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap  mGoogleMap;

    /***
     Add other needed views for this example
     ***/
    SupportMapFragment mMapFragment;

    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //

    @Nullable Set<MPLocationSource>         locationDataSources;
    @Nullable MPLocationSource              mpLocationSource;
    @Nullable List<ExternalPOIDataSource>   externalPOIDataSources;


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
        return inflater.inflate( R.layout.fragment_location_data_sources, container, false );
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

            if( externalPOIDataSources != null ) {
                for( final ExternalPOIDataSource s : externalPOIDataSources ) {
                    s.stopMockingPOIsPositions();
                }
            }

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
        if( getActivity() == null )
        {
            return;
        }

        MapsIndoors.onApplicationTerminate();

        MapsIndoors.initialize( DemoApplication.getInstance(), getString( R.string.mi_api_key ) );
//
//        // Your Google Maps API key
//        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );

        setupLocationDataSources( error -> setupMapControl() );
    }

    void setupLocationDataSources( @NonNull OnResultReadyListener listener )
    {
        locationDataSources = new HashSet<>( 2 );

        // Data coming from MapsPeople's servers.
        mpLocationSource = MapsIndoors.getMapsIndoorsLocationSource();
        locationDataSources.add( mpLocationSource );

        // A custom source of dynamic POIs where only their position changes over time
        externalPOIDataSources = new ArrayList<>();
        externalPOIDataSources.add(
                new ExternalPOIDataSource(
                        ExternalPOIDataSource.DEMO_MODE_MOVING_POIS,
                        ExternalPOIDataSource.SOURCE_ID + externalPOIDataSources.size()
                )
        );

        // A custom source where only the POI display rule type changes over time
        externalPOIDataSources.add(
                new ExternalPOIDataSource(
                        ExternalPOIDataSource.DEMO_MODE_ANIMATED_TYPES,
                        ExternalPOIDataSource.SOURCE_ID + externalPOIDataSources.size()
                )
        );

        // A custom source where only the POI icon and tint color changes over time
        externalPOIDataSources.add(
                new ExternalPOIDataSource(
                        ExternalPOIDataSource.DEMO_MODE_ANIMATED_MARKER_ICONS_AND_COLORS,
                        ExternalPOIDataSource.SOURCE_ID + externalPOIDataSources.size()
                )
        );

        locationDataSources.addAll( externalPOIDataSources );

        /***
         Set the location sources to `ExternalPOIDataSource` and `MapsIndoorsLocationSource`
         ***/
        MapsIndoors.setLocationSources( locationDataSources.toArray( new MPLocationSource[0] ), error -> {

            final FragmentActivity context = getActivity();
            if( context != null ) {
                context.runOnUiThread( () -> {
                    if( error != null ) {
                        Toast.makeText( context, "Error occurred when setting the Datasources", Toast.LENGTH_SHORT ).show();
                    }

                    listener.onResultReady( error );
                } );
            }
        } );
    }

    void setupMapControl()
    {
        final Activity activityContext = getActivity();
        if( (activityContext == null) || (mMapFragment == null) ) {
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
        final LocationDisplayRule poiDisplayRule = new LocationDisplayRule.Builder( POI_TYPE_1 ).
                setBitmapDrawableIcon( R.drawable.generic_user ).
                setVisible( true ).
                setShowLabel( false ).
                setZoomLevelOn( 18 ).
                setLocationClusterId( POI_GROUP_ID_1 ).
                setDisplayRank( 1 ).
                build();

        mMapControl.addDisplayRule( poiDisplayRule );

        //
        setupOtherDisplayRules();

        //
        MapsIndoors.addLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );

        mMapControl.setLocationClusteringEnabled( false );

        /***
         Init the MapControl object which will sync data
         ***/
        mMapControl.init( null );
    }

    void setupOtherDisplayRules()
    {
        final List<LocationDisplayRule> dispRules = new ArrayList<>();

        //
        {
            final LocationDisplayRule dr = new LocationDisplayRule.Builder( POI_TYPE_2 ).
                    setVectorDrawableIcon( R.drawable.ic_battery_60_black_24dp ).
                    setVisible( true ).
                    setShowLabel( false ).
                    setZoomLevelOn( 16 ).
                    setLocationClusterId( POI_GROUP_ID_2 ).
                    setDisplayRank( 1 ).
                    build();
            dispRules.add( dr );
        }

        //
        {
            final LocationDisplayRule dr = new LocationDisplayRule.Builder( POI_TYPE_AVAILABLE ).
                    setVectorDrawableIcon( R.drawable.ic_whatshot_black_24dp ).
                    setTint( 0xff2DD855 ).
                    setVisible( true ).
                    setShowLabel( false ).
                    setZoomLevelOn( 17 ).
                    setLocationClusterId( POI_GROUP_ID_3 ).
                    setDisplayRank( 1 ).
                    build();
            dispRules.add( dr );
        }

        //
        {
            final LocationDisplayRule dr = new LocationDisplayRule.Builder( POI_TYPE_NOT_AVAILABLE ).
                    setVectorDrawableIcon( R.drawable.ic_whatshot_black_24dp, 32,32 ).
                    setTint( 0xffFF3700 ).
                    setVisible( true ).
                    setShowLabel( false ).
                    setZoomLevelOn( 17 ).
                    setLocationClusterId( POI_GROUP_ID_3 ).
                    setDisplayRank( 1 ).
                    build();
            dispRules.add( dr );
        }

        mMapControl.addDisplayRules( dispRules );
    }

    final MPLocationSourceOnStatusChangedListener locationSourceOnStatusChangedListener = new MPLocationSourceOnStatusChangedListener() {
        @Override
        public void onStatusChanged( @NonNull MPLocationSourceStatus status, int sourceId )
        {
            if( status == MPLocationSourceStatus.AVAILABLE ) {
                final Activity context = getActivity();
                if( context != null ) {
                    context.runOnUiThread( () -> {

                        if( externalPOIDataSources != null ) {
                            for( final ExternalPOIDataSource s : externalPOIDataSources ) {
                                s.startMockingPOIsPositions();
                            }
                        }

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
