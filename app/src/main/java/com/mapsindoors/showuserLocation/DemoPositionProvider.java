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

public class DemoPositionProvider  implements
        PositionProvider {

    boolean isRunning = false;
    OnPositionUpdateListener mPositionUpdateListener;

     MPPositionResult mLatestPosition;

    MPPositionResult fixedPosition =  new MPPositionResult(
            new Point( 57.05813067, 9.95058065  ),
            0,
            0
    );


    Timer mPositionUpdateTimer = new Timer();






    @NonNull
    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    public boolean isPSEnabled() {
        return true ;
    }

    @Override
    public void startPositioning(@Nullable String arg) {

        //Set the schedule function and rate
        mPositionUpdateTimer.scheduleAtFixedRate(new TimerTask() {

                                                     @Override
                                                     public void run() {


                                                         updatePosition();
                                                     }

                                                 },
                //Set how long before to start calling the TimerTask (in milliseconds)
                0,
                //Set the amount of time between each execution (in milliseconds)
                1000);

        isRunning = true;
    }

    @Override
    public void stopPositioning(@Nullable String arg) {
        mPositionUpdateTimer.cancel();
        isRunning = false;

    }

    @Override
    public boolean isRunning() {
        return isRunning ;
    }

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

    void updatePosition(){

        mLatestPosition = fixedPosition;


        mLatestPosition.setProvider( this );
        if (mPositionUpdateListener != null) {
            mPositionUpdateListener.onPositionUpdate(mLatestPosition);
        }

    }
}
