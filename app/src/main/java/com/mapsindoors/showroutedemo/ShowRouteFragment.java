package com.mapsindoors.showroutedemo;

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
import com.mapspeople.MPDirectionsRenderer;
import com.mapspeople.MPRoutingProvider;
import com.mapspeople.MapControl;
import com.mapspeople.MapsIndoors;
import com.mapspeople.OnRouteResultListener;
import com.mapspeople.RoutingProvider;
import com.mapspeople.errors.MIError;
import com.mapspeople.models.Point;
import com.mapspeople.models.Route;

import com.mapsindoors.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ShowRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowRouteFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;
    RoutingProvider mRoutingProvider ;
    MPDirectionsRenderer mRoutingRenderer;
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //query objects


    public ShowRouteFragment() {
        // Required empty public constructor
    }

    public static ShowRouteFragment newInstance() {
        ShowRouteFragment fragment = new ShowRouteFragment();

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
        mRoutingProvider  = new MPRoutingProvider();

        mMapControl = new MapControl( getActivity(), mMapFragment, mGoogleMap );

        mRoutingRenderer = new MPDirectionsRenderer(getContext(),null,mGoogleMap);


        mMapControl.init( miError -> {

            if( getActivity() != null )
            {
                getActivity().runOnUiThread(() -> {
                    //setting the floor level programatically
                    mMapControl.selectFloor( 1 );

                    // make the route
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 19f ) );

                });
            }
        });


        routing();

    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }



    void routing(){
        mRoutingProvider.setOnRouteResultListener(new OnRouteResultListener() {
            @Override
            public void onRouteResult(@Nullable Route route, @Nullable MIError error) {
                mRoutingRenderer.setRoute(route);
               // mRoutingRenderer.setAlpha(255);
                getActivity().runOnUiThread(() -> {
                    mRoutingRenderer.setRouteLegIndex(0);
                });

            }
        });

        Point origin = new Point(57.057917,9.950361 );
        Point destination = new Point(57.058038,9.950509 ) ;


        mRoutingProvider.query( origin, destination);


    }


}
