package mapsindoors.com.midemo.showlocationdemo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import com.mapspeople.MapsIndoors;
import com.mapspeople.OnLoadingDataReadyListener;
import com.mapspeople.OnLocationsReadyListener;
import com.mapspeople.errors.MIError;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mapsindoors.com.midemo.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ShowLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowLocationFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //querry objects
    LocationQuery mLocationQuerry;
    LocationQuery.Builder mLocationQueryBuilder;

    public ShowLocationFragment() {
        // Required empty public constructor
    }


    public static ShowLocationFragment newInstance(String param1, String param2) {
        ShowLocationFragment fragment = new ShowLocationFragment();

        return fragment;
    }

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
        View rootView = inflater.inflate(R.layout.fragment_show_location, container, false);
        setupView(rootView);

        return rootView;
    }

    private void setupView(View rootView) {

        FragmentManager fm = getChildFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);

        mMapFragment.getMapAsync(mOnMapReadyCallback);
    }

    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            setupMapsIndoors();

        }
    };



    void  setupMapsIndoors() {

        mMapControl = new MapControl(getActivity(), mMapFragment, mGoogleMap);

        mMapControl.init(new OnLoadingDataReadyListener() {
            @Override
            public void onLoadingDataReady(@Nullable MIError miError) {
                // after the map control is initialized we can
                queryLocation();

                getActivity().runOnUiThread(() -> {
                    mMapControl.selectFloor( 1 );
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 18f ) );

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



    MPLocationsProvider mLocationsProvider;


    void queryLocation(){

        mLocationsProvider = new MPLocationsProvider();

        mLocationQueryBuilder =     new LocationQuery.Builder(
                getContext(),
                new Locale(MapsIndoors.getLanguage()),
                getString( R.string.mapsindoors_api_key)
        );

        // init the querry builder, in this case we will querry the coffee machine in our office
        mLocationQueryBuilder.
                setQuery("coffee machine").
                setOrderBy( LocationQuery.NO_ORDER ).
                setFloor(1).
                setMaxResults(1);
        // Build the querry
        mLocationQuerry = mLocationQueryBuilder.build();
        // Querry the data
        mLocationsProvider.getLocationsAsync( mLocationQuerry, mSearchLocationsReadyListener );


    }


    OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener() {


        @Override
        public void onLocationsReady(@Nullable List<Location> locations, @Nullable MIError error) {

            if(locations != null && locations.size() != 0){
                mMapControl.displaySearchResults(Collections.singletonList( locations.get(0) ) ,true);
            }
        }

        };


    }
