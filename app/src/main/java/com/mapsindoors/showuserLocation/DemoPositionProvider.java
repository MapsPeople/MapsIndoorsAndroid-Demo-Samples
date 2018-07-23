package com.mapsindoors.showuserLocation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.models.Point;

import java.util.Timer;
import java.util.TimerTask;

/***
 ---
 title: Show the Blue Dot with MapsIndoors - Part 1
 ---

 In this tutorial we will show how you can show a blue dot on the map, representing the users location. The position will be served from a mocked positioning provider and displayed on a map in a view controller.

 We will start by creating our implementation of a positioning provider.

 Create a class `DemoPositionProvider` that implements `PositionProvider`.
 ***/
public class DemoPositionProvider  implements
        PositionProvider
//
{

    /***
     Add some member variables to `DemoPositionProvider`.

     * `mPositionUpdateListener`: The listener object
     * `isRunning`: A running state boolean flag
     * `mLatestPosition`: The latest positioning result
     * `mPositionUpdateTimer`: A timer so we can update the position of the user within an time interval
     ***/

    OnPositionUpdateListener mPositionUpdateListener;
    boolean isRunning = false;

    MPPositionResult mLatestPosition;
    MPPositionResult fixedPosition =  new MPPositionResult(
            new Point( 57.05813067, 9.95058065  ),
            0,
            0
    );

    Timer mPositionUpdateTimer = new Timer();

    /***
     Create a method called `updatePosition`. This will be our "loop" constantly posting a new position to the delegate.

     * Check if the provider has a running state
     * Assign the `fixedPosition` value to `mLatestPosition`
     * Notify the listener by calling `onPositionUpdate` passing the new position as argument
     ***/
    void updatePosition(){
        if(isRunning){
            mLatestPosition = fixedPosition;
            mLatestPosition.setProvider( this );
            if (mPositionUpdateListener != null) {
                mPositionUpdateListener.onPositionUpdate(mLatestPosition);
            }
        }
    }


    /***
     Implement the `isPSEnabled` method to check is the Positioning system is working or not, in this case the PS is always working since it's a mocked positioning provider.
     ***/
    @Override
    public boolean isPSEnabled() {
        return true ;
    }
    /***
     Implement the `startPositioning` method. We set the `running` boolean to true and schedule the repeating task of updating the user position by calling the updatePosition every 3 seconds.
     ***/
    @Override
    public void startPositioning(@Nullable String arg) {
        isRunning = true;

        //Set the schedule function and rate
        mPositionUpdateTimer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() { updatePosition();}},
                0,
                3000);
    }

    /***
     Implement the `stopPositioning` method. We set the `running` boolean to false and stop the timer.
     ***/
    @Override
    public void stopPositioning(@Nullable String arg) {
        isRunning = false;
        mPositionUpdateTimer.cancel();

    }

    /***
     Implement the `isRunning` method. Return the value of `running`.
     ***/
    @Override
    public boolean isRunning() {
        return isRunning ;
    }
    //
    @Override
    public void addOnPositionUpdateListener(@Nullable OnPositionUpdateListener listener) {

        mPositionUpdateListener = listener;

    }

    @Override
    public void removeOnPositionUpdateListener(@Nullable OnPositionUpdateListener listener) {
        mPositionUpdateListener = null;

    }

    @Override
    public void setProviderId(@Nullable String id) {

    }

    @Override
    public void addOnstateChangedListener(OnStateChangedListener onStateChangedListener) {

    }

    @Override
    public void removeOnstateChangedListener(OnStateChangedListener onStateChangedListener) {

    }

    @Override
    public void checkPermissionsAndPSEnabled(PermissionsAndPSListener permissionAPSlist) {

    }

    @Nullable
    @Override
    public String getProviderId() {
        return null;
    }

    @Nullable
    @Override
    public PositionResult getLatestPosition() {
        return null;
    }


    @Override
    public void startPositioningAfter(int delayInMs, @Nullable String arg) {

    }

    @Override
    public void terminate() {

    }

    @NonNull
    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }


    /***
     In [Part 2](showuserlocationshowuserlocationfragment) we will create the map fragment that displays the blue dot.
     ***/
    //

}
