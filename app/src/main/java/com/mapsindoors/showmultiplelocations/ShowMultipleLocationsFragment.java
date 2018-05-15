package com.mapsindoors.showmultiplelocations;

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
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.LocationQuery;
import com.mapsindoors.mapssdk.MPLocationsProvider;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.errors.MIError;

import java.util.List;

public class ShowMultipleLocationsFragment extends Fragment {

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap mGoogleMap;

    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    /***
     The query object and the querry builder
     ***/
    LocationQuery mLocationQuery;
    LocationQuery.Builder mLocationQueryBuilder;

    SupportMapFragment mMapFragment;


    public ShowMultipleLocationsFragment()
    {
        // Required empty public constructor
    }

    public static ShowMultipleLocationsFragment newInstance()
    {
        return new ShowMultipleLocationsFragment();
    }


    //region FRAGMENT LIFECYCLE

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key ) ) )
        {
            MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );
        }

        if( getActivity() == null )
        {
            return;
        }

        mMapControl = new MapControl( getActivity(), mMapFragment, mGoogleMap );
        mMapControl.init( miError -> {

            if( miError == null )
            {
                Activity context = getActivity();
                if( context != null )
                {
                    queryLocation();

                    context.runOnUiThread( () -> {
                        mMapControl.selectFloor( 1 );
                        mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 18f ) );

                    } );
                }
            }
        });
    }


    MPLocationsProvider mLocationsProvider;


    void queryLocation()
    {
        mLocationsProvider = new MPLocationsProvider();

        mLocationQueryBuilder = new LocationQuery.Builder();

        /*** init the query builder, in this case we will query for all to toilets ***/
        mLocationQueryBuilder.
                setQuery("Toilet").
                setOrderBy( LocationQuery.NO_ORDER ).
                setFloor(1).
                setMaxResults(50);

        /*** Build the query ***/
        mLocationQuery = mLocationQueryBuilder.build();

        /*** Query the data ***/
        mLocationsProvider.getLocationsAsync( mLocationQuery, mSearchLocationsReadyListener );
    }

    /*** Show search on map When the 'OnLocationsReadyListener' is called ***/
    OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener()
    {
        @Override
        public void onLocationsReady( @Nullable List<Location> locations, @Nullable MIError error )
        {
            if( locations != null && locations.size() != 0 )
            {
                /* Display the locations on the map */
                mMapControl.displaySearchResults( locations, true, 40 );
            }
        }
    };
}
