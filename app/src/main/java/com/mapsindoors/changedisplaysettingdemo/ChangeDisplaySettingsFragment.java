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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mapspeople.Location;
import com.mapspeople.LocationDisplayRule;
import com.mapspeople.LocationDisplayRules;
import com.mapspeople.LocationPropertyNames;
import com.mapspeople.MapControl;

import com.mapsindoors.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ChangeDisplaySettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeDisplaySettingsFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //query objects


    public ChangeDisplaySettingsFragment() {
        // Required empty public constructor
    }

    public static ChangeDisplaySettingsFragment newInstance(String param1, String param2) {
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

    void  setupMapsIndoors() {

        mMapControl = new MapControl( getActivity(), mMapFragment, mGoogleMap );

        LocationDisplayRules displayRules = new LocationDisplayRules();
        LocationDisplayRule.Builder builder = new LocationDisplayRule.Builder("MeetingRoom").
                setVectorDrawableIcon( R.drawable.ic_flight_takeoff_black_24dp, 20, 20 ).
                setZOn(16).
                setShowLabel(false).
                setTint( 0x7Fef0055 ).
                setVisible(true);

        displayRules.add(builder.build());

        mMapControl.addDisplayRules(displayRules);

        mMapControl.setOnMarkerClickListener( marker -> {

            final Location loc = mMapControl.getLocation( marker );
            if( loc != null )
            {
                marker.showInfoWindow();

                //LocationDisplayRule cafeDispRule = myMapControl.getDisplayRules().getRule( "<cafe>" );
                LocationDisplayRule cafeDispRule = mMapControl.getDisplayRules().getRule( "MeetingRoom" );


                loc.setDisplayRule( cafeDispRule );
                loc.setVisible( true );
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
