package com.mapsindoors.changedisplayruledemo;

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
import com.mapsindoors.mapssdk.LocationDisplayRule;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;


public class ChangeDisplayRuleFragment extends Fragment {

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );

    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;


    public ChangeDisplayRuleFragment()
    {
        // Required empty public constructor
    }

    @NonNull
    public static ChangeDisplayRuleFragment newInstance()
    {
        return new ChangeDisplayRuleFragment();
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



        mMapControl.init( miError -> {

            if( miError == null )
            {

                final Activity _context = getActivity();
                if( _context != null )
                {
                    mMapControl.selectFloor( 0 );
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
                }
                changeDisplayRules();
            }
        });
    }

    /**
     * Shows how to query a specific location and applying a {@link LocationDisplayRule} to it.
     * This example changes the south-west "Wrist meeting room 1"s icon to a "flame" instead of the
     * default meeting room icon.
     */
    void changeDisplayRules()
    {
        final LocationDisplayRule rule = new LocationDisplayRule.Builder( "MeetingRoomRule" ).
                setBitmapDrawableIcon( R.drawable.ic_whatshot_black_24dp, 20, 20 ).
                setZoomLevelOn( 16 ).
                setVisible( true ).
                build();

        MPQuery query = new MPQuery.Builder().setQuery("Wrist meeting room 1").build();
        MapsIndoors.getLocationsAsync(query, null, (list, miError) -> {
            if(list != null && list.size() > 0) {
                MPLocation location = list.get(0);
                mMapControl.setDisplayRule(rule, location);
            }
        });

    }
}
