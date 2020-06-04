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
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.Convert;
import com.mapsindoors.mapssdk.ImageSize;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationClusterImageAdapter;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;

import java.util.List;

/***
 ---
 title: Work with location grouping / clustering
 ---

 This is an example of enabling and disabling location clustering on the map as well as providing custom cluster tapping behaviour and custom cluster images.

 Create the class `LocationClusteringFragment` that extends `Fragment`.
 ***/
public class LocationClusteringFragment extends Fragment {

    /***
     Add a `GoogleMap` and a `MapControl` to the class
     ***/
    MapControl mMapControl;
    GoogleMap mGoogleMap;
    //****

    ToggleButton clusteringToggleButton;


    /***
     Add other needed views for this example
     ***/
    SupportMapFragment mMapFragment;

    /***
     The Venue's coordinates
     ***/
    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
    //****

    public LocationClusteringFragment()
    {
        // Required empty public constructor
    }

    @NonNull
    public static LocationClusteringFragment newInstance()
    {
        return new LocationClusteringFragment();
    }


    //region FRAGMENT LIFECYCLE
    @Override
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_clustering_map, container, false );
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

        // Note that runtime Marker clustering enable/disable doesn't work yet in the current SDK version
        //clusteringToggleButton = rootView.findViewById( R.id.clustering_toggle_button );
        //clusteringToggleButton.setOnCheckedChangeListener( onCheckedChangeListener );

        mMapFragment = (SupportMapFragment) fm.findFragmentById( R.id.mapfragment );

        mMapFragment.getMapAsync( mOnMapReadyCallback );
    }

    /***
     Once the map is ready, move the camera to the venue location and call setupMapsIndoors
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
     Add a `locationClusterImageAdapter` to customize the cluster marker icons and provide their sizes
     ***/
    MPLocationClusterImageAdapter locationClusterImageAdapter = new MPLocationClusterImageAdapter() {
        @Nullable
        @Override
        public Bitmap getImage( @NonNull String clusterId, @NonNull List<MPLocation> locations, @NonNull ImageSize imageSize )
        {
            final int textSize = Convert.getPixels( 15 );

            return getCircularImageWithText(
                    "" + locations.size(),
                    textSize,
                    imageSize.width,
                    imageSize.height
            );
        }

        @NonNull
        @Override
        public ImageSize getImageSize( @NonNull String clusterId, int count )
        {
            final int imageSizeInPixels = Convert.getPixels( 25 );

            return new ImageSize( imageSizeInPixels, imageSizeInPixels );
        }
    };

    /***
     Create a method `getCircularImageWithText` that creates the custom bitmaps for our cluster icons
     ***/
    @NonNull
    public static Bitmap getCircularImageWithText( @NonNull String text, int textSize, int width, int height )
    {
        final Paint background = new Paint();
        background.setColor( Color.WHITE );

        // Now add the icon on the left side of the background rect
        final Bitmap result = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        final Canvas canvas = new Canvas( result );

        final int radius = width >> 1;
        canvas.drawCircle( radius, radius, radius, background );

        background.setColor( Color.BLACK );
        background.setStyle( Paint.Style.STROKE );
        background.setStrokeWidth( 3 );

        canvas.drawCircle( radius, radius, radius - 2, background );

        final TextPaint tp = new TextPaint();
        tp.setTextSize( textSize );
        tp.setColor( Color.BLACK );

        final Rect bounds = new Rect();

        tp.getTextBounds( text, 0, text.length(), bounds );

        final int text_height = bounds.height();
        final int text_width = bounds.width();

        final int textpos_x = (width - text_width) >> 1;
        final int textpos_y = (height + text_height) >> 1;

        canvas.drawText( text, textpos_x, textpos_y, tp );

        return result;
    }

    /***
     Create a method `setupMapsIndoors` that:
     * Sets the API key to the desired solution.
     * Sets the Google API key (required by our routing provider).
     * Instantiates MapControl.
     * Enables clustering.
     * Sets an OnLocationClusterListener so we can handle the cluster markers click events.
     * Sets the LocationClusterImageAdapter.
     * Initializes the MapControl object which will synchronize the data.
     * When the MapControl's initialization is done, it selects a floor and animates the camera to the venue position.
     ***/
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

        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );

        mMapControl = new MapControl( context );
        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );

        // Currently, on Android, we can enable/disable clustering
        // - via our CMS
        // - by calling MapControl.setLocationClusteringEnabled( true/false ) BEFORE invoking MapControl.init()
        // Runtime clustering enable/disable is not currently supported
        mMapControl.setLocationClusteringEnabled( true );

        // Set the custom cluster image adapter
        mMapControl.setLocationClusterImageAdapter( locationClusterImageAdapter );

        // When clicking on a cluster marker zoom in to the maximum trying to break the cluster
        mMapControl.setOnLocationClusterClickListener( ( marker, locations ) -> {

            mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( marker.getPosition(), 22f  ) );
            return true;
        } );

        mMapControl.init( miError -> {
            if( miError == null ) {
                final Activity _context = getActivity();
                if( _context != null ) {
                    mMapControl.selectFloor( 20 );
                    mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
                }
            }
        } );
    }

//****

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged( @NonNull CompoundButton buttonView, boolean isChecked )
        {
            if( mMapControl != null ) {
                mMapControl.setLocationClusteringEnabled( isChecked );
            }
        }
    };
}
