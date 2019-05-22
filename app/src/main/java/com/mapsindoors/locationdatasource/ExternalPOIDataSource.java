package com.mapsindoors.locationdatasource;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;

import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.Floor;
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
 title: Creating your own Location Source - Part 1
 ---

 > Note! This document describes a pre-release feature. We reserve the right to change this feature and the corresponding interfaces without further notice. Any mentioned SDK versions are not intended for production use.

 In this tutorial we will show how you can build a custom Location Source, representing locations of people. The people locations will be served from a mocked list in the source and displayed on a map in a view controller.

 We will start by creating our implementation of a location source.

 Create a class `ExternalPOIDataSource` that implements `MPLocationSource`.
 ***/
public class ExternalPOIDataSource implements MPLocationSource {


    static final int SOURCE_ID = 0x0EFACAFE;

    static final int POI_COUNT = 30;

    static final LatLng BASE_POSITION = new LatLng( 57.0579814,9.9504668 );

    // Position updated
    static final int DEMO_MODE_MOVING_POIS                      = 0;
    // Type updated
    static final int DEMO_MODE_ANIMATED_TYPES                   = 1;
    // Marker icon and color animated
    static final int DEMO_MODE_ANIMATED_MARKER_ICONS_AND_COLORS = 2;



    /***
     Add some member variables to `PeopleLocationsDataSource`.

     * `observers`: The observer objects that we will notify about changes
     * `locationsList`: A list of MPLocation - Will have the list of the MPLocations up to date
     * `status`: will hold the status of the Datasource
     * `POI_COUNT`: The number of people to mock
     ***/

    @NonNull
    List<MPLocationsObserver> observers;

    List<MPLocation> locationsList;

    MPLocationSourceStatus status;

    // Each instance of this class must get a different sourceId
    int locationDataSourceId;
    String type;
    int selectedDemoMode;
    // Time in ms
    int timerUpdatePeriod;

    // Static id so each id created here will be unique...
    static int cPOIId = 1;

    // Types (Display rules)
    @NonNull
    final String[] drTypes = new String[]{
            LocationDataSourcesFragment.POI_TYPE_1,
            LocationDataSourcesFragment.POI_TYPE_3
    };

    // Icons
    @DrawableRes final int[] icons = new int[]{
            R.drawable.ic_battery_20_black_24dp,
            R.drawable.ic_battery_30_black_24dp,
            R.drawable.ic_battery_50_black_24dp,
            R.drawable.ic_battery_60_black_24dp,
            R.drawable.ic_battery_80_black_24dp,
            R.drawable.ic_battery_90_black_24dp,
            R.drawable.ic_battery_full_black_24dp
    };



    /***
     Create a Constructor that takes a type string. Call `generatePOIs` and `startMockingPersonsPositions`.
     ***/
    public ExternalPOIDataSource( int demoMode, int locationDataSourceId )
    {
        this.selectedDemoMode = demoMode;
        this.locationDataSourceId = locationDataSourceId;

        switch( selectedDemoMode ) {
            case DEMO_MODE_MOVING_POIS: {
                this.type = LocationDataSourcesFragment.POI_TYPE_1;
                this.timerUpdatePeriod = 1500;
                break;
            }
            case DEMO_MODE_ANIMATED_TYPES: {
                this.type = LocationDataSourcesFragment.POI_TYPE_2;
                this.timerUpdatePeriod = 3000;
                break;
            }
            case DEMO_MODE_ANIMATED_MARKER_ICONS_AND_COLORS: {
                this.type = LocationDataSourcesFragment.POI_TYPE_3;
                this.timerUpdatePeriod = 250;
                break;
            }
        }

        this.locationsList = new ArrayList<>( POI_COUNT );
        this.observers = new ArrayList<>();

        this.status = MPLocationSourceStatus.NOT_INITIALIZED;
    }

    /**
     * Main data setup
     *
     * @return {@code true} if location data has already been setup, {@code false} otherwise
     */
    boolean setup()
    {
        if( this.status != MPLocationSourceStatus.NOT_INITIALIZED ) {
            return true;
        }

        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final boolean gotBuildings = buildingCollection != null;
        if( !gotBuildings ) {
            return false;
        }

        createMockMPLocations();
        notifyUpdateLocations( locationsList );
        setStatus( MPLocationSourceStatus.AVAILABLE );

        return true;
    }

    /***
     Create a method called `startMockingPOIsPositions` that simply just calls `updatePOIs` in the future.
     ***/
    Timer mDataUpdateTimer;

    /**
     *
     */
    public void startMockingPOIsPositions()
    {
        if( !setup() ) {
            return;
        }

        if( mDataUpdateTimer != null ) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer = null;
        }

        mDataUpdateTimer = new Timer();

        mDataUpdateTimer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run()
            {
                updatePOIs();
            }
        }, 2000, timerUpdatePeriod );
    }

    /**
     *
     */
    public void stopMockingPOIsPositions()
    {
        if( mDataUpdateTimer != null ) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer.purge();
        }
    }

    /***
     Create a method called `getRandomPosition` that simply just returns a random LatLng (here within proximity of the demo venue)
     ***/
    Random random = new Random();

    private LatLng getRandomPosition()
    {
        final double lat = BASE_POSITION.latitude  + (-4 + random.nextInt( 20 )) * 0.000005;
        final double lng = BASE_POSITION.longitude + (-4 + random.nextInt( 20 )) * 0.000010;

        return new LatLng( lat, lng );
    }


    private void createMockMPLocations()
    {
        locationsList.clear();
        locationsList.addAll( generatePOIs( type ) );
    }

    int iconCurrentIndex = 0;

    /***
     Create a method called `updatePeoplePositions`. Iterate numberOfPeople again and for each iteration:
     * Get the corresponding MPLocation Builder
     * Set a new position
     * Generate MPLocation from the MPLocation.Builder
     * Call the notifyUpdateLocations with the updated list
     ***/
    void updatePOIs()
    {
        if( !MapsIndoors.isReady() ) {
            return;
        }

        final int locCount = locationsList.size();
        final List<MPLocation> updatedList = new ArrayList<>( locCount );

        // Pick a random type
        final int availableTypesCount = drTypes.length;
        final String currentType = drTypes[ random.nextInt( availableTypesCount )];

        // Icon and tint color (animated)
        final int availableIconsCount = icons.length;
        final int iColor = ColorUtils.blendARGB( 0xff2DD855, 0xffFF3700, ((1f * iconCurrentIndex) / availableIconsCount) );
        @DrawableRes final int currentIcon = icons[iconCurrentIndex];
        iconCurrentIndex = (iconCurrentIndex < (availableIconsCount - 1)) ? (iconCurrentIndex + 1) : 0;


        for( final MPLocation p : locationsList )
        {
            final MPLocation.Builder updatedLoc = new MPLocation.Builder( p );

            switch( selectedDemoMode )
            {
                case DEMO_MODE_MOVING_POIS:
                {
                    // Change the position
                    updatedLoc.setPosition( getRandomPosition() );
                    break;
                }
                case DEMO_MODE_ANIMATED_TYPES:
                {
                    // Change the type (display rule)
                    updatedLoc.setType( currentType );
                    break;
                }
                case DEMO_MODE_ANIMATED_MARKER_ICONS_AND_COLORS:
                {
                    // Change the icon & tint color
                    updatedLoc.
                            // Use the default icon size
                            //setVectorDrawableIcon( currentIcon ).
                            // Specify a size
                            setVectorDrawableIcon( currentIcon, 32, 32 ).
                            // Specify the tint color
                            setTint( iColor )
                    ;
                    break;
                }
            }

            updatedList.add( updatedLoc.build() );
        }

        locationsList.clear();
        locationsList.addAll( updatedList );

        notifyUpdateLocations( updatedList );
    }

    /***
     Create a method called `generatePOIs` that takes a type string. Iterate numberOfPeople and for each iteration create:

     * An MPLocation Builder with an id
     * A random position
     * A name
     * A type - later used to style the location
     * A floor Index
     * A building
     ***/
    @NonNull
    private List<MPLocation> generatePOIs( @NonNull String type )
    {
        final List<MPLocation> peoplePOIs = new ArrayList<>( POI_COUNT );

        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final boolean gotBuildingData = buildingCollection != null;

        for( int i = 0; i < POI_COUNT; i++ ) {

            final String personName = getPersonName();
            final LatLng personPosition = getRandomPosition();

            final MPLocation.Builder locBuilder = new MPLocation.Builder( "" + cPOIId++ );
            locBuilder.setPosition( personPosition ).
                    setName( personName ).
                    setType( type )
                    ;

            if( gotBuildingData ) {
                // Find a building at this location (personPosition)
                final Building building = buildingCollection.getBuilding( personPosition );
                if( building != null ) {

                    // Building found at this location, get the list of floors in it
                    final List<Floor> floors = building.getFloors();

                    // Choose a random floor
                    final Floor floor = floors.get( (int) (Math.random() * (floors.size() - 1)) );

                    // Set the POI floor
                    locBuilder.setFloor( (floor != null) ? floor.getZIndex() : 0 );

                    // Set the building name where this POI is in
                    locBuilder.setBuilding( building.getName() );
                } else {
                    // If this location was outside a building, set its floor/z index to zero (ground floor)
                    locBuilder.setFloor( Floor.DEFAULT_GROUND_FLOOR_INDEX );
                }
            } else {
                // If this location was outside a building, set its floor/z index to zero (ground floor)
                locBuilder.setFloor( Floor.DEFAULT_GROUND_FLOOR_INDEX );
            }

            peoplePOIs.add( locBuilder.build() );
        }

        return peoplePOIs;
    }

    /***
     Create a method called `getPersonName` that simply just returns a random name selected from the arrays below
     ***/
    private final String[] FIRST_NAMES = {"John", "Joe", "Javier", "Mike", "Janet", "Susan", "Cristina", "Michelle"};
    private final String[] LAST_NAMES = {"Smith", "Jones", "Andersson", "Perry", "Brown", "Hill", "Moore", "Baker"};

    private String getPersonName()
    {
        final int firstNameIndex = random.nextInt( FIRST_NAMES.length );
        final int lastNameIndex = random.nextInt( LAST_NAMES.length );

        return String.format( "%1s %2s", FIRST_NAMES[firstNameIndex], LAST_NAMES[lastNameIndex] );
    }

    /***
     Create a method called `notifyUpdateLocations` to loop all the observers and notify them with an update
     ***/
    private void notifyUpdateLocations( List<MPLocation> updatedLocations )
    {
        for( int i = observers.size(); --i >= 0; ) {
            observers.get( i ).onLocationsUpdated( updatedLocations, this );
        }
    }

    void notifyLocationStatusChanged( @NonNull MPLocationSourceStatus prevStatus, @NonNull MPLocationSourceStatus newStatus )
    {
        for( int i = observers.size(); --i >= 0; ) {
            observers.get( i ).onStatusChanged( newStatus, this );
        }
    }

    /**
     * Sets the internal state and notifies a status changed message if applies
     *
     * @param newStatus The new {@link MPLocationSourceStatus}
     * @since 3.0.0
     */
    void setStatus( @NonNull MPLocationSourceStatus newStatus )
    {
        MPLocationSourceStatus cStatus =status;

        if( cStatus != newStatus ) {
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
    public void addLocationsObserver( @Nullable MPLocationsObserver observer )
    {
        if( observer != null ) {
            observers.remove( observer );
            observers.add( observer );
        }
    }

    /***
     Implement the MPLocationSource method `removeLocationObserver`.
     ***/
    @Override
    public void removeLocationsObserver( @Nullable MPLocationsObserver observer )
    {
        if( observer != null ) {
            observers.remove( observer );
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
        return locationDataSourceId;
    }
}
