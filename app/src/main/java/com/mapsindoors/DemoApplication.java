package com.mapsindoors;

import android.app.Application;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;
import com.squareup.leakcanary.LeakCanary;

public class DemoApplication extends Application
{
    public static final String      TAG = DemoApplication.class.getSimpleName();

    //region DEBUG
    static final boolean ENABLE_LEAK_CANARY    = BuildConfig.DEBUG && false;
    static final boolean ENABLE_OS_STRICT_MODE = BuildConfig.DEBUG && false;
    //endregion

    private static      Application sInstance;


    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate()
    {
        super.onCreate();
        // Required initialization logic here!

        // Enable MapsIndoors debug messages (console)
        {
            dbglog.useDebug( BuildConfig.DEBUG );
            dbglog.setCustomTagPrefix( TAG + "_" );
        }

        //region DEBUG
        enableLeakCanary();
        enableOSStrictMode();
        //endregion

        // Initialize the MapsIndoors SDK here by providing:
        // - The application context
        // - The MapsIndoors API key
        MapsIndoors.initialize( getApplicationContext(), getString( R.string.mi_api_key ) );

        // Your Google Maps API key
        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );

        sInstance = this;
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory()
    {
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

    @NonNull
    public static Application getInstance()
    {
        return sInstance;
    }


    //region DEBUG
    private void enableLeakCanary()
    {
        if( ENABLE_LEAK_CANARY )
        {
            // Leak canary
            if( LeakCanary.isInAnalyzerProcess( this ) ) {
                return;
            }
            LeakCanary.install( this );
        }
    }

    private void enableOSStrictMode()
    {
        if( ENABLE_OS_STRICT_MODE )
        {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().
                            detectAll().
                            penaltyLog().
                            penaltyFlashScreen().
                            build()
            );
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder().
                            detectAll().
                            penaltyLog().
                            build()
            );
        }
    }
    //endregion
}
