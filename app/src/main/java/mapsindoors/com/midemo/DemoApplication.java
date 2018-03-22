package mapsindoors.com.midemo;

import android.app.Application;
import android.content.res.Configuration;

import com.mapspeople.MapsIndoors;
import com.mapspeople.dbglog;

public class DemoApplication extends Application {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!

        MapsIndoors.initialize(
                getApplicationContext(),
                getString( R.string.mapsindoors_api_key),
                getString( R.string.google_maps_key )
        );

        dbglog.useDebug( true );
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}
