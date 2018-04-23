package com.mapsindoors;

import android.app.Application;
import android.content.res.Configuration;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;

public class DemoApplication extends Application
{
    public static final String TAG = DemoApplication.class.getSimpleName();
    private static Application sInstance;

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!

        dbglog.useDebug( true );
        dbglog.setCustomTagPrefix( TAG + "_" );

        MapsIndoors.initialize(
                getApplicationContext(),
                getString( R.string.mi_api_key),
                getString( R.string.google_maps_key )
        );
        sInstance = this;

        //MapsIndoors.synchronizeContent( null );
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );

        MapsIndoors.onApplicationConfigurationChanged( newConfig );
    }

    public static Application getInstance() {
        return sInstance;
    }

}
