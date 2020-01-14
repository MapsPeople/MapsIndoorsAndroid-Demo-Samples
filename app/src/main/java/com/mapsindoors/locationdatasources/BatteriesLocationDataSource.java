package com.mapsindoors.locationdatasources;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;

import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.LocationDisplayRule;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationSource;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MPLocationsObserver;
import com.mapsindoors.mapssdk.MapsIndoors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/***
 ---
 title: Creating your own Location Data Source - Part 2
 ---

 In this tutorial we will show how you can build a custom Location Source. In [Part 1](locationdatasourcesspeoplelocationdatasource) we created a people location source that mocks locations of people.

 Now we will create another location source that represents batteries. The power level of the batteries will be changing over time and it needs to be visible on the map realtime.

 We will start by creating our implementation of a location source.

 Create the class `BatteriesLocationDataSource` that implements `MPLocationSource`.
 ***/
public class BatteriesLocationDataSource implements MPLocationSource {

    /***
     First we need to predefine some attributes.
     * `BASE_POSITION`: We need a base position as a start for the Locations.
     * `LOCATIONS_COUNT`: The number of locations desired.
     * `LOCATION_SOURCE_ID`: a unique  location source ID.
     * `LOCATION_TYPE`: a type for the locations of this source.
     * `LOCATION_CLUSTER_ID`: A cluster ID that will let locations from this source cluster together on the map in case of overlap.
     * `DISPLAY_RULE`: The display rule for the locations of this source.
     ***/
    private static final LatLng BASE_POSITION       = new LatLng( 57.0579814, 9.9504668 );
    private static final int    LOCATIONS_COUNT     = 20;
    private static final int    LOCATION_SOURCE_ID  = 10000;
    private static final String LOCATION_TYPE       = "BatteryLocationType";
    private static final int    LOCATION_CLUSTER_ID = LOCATION_TYPE.hashCode();

    static final LocationDisplayRule DISPLAY_RULE = new LocationDisplayRule.Builder( LOCATION_TYPE ).
            setVectorDrawableIcon( R.drawable.ic_battery_60_black_24dp ).
            setVisible( true ).
            setShowLabel( false ).
            setZoomLevelOn( 16 ).
            setLocationClusterId( LOCATION_CLUSTER_ID ).
            setDisplayRank( 1 ).
            setShowIcon(true).
            build();

    /***
     Then we need to add some variables.
     * `observers`: The observer objects that we will notify about changes.
     * `locationsList`: A list of MPLocation - Will have the list of the MPLocations up to date.
     * `status`: holds the status of the location data source.
     * `random`: Used to generate some random values in the data creation and editing.
     * `mDataUpdateTimer`: Timer that we will need to plan some recurrent updates.
     * `iconCurrentIndex`: An index for icons so we can update the locations with the same icon each time.
     ***/
    @NonNull
    private List<MPLocationsObserver> observers;
    private List<MPLocation> locationsList;
    private MPLocationSourceStatus status;
    private Random random = new Random();
    private Timer mDataUpdateTimer;
    private int iconCurrentIndex = 0;



    BatteriesLocationDataSource() {

        this.locationsList = new ArrayList<>( LOCATIONS_COUNT );
        this.observers = new ArrayList<>();
        this.status = MPLocationSourceStatus.NOT_INITIALIZED;
    }

    /***
     Create the `startUpdatingIcons` method that simply calls `updateLocations` every half a second.
     ***/
    void startUpdatingIcons() {
        if (!setup()) {
            return;
        }

        if (mDataUpdateTimer != null) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer = null;
        }

        mDataUpdateTimer = new Timer();

        mDataUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLocations();
            }
        }, 2000, 500 );
    }

    /***
     Create a method that can stop the icons update at any time.
     ***/
    void stopUpdatingIcons() {
        if (mDataUpdateTimer != null) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer.purge();
        }
    }

    /***
     Create a method called `setup` that will:
     * Make sure that data source was not already initialized and data is loaded.
     * Create the locations.
     * Make the first notification.
     * Change the status to available.
     ***/
    private boolean setup()
    {
        if( this.status != MPLocationSourceStatus.NOT_INITIALIZED ) {
            return true;
        }

        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final boolean gotBuildings = buildingCollection != null;
        if( !gotBuildings ) {
            return false;
        }

        locationsList.clear();
        locationsList.addAll( generateLocations( true ) );

        notifyUpdateLocations( locationsList );

        setStatus( MPLocationSourceStatus.AVAILABLE );

        return true;
    }

    // Icons
    @DrawableRes
    final int[] icons = new int[]{
            R.drawable.ic_battery_20_black_24dp,
            R.drawable.ic_battery_30_black_24dp,
            R.drawable.ic_battery_50_black_24dp,
            R.drawable.ic_battery_60_black_24dp,
            R.drawable.ic_battery_80_black_24dp,
            R.drawable.ic_battery_90_black_24dp,
            R.drawable.ic_battery_full_black_24dp
    };

    /***
     Create a method called `updateLocations`. Iterate number again and for each iteration:
     * Get the corresponding MPLocation Builder
     * Set a new position
     * Generate an MPLocation from a MPLocation.Builder
     * Call the notifyUpdateLocations with the updated list.
     ***/
    void updateLocations() {
        if (!MapsIndoors.isReady()) {
            return;
        }

        final int locCount = locationsList.size();
        final List<MPLocation> updatedList = new ArrayList<>(locCount);

        int iColor = 0;
        @DrawableRes int currentIcon = 0;

        // Icon and tint color (animated)
        final int availableIconsCount = icons.length;
        // the color tint will vary
        iColor = ColorUtils.blendARGB(0xff2DD855, 0xffFF3700, ((1f * iconCurrentIndex) / availableIconsCount));
        currentIcon = icons[iconCurrentIndex];
        iconCurrentIndex = (iconCurrentIndex + 1) % availableIconsCount;

        for (int i = 0, LocationCount = locationsList.size(); i < LocationCount; i++) {
            final MPLocation p = locationsList.get( i );

            // "Update" an MPLocation by using the copy/edit builder
            final MPLocation.Builder updatedLocation = new MPLocation.Builder( p );

            // Change the icon & tint color
            updatedLocation.
                    // Specify a size
                    setVectorDrawableIcon( currentIcon, 32, 32 ).
                    // Specify the tint color
                    setTint( iColor );

            updatedList.add( updatedLocation.build() );
        }

        locationsList.clear();
        locationsList.addAll( updatedList );

        notifyUpdateLocations( updatedList );
    }

    /***
     Create a method called `generateLocations`:
     * An MPLocation.Builder with an id
     * A random position
     * A name
     * A type - later used to style the location
     * A floor Index
     * A building
     ***/
    @NonNull
    private List<MPLocation> generateLocations( boolean randomizeStartingPosition )
    {
        final List<MPLocation> batteryLocations = new ArrayList<>( LOCATIONS_COUNT );

        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final boolean gotBuildingData = buildingCollection != null;

        for ( int i = 0; i < LOCATIONS_COUNT; i++) {

            final LatLng batteryPosition;
            if (randomizeStartingPosition) {
                batteryPosition = getRandomPosition();
            } else {
                batteryPosition = BASE_POSITION;
            }

            final MPLocation.Builder locBuilder = new MPLocation.Builder( "" + LOCATION_SOURCE_ID + i );
            locBuilder.setPosition( batteryPosition ).
                    setName( "Battery" + i ).
                    setType( LOCATION_TYPE );


            if (gotBuildingData) {
                // Find a building at this location (batteryPosition)
                final Building building = buildingCollection.getBuilding(batteryPosition);
                if (building != null) {

                    // Building found at this location, get the list of floors in it
                    final List<Floor> floors = building.getFloors();

                    // Choose a random floor
                    final Floor floor = floors.get((int) (Math.random() * (floors.size())));

                    // Set the Location floor
                    locBuilder.setFloor((floor != null) ? floor.getZIndex() : Floor.DEFAULT_GROUND_FLOOR_INDEX);

                    // Set the building name where this Location is in
                    locBuilder.setBuilding( building.getName() );
                } else {
                    // If this location was outside a building, set its floor/z index to zero (ground floor)
                    locBuilder.setFloor( Floor.DEFAULT_GROUND_FLOOR_INDEX );
                }
            } else {
                // If this location was outside a building, set its floor/z index to zero (ground floor)
                locBuilder.setFloor( Floor.DEFAULT_GROUND_FLOOR_INDEX );
            }

            batteryLocations.add( locBuilder.build() );
        }

        return batteryLocations;
    }

    /***
     Create a method called `getRandomPosition` that simply returns a random LatLng (here within proximity of the demo venue)
     ***/
    private LatLng getRandomPosition() {
        final double lat = BASE_POSITION.latitude + (-4 + random.nextInt(20)) * 0.000005;
        final double lng = BASE_POSITION.longitude + (-4 + random.nextInt(20)) * 0.000010;

        return new LatLng(lat, lng);
    }

    /***
     Create a method called `notifyUpdateLocations` to loop all the observers and notify them with an update
     ***/
    private void notifyUpdateLocations( List<MPLocation> updatedLocations ) {
        for( int i = observers.size(); --i >= 0; ) {
            observers.get( i ).onLocationsUpdated( updatedLocations, this );
        }
    }

    /***
     The same thing for notifying observers with new status
     ***/
    private void notifyLocationStatusChanged( @NonNull MPLocationSourceStatus prevStatus, @NonNull MPLocationSourceStatus newStatus ) {
        for( int i = observers.size(); --i >= 0; ) {
            observers.get( i ).onStatusChanged( newStatus, this );
        }
    }

    /***
     Sets the internal state and notifies a status changed message if applies
     ***/
    private void setStatus( @NonNull MPLocationSourceStatus newStatus ) {
        MPLocationSourceStatus cStatus = status;

        if (cStatus != newStatus) {
            status = newStatus;
            notifyLocationStatusChanged( cStatus, newStatus );
        }
    }

    /***
     Implement the MPLocationSource method `getLocations`. This List will always contain the up to date MPLocations
     ***/
    @NonNull
    @Override
    public List<MPLocation> getLocations() {
        return locationsList;
    }

    /***
     Implement the MPLocationSource method `addLocationObserver`.
     ***/
    @Override
    public void addLocationsObserver(@Nullable MPLocationsObserver observer) {
        if (observer != null) {
            observers.remove(observer);
            observers.add(observer);
        }
    }

    /***
     Implement the MPLocationSource method `removeLocationObserver`.
     ***/
    @Override
    public void removeLocationsObserver(@Nullable MPLocationsObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    /***
     Implement the MPLocationSource method `getStatus`.
     ***/
    @NonNull
    @Override
    public MPLocationSourceStatus getStatus() {
        return status;
    }

    /***
     Implement the MPLocationSource method `getSourceId`.
     ***/
    @Override
    public int getSourceId() {
        return LOCATION_SOURCE_ID;
    }

    @Override
    public void clearCache() {

    }

    @Override
    public void terminate() {

    }

    /***
     In [Part 3](locationdatasourceslocationdatadourcesfragment) we will create a data source that shows how we can use data sources to show different states of POIs on a map.
     ***/
    //****
}
