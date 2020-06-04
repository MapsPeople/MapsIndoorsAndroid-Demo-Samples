package com.mapsindoors.customfloorselectordemo;

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
import com.mapsindoors.customfloorselectordemo.floorselectorcomponent.MapFloorSelector;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;



public class CustomFloorSelectorFragment extends Fragment {

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );

    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;
    MapFloorSelector mMapFloorSelector;


    public CustomFloorSelectorFragment()
    {
        // Required empty public constructor
    }

    @NonNull
    public static CustomFloorSelectorFragment newInstance()
    {
        return new CustomFloorSelectorFragment();
    }


    //region FRAGMENT LIFECYCLE
    @Override
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_map_with_floor_selector, container, false );
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

        mMapFloorSelector = rootView.findViewById( R.id.mp_floor_selector );

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

        mMapControl.setFloorSelector( mMapFloorSelector );

        mMapControl.init( miError -> {

            if( miError == null )
            {
                final Activity _context = getActivity();
                if( _context != null )
                {
                    //setting the floor level programmatically
                    mMapControl.selectFloor( 20 );

                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
                }
            }
        });
    }
}
