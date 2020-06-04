package com.mapsindoors.showbuildingdemo;

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
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.Building;

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

    @NonNull
    public static ShowBuildingFragment newInstance()
    {
        return new ShowBuildingFragment();
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
                    mMapControl.selectFloor( 20 );

                    final Building currentBuilding = mMapControl.getCurrentBuilding();

                    if( currentBuilding != null )
                    {
                        final LatLngBounds latLngBounds = currentBuilding.getLatLngBoundingBox();

                        if( (mGoogleMap != null) && (latLngBounds != null) )
                        {
                            mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngBounds( latLngBounds, 10 ) );
                        }
                    }
                }
            }
        });
    }
}
