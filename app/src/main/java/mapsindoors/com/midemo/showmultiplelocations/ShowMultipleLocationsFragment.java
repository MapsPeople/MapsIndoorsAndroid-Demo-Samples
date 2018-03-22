package mapsindoors.com.midemo.showmultiplelocations;

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
import com.mapspeople.Location;
import com.mapspeople.LocationQuery;
import com.mapspeople.MPLocationsProvider;
import com.mapspeople.MapControl;
import com.mapspeople.OnLocationsReadyListener;
import com.mapspeople.errors.MIError;

import java.util.List;

import mapsindoors.com.midemo.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ShowMultipleLocationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowMultipleLocationsFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //query objects
    LocationQuery mLocationQuery;
    LocationQuery.Builder mLocationQueryBuilder;

    public ShowMultipleLocationsFragment() {
        // Required empty public constructor
    }


    public static ShowMultipleLocationsFragment newInstance(String param1, String param2) {
        ShowMultipleLocationsFragment fragment = new ShowMultipleLocationsFragment();

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
        return inflater.inflate( R.layout.fragment_show_location, container, false );
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

        mMapControl = new MapControl(getActivity(), mMapFragment, mGoogleMap);

        mMapControl.init( miError -> {
            // after the map control is initialized we can
            queryLocation();

            if( getActivity() != null )
            {
                getActivity().runOnUiThread( () -> {
                    mMapControl.selectFloor( 1 );
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 18f ) );

                } );
            }
        } );
    }


    MPLocationsProvider mLocationsProvider;


    void queryLocation(){

        mLocationsProvider = new MPLocationsProvider();

        mLocationQueryBuilder =     new LocationQuery.Builder();

        // init the query builder, in this case we will query the coffee machine in our office
        mLocationQueryBuilder.
                setQuery("Toilet").
                setOrderBy( LocationQuery.NO_ORDER ).
                setFloor(1).
                setMaxResults(50);
        // Build the query
        mLocationQuery = mLocationQueryBuilder.build();
        // Query the data
        mLocationsProvider.getLocationsAsync( mLocationQuery, mSearchLocationsReadyListener );


    }

    OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener()
    {
        @Override
        public void onLocationsReady( @Nullable List< Location > locations, @Nullable MIError error )
        {

            if( locations != null && locations.size() != 0 )
            {

                mMapControl.displaySearchResults( locations, true );

            }
        }

    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
