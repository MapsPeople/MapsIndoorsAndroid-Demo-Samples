package com.mapsindoors.customfloorselectordemo;

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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.mapspeople.FloorSelectorType;
import com.mapspeople.MapControl;

import com.mapsindoors.R;
import com.mapsindoors.customfloorselectordemo.floorselectorcomponent.MapFloorSelector;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CustomFloorSelectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomFloorSelectorFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;
    MapFloorSelector mMapFloorSelector;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //query objects


    public CustomFloorSelectorFragment() {
        // Required empty public constructor
    }

    public static CustomFloorSelectorFragment newInstance(String param1, String param2) {
        CustomFloorSelectorFragment fragment = new CustomFloorSelectorFragment();

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
        return inflater.inflate(R.layout.fragment_map_with_floor_selector, container, false);
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

        mMapFloorSelector = rootView.findViewById(R.id.mp_floor_selector);

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
        mMapControl.setFloorSelector(mMapFloorSelector);
        mMapControl.setFloorSelectorType( FloorSelectorType.ONLYCURRENTBUILDING );

        mMapControl.addOnCameraIdleListener(() -> {
            CameraPosition pos = mGoogleMap.getCameraPosition();
            float cameraZoom = pos.zoom;

            mMapFloorSelector.onMapZoomLevelChanged(cameraZoom);
        });



        mMapControl.init( miError -> {

            if( getActivity() != null )
            {
                getActivity().runOnUiThread(() -> {
                    //setting the floor level programatically
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
