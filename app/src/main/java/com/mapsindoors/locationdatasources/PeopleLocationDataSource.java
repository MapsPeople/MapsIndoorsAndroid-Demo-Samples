package com.mapsindoors.locationdatasources;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.mapsindoors.mapssdk.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/***
 ---
 title: Creating your own Location Data Source - Part 1
 ---

 In this tutorial we will show how you can build a custom Location Source, representing locations of people. The people's locations will be served from a mocked list in the source and displayed on a map.

 We will start by creating our implementation of a location source.

 Create the class `PeopleLocationDataSource` that implements `MPLocationSource`.
 ***/
public class PeopleLocationDataSource implements MPLocationSource {

    /***
     First we need to predefine some attributes.
     * `BASE_POSITION`: We need a base position as a start for the Locations
     * `RANGE_MAX_LAT_OFFSET`: A max latitude offset to put a limit on how far the Locations can move on the latitude axis.
     * `RANGE_MAX_LNG_OFFSET`: A max longitude offset to put a limit on how far the Locations can move on the longitude axis.
     * `LOCATIONS_COUNT`: The number of Locations desired
     * `LOCATION_SOURCE_ID`: a unique  location source ID
     * `LOCATION_TYPE`: a type for the locations of this source
     * `LOCATION_CLUSTER_ID`: A cluster ID that will let locations from this source cluster together on the map in case of overlap.
     * `DISPLAY_RULE`: The display rule for the locations of this source.
     ***/
    private static final LatLng BASE_POSITION        = new LatLng( 57.0582502, 9.9504788 );
    private static final double RANGE_MAX_LAT_OFFSET = 0.000626 / 4;
    private static final double RANGE_MAX_LNG_OFFSET = 0.0003384 / 2;
    private static final int    LOCATIONS_COUNT      = 20;
    private static final int    LOCATION_SOURCE_ID   = 2000;
    private static final String LOCATION_TYPE        = "PeopleLocationType";
    public static final int     LOCATION_CLUSTER_ID  = 1;

    static final LocationDisplayRule DISPLAY_RULE = new LocationDisplayRule.Builder( LOCATION_TYPE ).
            setBitmapDrawableIcon( R.drawable.generic_user ).
            setVisible( true ).
            setShowLabel( false ).
            setZoomLevelOn( 18 ).
            setLocationClusterId( LOCATION_CLUSTER_ID ).
            setDisplayRank( 1 ).
            build();

    // People avatar icons
    @DrawableRes
    private final int[] peopleAvatars = new int[]{
            R.drawable.ic_avatar_1,
            R.drawable.ic_avatar_2,
            R.drawable.ic_avatar_3,
            R.drawable.ic_avatar_4,
            R.drawable.ic_avatar_5
    };

    /***
     Then we need to add some variables.
     * `observers`: We need a base position as a start for the Locations.
     * `locationsList`: A max latitude offset to put a limit on how far the Locations can move on the latitude axis.
     * `status`: holds the status of the location data source.
     * `mDataUpdateTimer`: Timer that we will need to plan some recurrent updates.
     * `dynamicLocations`: a List of DynaLocations that will carry the dynamic side of the locations.
     * `random`: used to generate some random values in the data creation and editing.
     ***/
    @NonNull
    private List<MPLocationsObserver> observers;
    private List<MPLocation> locationsList;
    private MPLocationSourceStatus status;
    private Timer mDataUpdateTimer;
    private List<DynaLocation> dynamicLocations;
    private Random random = new Random();



    /***
     Create the DynaLocation class that represents the moving Locations with a position and a heading
     ***/
    class DynaLocation
    {
        LatLng pos;
        double heading;

        DynaLocation( @NonNull LatLng pos, double heading )
        {
            this.pos = pos;
            this.heading = heading;
        }
    }


    PeopleLocationDataSource() {

        this.locationsList = new ArrayList<>( LOCATIONS_COUNT );
        this.observers = new ArrayList<>();
        this.status = MPLocationSourceStatus.NOT_INITIALIZED;
    }

    /***
     Create the `startUpdatingPositions` method that simply calls `updateLocations` every second.
     ***/
    void startUpdatingPositions() {
        if (!setup()) {
            return;
        }

        if (mDataUpdateTimer != null) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer = null;
        }

        mDataUpdateTimer = new Timer();

        mDataUpdateTimer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                updateLocations();
            }
        }, 2000, 1000 );
    }

    /***
     Create a method that can stop the positions updates at any time
     ***/
    void stopUpdatingPositions() {
        if (mDataUpdateTimer != null) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer.purge();
        }
    }

    /***
     Create a method called `setup` that will:
     * Make sure that the data source was not already initialized and data is loaded.
     * Create the locations.
     * Make the first notification.
     * Change the status to available
     ***/
    private boolean setup()
    {
        if( this.status != MPLocationSourceStatus.NOT_INITIALIZED ) {
            return true;
        }

        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final boolean gotBuildings = buildingCollection != null;
        if (!gotBuildings) {
            return false;
        }

        locationsList.clear();
        locationsList.addAll( generateLocations( false ) );

        notifyUpdateLocations( locationsList );

        setStatus( MPLocationSourceStatus.AVAILABLE );

        return true;
    }

    /***
     Create a method called `updateLocations` that will update the position of the Locations.
     ***/
    void updateLocations() {

        // make sure that that the MapsIndoors is ready and that everything is well set
        if (!MapsIndoors.isReady()) {
            return;
        }

        // Create a list where we are gonna put the updated locations
        final List<MPLocation> updatedList = new ArrayList<>();


        // First time, generate info
        final int locationListSize = locationsList.size();
        if ((dynamicLocations == null) && (locationListSize > 0)) {
            dynamicLocations = new ArrayList<>(locationListSize);

            for (int i = 0; i < locationListSize; i++) {
                final MPLocation Location = locationsList.get(i);

                double ang = (i * 10.0) % 360.0;

                dynamicLocations.add(i, new DynaLocation(Location.getLatLng(), ang));
            }
        }

        for (int i = 0, LocationCount = locationsList.size(); i < LocationCount; i++) {
            final MPLocation p = locationsList.get(i);
            // "Update" a Location MPLocation by using the copy/edit builder
            final MPLocation.Builder updatedLoc = new MPLocation.Builder(p);

            final DynaLocation dp = dynamicLocations.get(i);
            final LatLng dpOldPos = dp.pos;
            double newHeading = dp.heading;
            LatLng newPos = SphericalUtil.computeOffset(dpOldPos, 2, dp.heading);

            // Check limits, if the new position is outside the limit, then we will generate a new heading and calculate a new offset
            if ((Math.abs(BASE_POSITION.latitude - newPos.latitude) > RANGE_MAX_LAT_OFFSET) ||
                    (Math.abs(BASE_POSITION.longitude - newPos.longitude) > RANGE_MAX_LNG_OFFSET)) {
                newHeading += (180.0 + (random.nextInt() * 15));
                newHeading = newHeading % 360;
                newPos = SphericalUtil.computeOffset(newPos, 1, newHeading);
            }

            dp.pos = newPos;
            dp.heading = newHeading;

            // set the new position with an animation time
            updatedLoc.setPosition(dp.pos, 1000);
            // Add the updated Location to the updatedLoc list
            updatedList.add(updatedLoc.build());
        }
        // Update the current locations list
        locationsList.clear();
        locationsList.addAll(updatedList);
        // Give a notification
        notifyUpdateLocations(updatedList);

    }

    /***
     Create a method called `generateLocations`. Iterate numberOfPeople and for each iteration create:
     * An MPLocation Builder with an id
     * A random position according to the 'randomizeStartingPosition' parameter
     * A name
     * A type - later used to style the location
     * A floor Index
     * A building
     ***/
    @NonNull
    private List<MPLocation> generateLocations( boolean randomizeStartingPosition )
    {
        final List<MPLocation> peopleLocations = new ArrayList<>( LOCATIONS_COUNT );

        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final boolean gotBuildingData = buildingCollection != null;

        final int avatarIconsCount = peopleAvatars.length;
        int avatarCurrentIndex = 0;

        for ( int i = 0; i < LOCATIONS_COUNT; i++) {

            final String personName = getPersonName();
            final LatLng personPosition;
            if (randomizeStartingPosition) {
                personPosition = getRandomPosition();
            } else {
                personPosition = BASE_POSITION;
            }

            final MPLocation.Builder locBuilder = new MPLocation.Builder(""+ LOCATION_SOURCE_ID + i);
            locBuilder.setPosition(personPosition).
                    setName(personName).
                    setType( LOCATION_TYPE );

            // give an icon to the Location
            final @DrawableRes int currentAvatarIcon = peopleAvatars[avatarCurrentIndex];
            avatarCurrentIndex = (avatarCurrentIndex + 1) % avatarIconsCount;
            locBuilder.setVectorDrawableIcon(currentAvatarIcon, 32, 32);

            if (gotBuildingData) {
                // Find a building at this location (personPosition)
                final Building building = buildingCollection.getBuilding(personPosition);
                if (building != null) {

                    // Building found at this location, get the list of floors in it
                    final List<Floor> floors = building.getFloors();

                    // Choose a random floor
                    final Floor floor = floors.get((int) (Math.random() * (floors.size())));

                    // Set the Location floor
                    locBuilder.setFloor((floor != null) ? floor.getZIndex() : Floor.DEFAULT_GROUND_FLOOR_INDEX);

                    // Set the building name where this Location is in
                    locBuilder.setBuilding(building.getName());
                } else {
                    // If this location was outside a building, set its floor/z index to zero (ground floor)
                    locBuilder.setFloor(Floor.DEFAULT_GROUND_FLOOR_INDEX);
                }
            } else {
                // If this location was outside a building, set its floor/z index to zero (ground floor)
                locBuilder.setFloor(Floor.DEFAULT_GROUND_FLOOR_INDEX);
            }

            peopleLocations.add(locBuilder.build());
        }

        return peopleLocations;
    }

    /***
     Create a method called `getPersonName` that simply just returns a random name selected from the arrays below
     ***/
    // lists of names and last names
    private final String[] FIRST_NAMES = {"John", "Joe", "Javier", "Mike", "Janet", "Susan", "Cristina", "Michelle"};
    private final String[] LAST_NAMES = {"Smith", "Jones", "Andersson", "Perry", "Brown", "Hill", "Moore", "Baker"};

    private String getPersonName() {
        final int firstNameIndex = random.nextInt(FIRST_NAMES.length);
        final int lastNameIndex = random.nextInt(LAST_NAMES.length);

        return String.format("%1s %2s", FIRST_NAMES[firstNameIndex], LAST_NAMES[lastNameIndex]);
    }

    /***
     Create a method called `getRandomPosition` that simply just returns a random LatLng (here within proximity of the demo venue)
     ***/
    private LatLng getRandomPosition() {
        final double lat = BASE_POSITION.latitude + (-4 + random.nextInt(20)) * 0.000005;
        final double lng = BASE_POSITION.longitude + (-4 + random.nextInt(20)) * 0.000010;

        return new LatLng(lat, lng);
    }

    /***
     Create a method called `notifyUpdateLocations` to loop all the observers and notify them with an update
     ***/
    private void notifyUpdateLocations(List<MPLocation> updatedLocations) {
        for (int i = observers.size(); --i >= 0; ) {
            observers.get(i).onLocationsUpdated(updatedLocations, this);
        }
    }

    /***
     The same thing for notifying observers with new status
     Create a method called `notifyLocationStatusChanged` to loop all the observers and notify them with a status change
     ***/
    private void notifyLocationStatusChanged(@NonNull MPLocationSourceStatus prevStatus, @NonNull MPLocationSourceStatus newStatus) {
        for (int i = observers.size(); --i >= 0; ) {
            observers.get(i).onStatusChanged(newStatus, this);
        }
    }

    /***
      Sets the internal state and notifies a status changed message if applies
     ***/
    private void setStatus(@NonNull MPLocationSourceStatus newStatus) {
        MPLocationSourceStatus cStatus = status;

        if (cStatus != newStatus) {
            status = newStatus;
            notifyLocationStatusChanged(cStatus, newStatus);
        }
    }

    /***
     Implement the MPLocationSource method `getLocations`.
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
     In [Part 2](locationdatasourcesbatteriesLocationDataSource) we will create a data source that shows how we can use data sources to show different states of POIs on a map.
     ***/
    //****
}
