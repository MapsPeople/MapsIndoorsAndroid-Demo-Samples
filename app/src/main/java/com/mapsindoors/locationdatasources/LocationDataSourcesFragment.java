package com.mapsindoors.locationdatasources;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.mapsindoors.DemoApplication;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MPLocationSource;
import com.mapsindoors.mapssdk.MPLocationSourceOnStatusChangedListener;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnResultReadyListener;

import java.util.HashSet;
import java.util.Set;

/***
 ---
 title: Creating your own Location Source - Part 3
 ---

 This is part 3 of the tutorial of building a custom Location Source. [In Part 1 we created the People Location Source](locationdatasourcespeoplelocationdatasource) and [In Part 2 we created the Batteries Location Source](locationdatasourcesbatterieslocationdatasource). Now we will create a Fragment displaying a map that shows the mocked people locations and the batteries on top of a MapsIndoors map.

 Create a class `LocationDataSourcesFragment` that extends `Fragment`.
 ***/
public class LocationDataSourcesFragment extends Fragment {

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap mGoogleMap;

    /***
     Add other needed views for this example
     ***/
    SupportMapFragment mMapFragment;

    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng(57.05813067, 9.95058065);
    //****


    /***
     Data sources objects
     ***/
    PeopleLocationDataSource peopleLocationDataSource;
    BatteriesLocationDataSource batteriesLocationDataSource;
    //****

    public LocationDataSourcesFragment() {
    }

    @NonNull
    public static LocationDataSourcesFragment newInstance() {
        return new LocationDataSourcesFragment();
    }


    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_data_sources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView(view);
    }




    private void setupView(View rootView) {
        FragmentManager fm = getChildFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);

        mMapFragment.getMapAsync(mOnMapReadyCallback);
    }

    /***
     Once the map is ready move the camera to the venue location and call the setupMapsIndoors
     ***/
    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VENUE_LAT_LNG, 13.0f));

            setupMapsIndoors();
        }
    };

    /***
     Create a method `setupMapsIndoors`
     * Init the MapsIndoors
     * Set the google API key
     * Attach a listener to listen to the status of the data sources
     * Call setupMapControl
     ***/
    void setupMapsIndoors() {
        final Activity context = getActivity();

        if ((context == null) || (mMapFragment == null) || (mMapFragment.getView() == null)) {
            return;
        }

        MapsIndoors.onApplicationTerminate();
        MapsIndoors.initialize(DemoApplication.getInstance(), getString(R.string.mi_api_key));
        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );
        MapsIndoors.addLocationSourceOnStatusChangedListener(locationSourceOnStatusChangedListener);

        setupLocationDataSources(error -> setupMapControl());
    }

    /***
     Create a method `setupLocationDataSources`
     * Instantiate `PeopleLocationDataSource` and `BatteriesLocationDataSource`
     * Set the location sources to the MapsIndoors
     ***/
    void setupLocationDataSources(@NonNull OnResultReadyListener listener) {

        Set<MPLocationSource> locationDataSources= new HashSet<>(2);

        peopleLocationDataSource = new PeopleLocationDataSource();
        locationDataSources.add(peopleLocationDataSource);

        batteriesLocationDataSource = new BatteriesLocationDataSource();
        locationDataSources.add(batteriesLocationDataSource);

        MapsIndoors.setLocationSources(locationDataSources.toArray(new MPLocationSource[0]), error -> {

            final FragmentActivity context = getActivity();
            if (context != null) {
                context.runOnUiThread(() -> {
                    if (error != null) {
                        Toast.makeText(context, "Error occurred when setting the Datasources", Toast.LENGTH_SHORT).show();
                    }

                    listener.onResultReady(error);
                });
            }
        });
    }

    /***
     Create a method 'setupMapControl'
     * Instantiate and init the MapControl object which will sync data
     * Add the location sources display rules to the map control.
     * Init the MapControl object which will sync data
     ***/
    void setupMapControl() {
        final Activity activityContext = getActivity();
        if ((activityContext == null) || (mMapFragment == null)) {
            return;
        }

        mMapControl = new MapControl(activityContext);
        mMapControl.setGoogleMap(mGoogleMap, mMapFragment.getView());

        mMapControl.addDisplayRule(PeopleLocationDataSource.DISPLAY_RULE);
        mMapControl.addDisplayRule(BatteriesLocationDataSource.DISPLAY_RULE);

        mMapControl.init(null);
    }

    /***
     Add `locationSourceOnStatusChangedListener`
     * Once the status of the location sources is available we can start updating our locations
     * Select the first floor and move the camera to the Venue position
     ***/
    final MPLocationSourceOnStatusChangedListener locationSourceOnStatusChangedListener = new MPLocationSourceOnStatusChangedListener() {
        @Override
        public void onStatusChanged(@NonNull MPLocationSourceStatus status, int sourceId) {
            if (status == MPLocationSourceStatus.AVAILABLE) {
                final Activity context = getActivity();
                if (context != null) {
                    context.runOnUiThread(() -> {

                        peopleLocationDataSource.startUpdatingPositions();
                        batteriesLocationDataSource.startUpdatingIcons();

                        //Select a floor and animate the camera to the venue position
                        mMapControl.selectFloor(1);
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(VENUE_LAT_LNG, 20f));
                    });
                }
            }
        }
    };

    /***
     Implement the `onDestroyView` method to stop the updating of the location source
     ***/
    @Override
    public void onDestroyView() {
        if (mMapControl != null) {

            peopleLocationDataSource.stopUpdatingPositions();
            batteriesLocationDataSource.stopUpdatingIcons();

            MapsIndoors.removeLocationSourceOnStatusChangedListener(locationSourceOnStatusChangedListener);

            mMapControl.onDestroy();

            MapsIndoors.onApplicationTerminate();

            MapsIndoors.initialize(DemoApplication.getInstance(), getString(R.string.mi_api_key));
        }

        super.onDestroyView();
    }
//****
}
