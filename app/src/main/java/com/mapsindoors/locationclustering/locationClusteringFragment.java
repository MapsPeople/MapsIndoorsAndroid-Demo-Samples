package com.mapsindoors.locationclustering;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.Convert;
import com.mapsindoors.mapssdk.ImageSize;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationClusterImageAdapter;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLocationClusterClickListener;
import com.mapsindoors.mapssdk.OnSingleImageLoadedListener;

import java.util.List;

/***
 ---
 title: Work with location grouping / clustering
 ---

 This is an example of enabling and disabling location grouping on the map as well as providing custom cluster tapping behavior and custom cluster images.

 Create a class `locationClusteringFragment` that extends `Fragment`.
 ***/

public class locationClusteringFragment extends Fragment {

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap mGoogleMap;
    ToggleButton clusteringToggleButton;
    /***
     Add other needed views for this example
     ***/

    SupportMapFragment mMapFragment;

    /***
     The lat lng of the Venue
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //

    public locationClusteringFragment()
    {
        // Required empty public constructor
    }

    public static locationClusteringFragment newInstance()
    {
        return new locationClusteringFragment();
    }


    //Region FRAGMENT LIFECYCLE

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clustering_map, container, false);
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

    /***
     Setup the needed views for this example
     ***/
    private void setupView( View rootView )
    {
        FragmentManager fm = getChildFragmentManager();

        clusteringToggleButton = rootView.findViewById(R.id.clustering_toggle_button);


        clusteringToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mMapControl!= null){
                    mMapControl.setLocationClusteringEnabled(b);
                }
            }
        });

        mMapFragment = (SupportMapFragment) fm.findFragmentById( R.id.mapfragment );

        mMapFragment.getMapAsync( mOnMapReadyCallback );
    }
    /***
     Once the map is ready move the camera to the venue location and call the setupMapsIndoors
     ***/
    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 13.0f ) );

            setupMapsIndoors();
        }
    };


    /***
     Setup MapsIndoors
     ***/
    void setupMapsIndoors()
    {
        /***
         Setting the API key to the desired solution
         ***/

            MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );

        /***
         Setting the Google API key
         ***/
        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );
        if( getActivity() == null )
        {
            return;
        }


        /***
         Instantiate and init the MapControl object which will sync data
         ***/
        mMapControl = new MapControl( getActivity() );
        mMapControl.setGoogleMap(mGoogleMap, mMapFragment.getView());

        mMapControl.setLocationClusteringEnabled(true);


       mMapControl.setLocationClusterImageAdapter(new MPLocationClusterImageAdapter() {
            @Nullable
            @Override
            public Bitmap getImage(@NonNull String clusterId, @NonNull List<MPLocation> locations, @NonNull ImageSize imageSize) {
                int textSize = Convert.getPixels(15);

                return getCircularImageWithText(""+ locations.size(),textSize,imageSize.width, imageSize.height );
            }

            @NonNull
            @Override
            public ImageSize getImageSize(@NonNull String clusterId, int count) {
                return new ImageSize(Convert.getPixels(25),Convert.getPixels(25));
            }
        });


        /***
         Define the delegate method `didTap` that will receive tap events from a cluster marker

         * Check if zoom is possible and increment map zoom
         * Return true to indicate that you handle the event and do not want default behavior to happen
         ***/

        mMapControl.setOnLocationClusterClickListener(new OnLocationClusterClickListener() {
            @Override
            public boolean onLocationClusterClick(@NonNull Marker marker, @Nullable List<MPLocation> locations) {

                mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 13.0f ) );

                return true;
            }
        });

        /***
         Init the MapControl object which will sync data
         ***/
        mMapControl.init( miError -> {

            if( miError == null )
            {
                Activity context = getActivity();
                if( context != null )
                {
                    context.runOnUiThread( () -> {
                        /***
                         Select a floor and animate the camera to the venue position
                         ***/
                        mMapControl.selectFloor( 1 );
                        mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
                    });
                }
            }
        });
    }


    public static Bitmap getCircularImageWithText(String text, int textSize, int width, int height){

        Paint background = new Paint();
        background.setColor( Color.WHITE );


        // Now add the icon on the left side of the background rect
        Bitmap result = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas( result );
        int radius = width/2;
        canvas.drawCircle( radius, radius, radius, background );

        background.setColor( Color.BLACK );
        background.setStyle(Paint.Style.STROKE);
        background.setStrokeWidth(3);


        canvas.drawCircle( radius, radius, radius-2, background );


        TextPaint tp = new TextPaint();
        tp.setTextSize( textSize);
        tp.setColor( Color.BLACK );

        Rect bounds = new Rect();

        int text_height = 0;
        int text_width = 0;

        tp.getTextBounds(text, 0, text.length(), bounds);

        text_height =  bounds.height();
        text_width =  bounds.width();

        int textpos_x = (width - text_width) /2;
        int textpos_y =  (height+ text_height)/2;
        canvas.drawText( text, textpos_x, textpos_y, tp );


        return result;
    }


}
