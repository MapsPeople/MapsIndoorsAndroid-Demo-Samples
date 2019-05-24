package com.mapsindoors;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mapsindoors.changedisplaysettingdemo.ChangeDisplaySettingsFragment;
import com.mapsindoors.changefloordemo.ChangeFloorFragment;
import com.mapsindoors.customfloorselectordemo.CustomFloorSelectorFragment;
import com.mapsindoors.locationclustering.LocationClusteringFragment;
import com.mapsindoors.locationdatasource.LocationDataSourcesFragment;
import com.mapsindoors.locationdetailsdemo.LocationDetailsFragment;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.multipledatasets.MultiSolutionsFragment;
import com.mapsindoors.multipledatasets.SolutionSelectorFragment;
import com.mapsindoors.searchmapdemo.SearchFragment;
import com.mapsindoors.searchmapdemo.SearchMapFragment;
import com.mapsindoors.showbuildingdemo.ShowBuildingFragment;
import com.mapsindoors.showlocationdemo.ShowLocationFragment;
import com.mapsindoors.showmultiplelocations.ShowMultipleLocationsFragment;
import com.mapsindoors.showroutedemo.ShowRouteFragment;
import com.mapsindoors.showuserLocation.ShowUserLocationFragment;
import com.mapsindoors.showvenuedemo.ShowVenueFragment;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;


public class MainActivity extends AppCompatActivity
        implements
            NavigationView.OnNavigationItemSelectedListener,
            SearchFragment.OnFragmentInteractionListener,
            SearchMapFragment.OnFragmentInteractionListener,
            SolutionSelectorFragment.OnFragmentInteractionListener
{

    public static final int DEMO_ITEM_SHOW_LOCATION           = 0;
    public static final int DEMO_ITEM_LOCATION_DETAILS        = 1;
    public static final int DEMO_ITEM_SHOW_MULTIPLE_LOCATIONS = 2;
    public static final int DEMO_ITEM_SHOW_ROUTE              = 3;
    public static final int DEMO_ITEM_SHOW_VENUE              = 4;
    public static final int DEMO_ITEM_SHOW_BUILDING           = 5;
    public static final int DEMO_ITEM_SHOW_FLOOR              = 6;
    public static final int DEMO_ITEM_CHANGE_DISPLAY_SETTING  = 7;
    public static final int DEMO_ITEM_CUSTOM_FLOOR_SELECTOR   = 8;
    public static final int DEMO_ITEM_SHOW_MY_LOCATION        = 9;
    public static final int DEMO_ITEM_MULTIPLE_DATASETS       = 10;
    public static final int DEMO_ITEM_SEARCH_MAP              = 11;
    public static final int DEMO_ITEM_LOCATIONS_DATA_SOURCES  = 12;
    public static final int DEMO_ITEM_MARKER_CLUSTERING       = 13;



    NavigationView mNavigationView;
    SearchMapFragment mSearchMapFragment;


    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        mNavigationView = findViewById( R.id.nav_view );
        mNavigationView.setNavigationItemSelectedListener( this );

        // showing the first demo
        onNavigationItemSelected( mNavigationView.getMenu().getItem( DEMO_ITEM_SHOW_LOCATION ) );
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        if( drawer.isDrawerOpen( GravityCompat.START ) ) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if( id == R.id.action_settings ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onNavigationItemSelected( @NonNull MenuItem item )
    {
        // Handle navigation view item clicks here.
        final Fragment fragment;

        switch(item.getItemId()) {
            case R.id.show_location_item:
            default:
                fragment = ShowLocationFragment.newInstance();
                break;

            case R.id.show_multiple_locations_item:
                fragment = ShowMultipleLocationsFragment.newInstance();
                break;

            case R.id.show_location_details_item:
                fragment = LocationDetailsFragment.newInstance();
                break;

            case R.id.show_building_item:
                fragment = ShowBuildingFragment.newInstance();
                break;

            case R.id.show_venue_item:
                fragment = ShowVenueFragment.newInstance();
                break;

            case R.id.show_my_location_item:
                fragment = ShowUserLocationFragment.newInstance();
                break;

            case R.id.show_floor_item:
                fragment = ChangeFloorFragment.newInstance();
                break;

            case R.id.show_route_demo_item:
                fragment = ShowRouteFragment.newInstance();
                break;

            case R.id.custom_floor_selector_item:
                fragment = CustomFloorSelectorFragment.newInstance();
                break;

            case R.id.change_display_setting_item:
                fragment = ChangeDisplaySettingsFragment.newInstance();
                break;

            case R.id.search_map_item :
               fragment =  mSearchMapFragment = SearchMapFragment.newInstance();
                break;

            case R.id.multiple_dataset_item :
                fragment  = SolutionSelectorFragment.newInstance();
                break;

            case R.id.location_data_source_item :
                fragment  = LocationDataSourcesFragment.newInstance();
                break;

            case R.id.clustering_item :
                fragment  = LocationClusteringFragment.newInstance();
                break;
        }

        attachFragmentToActivity( fragment );

        // Set action bar title
        setTitle( item.getTitle() );
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    @Override
    public void onUserSelectedLocation( @Nullable MPLocation loc )
    {
        // Insert the fragment by replacing any existing fragment
        attachFragmentToActivity( mSearchMapFragment );

        mSearchMapFragment.selectLocation( loc );
    }

    @Override
    public void onSearchButtonClick()
    {
        attachFragmentToActivity( SearchFragment.newInstance() );
    }

    @Override
    public void onSolutionChosen()
    {
        final Fragment multiSolutionFrag = MultiSolutionsFragment.newInstance();

        attachFragmentToActivity( multiSolutionFrag );
    }

    void attachFragmentToActivity( @NonNull Fragment fragment )
    {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().
                replace( R.id.flContent, fragment ).
                setTransition( TRANSIT_FRAGMENT_FADE ).
                commit();
    }
}
