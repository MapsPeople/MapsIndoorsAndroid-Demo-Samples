package mapsindoors.com.midemo.locationdetailsdemo;

import android.content.Context;
import android.os.Bundle;
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
import com.mapspeople.LocationPropertyNames;
import com.mapspeople.LocationQuery;
import com.mapspeople.MapControl;
import com.mapspeople.OnLoadingDataReadyListener;
import com.mapspeople.errors.MIError;

import mapsindoors.com.midemo.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LocationDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationDetailsFragment extends Fragment {


    MapControl mMapControl;
    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;
    TextView detailsTextView;

    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //querry objects
    LocationQuery mLocationQuerry;
    LocationQuery.Builder mLocationQueryBuilder;

    public LocationDetailsFragment() {
        // Required empty public constructor
    }


    public static LocationDetailsFragment newInstance(String param1, String param2) {
        LocationDetailsFragment fragment = new LocationDetailsFragment();

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
        View rootView = inflater.inflate(R.layout.fragment_show_location_details, container, false);
        setupView(rootView);

        return rootView;
    }

    private void setupView(View rootView) {

        FragmentManager fm = getChildFragmentManager();


        detailsTextView = rootView.findViewById(R.id.details_text_view);

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);

        mMapFragment.getMapAsync(mOnMapReadyCallback);
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

            mMapControl.setOnMarkerClickListener( marker -> {

            final Location loc = mMapControl.getLocation( marker );
            if( loc != null )
            {
                marker.showInfoWindow();
                detailsTextView.setText("Name: "+loc.getName() +
                        "\nDescription: " + loc.getStringProperty(LocationPropertyNames.DESCRIPTION));
            }

            return true;
        });


        mMapControl.init(new OnLoadingDataReadyListener() {
            @Override
            public void onLoadingDataReady(@Nullable MIError miError) {


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






    }
