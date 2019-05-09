package com.mapsindoors.locationdatasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
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

 Create a class `PeopleDataSource` that implements `MPLocationSource`.
 ***/
public class PeopleDataSource implements MPLocationSource {


    static final int SOURCE_ID = 0x0EFACAFE;

    static final int PEOPLE_COUNT = 100;


    /***
     Add some member variables to `PeopleLocationsDataSource`.

     * `observers`: The observer objects that we will notify about changes
     * `locationsList`: A list of MPLocation - Will have the list of the MPLocations up to date
     * `locationsBuilders`: A list of MPLocation.Builder - the MPLocation builders
     * `status`: will hold the status of the Datasource
     * `PEOPLE_COUNT`: The number of people to mock
     ***/

    @NonNull
    List<MPLocationsObserver> observers = new ArrayList<>();

    List<MPLocation> locationsList = new ArrayList<>();
    List<MPLocation.Builder> locationsBuilders = new ArrayList<>();

    MPLocationSourceStatus status;
    BuildingCollection buildingCollection;

    String type;

    /***
     Create a Constructor that takes a type string. Call `generatePeoplesLocations` and `startMockingPersonsPositions`.
     ***/
    public PeopleDataSource( String type) {

        status = MPLocationSourceStatus.NOT_INITIALIZED;

        this.type = type;
    }

    /***
     Create a method called `startMockingPersonsPositions` that simply just calls `updatePositions` in the future.
     ***/
    Timer mDataUpdateTimer;

    public void startMockingPeoplePositions()
    {
        if( mDataUpdateTimer != null ) {
            mDataUpdateTimer.cancel();
            mDataUpdateTimer = null;
        }

        mDataUpdateTimer = new Timer();

        mDataUpdateTimer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run()
            {
                updatePeoplePositionsRandomly();
            }
        }, 2000, 5000 );
    }

    public void stopMockingPeoplePositions()
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
        final double lat = 57.058037 + (-4 + random.nextInt( 9 )) * 0.000005;
        final double lng = 9.950572 + (-4 + random.nextInt( 9 )) * 0.000010;

        return new LatLng( lat, lng );
    }

    public void createMockMPLocations()
    {
        buildingCollection = MapsIndoors.getBuildings();
        locationsList.clear();
        locationsList.addAll( generatePeoplesLocations( type ) );

        startMockingPeoplePositions();

        status = MPLocationSourceStatus.AVAILABLE;
    }

    /***
     Create a method called `updatePeoplePositionsRandomly`. Iterate numberOfPeople again and for each iteration:
     * Get the corresponding MPLocation Builder
     * Set a new position
     * Generate MPLocation from the MPLocation.Builder
     * Call the notifyUpdateLocations with the updated list
     ***/
    public void updatePeoplePositionsRandomly() {

        final List<MPLocation> updatedList = new ArrayList<>();

        for( int locID = 0, locCount = locationsList.size(); locID < locCount; locID++ ) {

            final MPLocation.Builder updateBuilder = locationsBuilders.get( locID );

            updateBuilder.setPosition( getRandomPosition() );
            updatedList.add( updateBuilder.build() );
        }

        locationsList.clear();
        locationsList.addAll( updatedList );

        notifyUpdateLocations( updatedList );
    }

    /***
     Create a method called `generatePersonsLocations` that takes a type string. Iterate numberOfPeople and for each iteration create:

     * An MPLocation Builder with an id
     * A random position
     * A name
     * A type - later used to style the location
     * A floor Index
     * A building
     ***/
    private List<MPLocation> generatePeoplesLocations( String type )
    {
        status = MPLocationSourceStatus.INITIALISING;

        List<MPLocation> res = new ArrayList<>();

        for( int locID = 0; locID < PEOPLE_COUNT; locID++ ) {

            final String personName = getPersonName();
            final LatLng personPosition = getRandomPosition();

            final Building building = buildingCollection.getBuilding( personPosition );
            if( building == null ) {
                continue;
            }

            final Floor initialFloor = building.getInitFloor();
            final MPLocation.Builder locBuilder = new MPLocation.Builder( "" + locID );
            locBuilder.setPosition( personPosition ).
                    setName( personName ).
                    setType( type ).
                    setFloor( (initialFloor != null) ? initialFloor.getZIndex() : 0 ).
                    setBuilding( building.getName() );

            locationsBuilders.add( locBuilder );

            res.add( locBuilder.build() );
        }

        return res;
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
        return SOURCE_ID;
    }
}
