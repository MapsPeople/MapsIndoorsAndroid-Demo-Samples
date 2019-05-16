package com.mapsindoors.mapviewdemo;

import android.support.v4.app.Fragment;


public class MapViewFragment extends Fragment
{
//    MapControl mMapControl;
//    GoogleMap mGoogleMap;
//
//    SupportMapFragment mMapFragment;
//    TextView detailsTextView;
//
//    static final LatLng VENUE_LAT_LNG = new LatLng( 57.05813067, 9.95058065 );
//    //
//
//    public MapViewFragment()
//    {
//        // Required empty public constructor
//    }
//
//    @NonNull
//    public static MapViewFragment newInstance()
//    {
//        return new MapViewFragment();
//    }
//
//
//    //region FRAGMENT LIFECYCLE
//    @Override
//    @Nullable
//    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
//    {
//        // Inflate the layout for this fragment
//        return inflater.inflate( R.layout.fragment_show_location_details, container, false );
//    }
//
//    @Override
//    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState )
//    {
//        super.onViewCreated( view, savedInstanceState );
//
//        setupView( view );
//    }
//
//    @Override
//    public void onDestroyView()
//    {
//        if( mMapControl != null ) {
//            mMapControl.onDestroy();
//        }
//
//        super.onDestroyView();
//    }
//    //endregion
//
//
//    private void setupView( View rootView )
//    {
//        FragmentManager fm = getChildFragmentManager();
//
//        detailsTextView = rootView.findViewById( R.id.details_text_view );
//
//        mMapFragment = (SupportMapFragment) fm.findFragmentById( R.id.mapfragment );
//
//        mMapFragment.getMapAsync( mOnMapReadyCallback );
//    }
//
//    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
//        @Override
//        public void onMapReady( GoogleMap googleMap )
//        {
//            mGoogleMap = googleMap;
//            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 13.0f ) );
//
//            setupMapsIndoors();
//        }
//    };
//
//    void setupMapsIndoors()
//    {
//        if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key ) ) )
//        {
//            MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );
//        }
//
//        MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );
//        if( getActivity() == null )
//        {
//            return;
//        }
//
//        mMapControl = new MapControl( getActivity() );
//        mMapControl.setGoogleMap( mGoogleMap, mMapFragment.getView() );
//
//        mMapControl.setOnMarkerClickListener( marker -> {
//
//            final MPLocation loc = mMapControl.getLocation( marker );
//            if( loc != null )
//            {
//                marker.showInfoWindow();
//
//                if( detailsTextView.getVisibility() != View.VISIBLE )
//                {
//                    detailsTextView.setVisibility( View.VISIBLE );
//                }
//
//                detailsTextView.setText( "Name: " + loc.getName() + "\nDescription: " + loc.getDescription() );
//            }
//
//            return true;
//        } );
//
//        mMapControl.init( miError -> {
//
//            if( miError == null )
//            {
//                mMapControl.selectFloor( 1 );
//                mGoogleMap.animateCamera( CameraUpdateFactory.newLatLngZoom( VENUE_LAT_LNG, 20f ) );
//            }
//        });
//    }
}
