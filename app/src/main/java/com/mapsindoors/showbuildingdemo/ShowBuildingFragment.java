package com.mapsindoors.showbuildingdemo;

import android.app.Activity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.models.Building;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ShowBuildingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowBuildingFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );


    public ShowBuildingFragment()
    {
        // Required empty public constructor
    }

    public static ShowBuildingFragment newInstance()
    {
        return new ShowBuildingFragment();
    }


    //region FRAGMENT

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        setupView(rootView);

        return rootView;
    }

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
    //endregion


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
                    context.runOnUiThread( () -> {

                        mMapControl.selectFloor( 1 );

                        Building currentBuilding = mMapControl.getCurrentBuilding();

                        if( currentBuilding != null )
                        {
                            LatLngBounds latLngBounds = currentBuilding.getLatLngBoundingBox();

                            if( (mGoogleMap != null) && (latLngBounds != null) )
                            {
                                mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngBounds( latLngBounds, 10 ) );
                            }
                        }
                    });
                }
            }
        });
    }
}
