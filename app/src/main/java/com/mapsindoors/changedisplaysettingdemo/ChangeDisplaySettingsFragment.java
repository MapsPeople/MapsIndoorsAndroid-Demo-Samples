package com.mapsindoors.changedisplaysettingdemo;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;


public class ChangeDisplaySettingsFragment extends Fragment {

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );

    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;


    public ChangeDisplaySettingsFragment()
    {
        // Required empty public constructor
    }

    public static ChangeDisplaySettingsFragment newInstance()
    {
        return new ChangeDisplaySettingsFragment();
    }


    //region FRAGMENT LIFECYCLE

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
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

        mMapControl = new MapControl( getActivity() );
        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );

        changeDisplayRules();

        mMapControl.init( miError -> {

            if( miError == null )
            {
                Activity context = getActivity();
                if( context != null )
                {
                    context.runOnUiThread(() -> {
                        mMapControl.selectFloor( 0 );
                        mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
                    });
                }
            }
        });
    }

    void changeDisplayRules()
    {
        final LocationDisplayRule rule = new LocationDisplayRule.Builder( "MeetingRoom" ).
                setIcon( R.drawable.archive, 20, 20 ).
                setZoomLevelOn( 16 ).
                setVisible( true ).
                build();

        mMapControl.addDisplayRule( rule );
    }
}
