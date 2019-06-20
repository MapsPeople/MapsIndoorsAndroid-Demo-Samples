package com.mapsindoors.showlocationdemo;

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
import com.mapsindoors.BuildConfig;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationSourceOnStatusChangedListener;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;

import java.util.List;

public class ShowLocationFragment extends Fragment
{


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );



    public ShowLocationFragment()
    {
        // Required empty public constructor
    }

    @NonNull
    public static ShowLocationFragment newInstance()
    {
        return new ShowLocationFragment();
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
        if( mMapControl != null ) {
            MapsIndoors.removeLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );
            mMapControl.onDestroy();
        }

        super.onDestroyView();
    }
    //endregion


    private void setupView( View rootView )
    {
        final FragmentManager fm = getChildFragmentManager();

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
        final Activity context = getActivity();

        if( (context == null) || (mMapFragment == null) || (mMapFragment.getView() == null) )
        {
            return;
        }

        if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key ) ) )
        {
            MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );
        }

        mMapControl = new MapControl( context );
        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );

        //
        MapsIndoors.addLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );

        // Initialize MapControl. In this case we only need to know when locations are ready
        mMapControl.init( null );
        //
    }

    void queryLocation()
    {
        /*** Init the query builder and build a query, in this case we will query for coffee machines ***/
        MPQuery query = new MPQuery.Builder().
                setQuery("coffee machine").
                build();

        /*** Init the filter builder and build a filter, the criteria in this case we want 1 coffee machine from the 1st floor ***/
        MPFilter filter = new MPFilter.Builder().
                setFloorIndex(1).
                build();

        /*** Query the data ***/
        MapsIndoors.getLocationsAsync( query, filter, ( locs, err ) -> {
            if( locs != null && locs.size() != 0 ) {
                mMapControl.displaySearchResults( locs, true );
            }
        } );
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

//                        final List<MPLocation> locations = MapsIndoors.getLocations();
//                        if( locations.size() == 0 ) {
//                            if(BuildConfig.DEBUG){}
//                        }

                        //
                        mMapControl.selectFloor( 1 );

                        //
                        queryLocation();

                        //mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 21f ) );
                    } );
                }
            }
        }
    };
}
