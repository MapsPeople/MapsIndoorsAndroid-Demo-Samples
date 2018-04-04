package com.mapsindoors.changedisplaysettingdemo;

import android.content.Context;
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
import com.mapsindoors.mapssdk.LocationDisplayRule;
import com.mapsindoors.mapssdk.MapControl;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ChangeDisplaySettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeDisplaySettingsFragment extends Fragment {

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );

    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    LocationDisplayRule myCustomMeetingRoomDisplayRule;

    //query objects


    public ChangeDisplaySettingsFragment() {
        // Required empty public constructor
    }

    public static ChangeDisplaySettingsFragment newInstance() {
        ChangeDisplaySettingsFragment fragment = new ChangeDisplaySettingsFragment();

        return fragment;
    }


    //region FRAGMENT LIFECYCLE
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

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

    private void setupView( View rootView) {

        FragmentManager fm = getChildFragmentManager();



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

    void setupMapsIndoors() {

        mMapControl = new MapControl( getActivity(), mMapFragment, mGoogleMap );

        myCustomMeetingRoomDisplayRule = new LocationDisplayRule.Builder("MeetingRoom").
                setIcon( R.drawable.ic_flight_takeoff_black_24dp, 20, 20 ).
                setZoomLevelOn( 16 ).
                setShowLabel( false ).
                setTint( 0x7Fef0055 ).
                setVisible( true ).
                build();

        mMapControl.addDisplayRule( myCustomMeetingRoomDisplayRule );

        mMapControl.setOnMarkerClickListener( marker -> {

            final Location loc = mMapControl.getLocation( marker );
            if( loc != null )
            {
                marker.showInfoWindow();

                loc.setDisplayRule( myCustomMeetingRoomDisplayRule );
            }

            return true;
        });

        mMapControl.init( miError -> {

            if( getActivity() != null )
            {
                getActivity().runOnUiThread(() -> {
                    mMapControl.selectFloor( 1 );
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );



                });
            }
        });
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }





}
