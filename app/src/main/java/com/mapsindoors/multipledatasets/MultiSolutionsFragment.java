package com.mapsindoors.multipledatasets;

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
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MultiSolutionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MultiSolutionsFragment extends Fragment
{


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;


    final        LatLng MAPSPEOPLE_CORPORATE_HQ_LOCATION       = new LatLng( 57.05813067, 9.95058065 );
    static final float  MAPSPEOPLE_CORPORATE_HQ_ZOOM           = 19f;
    static final int    MAPSPEOPLE_CORPORATE_HQ_SELECTED_FLOOR = 1;

    final        LatLng AAU_CREATE_BUILDING_LOCATION       = new LatLng( 57.04807056, 9.92998432 );
    static final float  AAU_CREATE_BUILDING_ZOOM           = 18f;
    static final int    AAU_CREATE_BUILDING_SELECTED_FLOOR = 0;

    LatLng selectedCameraPosition = MAPSPEOPLE_CORPORATE_HQ_LOCATION;
    float  selectedCameraCloseUpZoom = MAPSPEOPLE_CORPORATE_HQ_ZOOM;
    int selectedFloorIndex;


    public MultiSolutionsFragment()
    {
        // Required empty public constructor
    }

    public static MultiSolutionsFragment newInstance()
    {
        return new MultiSolutionsFragment();
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

        if( MapsIndoors.getAPIKey().equalsIgnoreCase( "57e4e4992e74800ef8b69718" ) )
        {
            selectedCameraPosition = MAPSPEOPLE_CORPORATE_HQ_LOCATION;
            selectedCameraCloseUpZoom = MAPSPEOPLE_CORPORATE_HQ_ZOOM;
            selectedFloorIndex = MAPSPEOPLE_CORPORATE_HQ_SELECTED_FLOOR;
        }
        else if( MapsIndoors.getAPIKey().equalsIgnoreCase( "5667325fc1843a06bca1dd2b" ) )
        {
            selectedCameraPosition = AAU_CREATE_BUILDING_LOCATION;
            selectedCameraCloseUpZoom = AAU_CREATE_BUILDING_ZOOM;
            selectedFloorIndex = AAU_CREATE_BUILDING_SELECTED_FLOOR;
        }

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

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);

        mMapFragment.getMapAsync( mOnMapReadyCallback );
    }

    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( selectedCameraPosition, 13.0f ) );

            setupMapsIndoors();
        }
    };

    void setupMapsIndoors()
    {
        if( getActivity() == null )
        {
            return;
        }

        mMapControl = new MapControl( getActivity() );
        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );

        mMapControl.init( miError -> {

            if( miError == null )
            {
                Activity context = getActivity();
                if( context != null )
                {
                    context.runOnUiThread( () -> {

                        mMapControl.selectFloor( 1 );
                        mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( selectedCameraPosition, selectedCameraCloseUpZoom ) );
                    });
                }
            }
        });
    }
}
