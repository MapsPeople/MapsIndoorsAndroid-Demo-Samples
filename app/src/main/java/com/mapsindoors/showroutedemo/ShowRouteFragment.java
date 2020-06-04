package com.mapsindoors.showroutedemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MPDirectionsRenderer;
import com.mapsindoors.mapssdk.MPRoutingProvider;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.RoutingProvider;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.TravelMode;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ShowRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowRouteFragment extends Fragment
{

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );

    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;
    RoutingProvider mRoutingProvider ;
    MPDirectionsRenderer mRoutingRenderer;



    public ShowRouteFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static ShowRouteFragment newInstance() {
        return new ShowRouteFragment();
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


    private void setupView( View rootView) {

        final FragmentManager fm = getChildFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);

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

        mRoutingProvider = new MPRoutingProvider();

        setupRouteRenderer( context );

        mMapControl.init( miError -> {

            if( miError == null )
            {
                final Activity _context = getActivity();
                if( _context != null )
                {
                    // Setting the floor level programmatically
                    mMapControl.selectFloor( 20 );

                    // Make the route
                    //mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 19f ) );

                    // Wait a bit before create/render the route
                    new Handler( _context.getMainLooper() ).postDelayed( this::routing, 2000 );
                }
            }
        });
    }

    void setupRouteRenderer( @NonNull Context context )
    {
        mRoutingRenderer = new MPDirectionsRenderer( context, mGoogleMap, mMapControl, null );

        mRoutingRenderer.setPrimaryColor( ContextCompat.getColor( context, R.color.colorPrimary ) );
        mRoutingRenderer.setAccentColor( ContextCompat.getColor( context, R.color.colorAccent ) );
        mRoutingRenderer.setTextColor( ContextCompat.getColor( context, R.color.white ) );

        mRoutingRenderer.setAnimated( true );
    }

    void routing()
    {
        mRoutingProvider.setOnRouteResultListener( ( route, error ) -> {
            if( route != null )
            {
                mRoutingRenderer.setRoute( route );

                final Activity activity  = getActivity();
                if( activity != null )
                {
                    activity.runOnUiThread( () -> {
                        mRoutingRenderer.setRouteLegIndex( 0 );
                    });
                }
            } else
            {
                // Can't get a route between the given points
            }
        });

        final Point origin = new Point( 57.057917, 9.950361, 1 );
        final Point destination = new Point( 57.058038, 9.950509, 1 );

        mRoutingProvider.setTravelMode( TravelMode.WALKING );
        mRoutingProvider.query( origin, destination );
    }
}
