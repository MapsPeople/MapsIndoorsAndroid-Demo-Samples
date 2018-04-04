package com.mapsindoors.searchmapdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.MapControl;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SearchMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMapFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;
    Button searchButton;
    Location locationToSelect = null;

    private OnFragmentInteractionListener mListener;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //query objects


    public SearchMapFragment() {
        // Required empty public constructor
    }

    public static SearchMapFragment newInstance() {

        SearchMapFragment fragment = new SearchMapFragment();

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
        return inflater.inflate(R.layout.fragment_search_map, container, false);
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
        searchButton = rootView.findViewById(R.id.search_button);

        searchButton.setOnClickListener( v -> mListener.onSearchButtonClick() );

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



        mMapControl.init( miError -> {

            if( getActivity() != null )
            {
                getActivity().runOnUiThread(() -> {

                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );

                    if(locationToSelect != null){
                        mMapControl.selectLocation(locationToSelect);
                        locationToSelect = null;
                    } else {
                        mMapControl.selectFloor( 1 );
                    }

                });
            }

        });
    }





    @Override
    public void onDetach() {
        super.onDetach();

    }


    public interface OnFragmentInteractionListener {
        void onSearchButtonClick();
    }

    public void selectLocation(Location loc){

        locationToSelect = loc;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

}
