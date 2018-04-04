package com.mapsindoors.customfloorselectordemo.floorselectorcomponent;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.mapsindoors.BuildConfig;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.IFloorSelector;
import com.mapsindoors.mapssdk.OnFloorSelectedListener;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.models.Building;
import com.mapsindoors.mapssdk.models.Floor;
import com.mapsindoors.mapssdk.models.FloorBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * MapFloorSelector
 * MapsIndoorsDemo
 *
 * Created by Jose J Varó on 2/28/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class MapFloorSelector extends FrameLayout
		implements
		IFloorSelector
{
	public final String TAG = MapFloorSelector.class.getSimpleName();

	private static final int FADE_IN_ANIM_TIME_IN_MS = 500;
	private static final int FADE_OUT_ANIM_TIME_IN_MS = 500;

	public static final int FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL = (1 << 0); // 0001
	public static final int FLAG_DISABLE_AUTO_POPULATE          = (1 << 1); // 0010
	public static final int FLAG_DISABLE_AUTO_FLOOR_CHANGE      = (1 << 2); // 0100


	OnFloorSelectedListener mFloorSelectedListener;
	int mCurrentFloorIndex;
	private ViewPropertyAnimatorCompat mAnimator;


	private MapFloorSelectorAdapter mListAdapter;

	private boolean mWillShowView = true;

	private boolean mWillShowViewPrev, mShowViewCancelled;

	/** Set in the populateList methods */
	private boolean mHasFloorsToShow;

	private int mFlags;

	ListView mFloorSelectorListView;


	View bottomScrollGradient;
	View topScrollGradient;

	float currentZoomLevel;

	public MapFloorSelector( Context context )
	{
		super( context );
		init( context );
	}

	public MapFloorSelector( Context context, @Nullable AttributeSet attrs )
	{
		super( context, attrs );
		init( context );
	}

	public MapFloorSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		init( context );
	}

	@TargetApi( Build.VERSION_CODES.LOLLIPOP )
	public MapFloorSelector( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
		init( context );
	}

	private void init(Context context)
	{
		inflate( context, R.layout.control_mapsindoors_floor_selector, this );

		mCurrentFloorIndex = Integer.MAX_VALUE;
		mAnimator = null;
		mFlags = 0;

		//if( !isInEditMode() )
		{

			bottomScrollGradient = findViewById(R.id.bottom_gradient);
			topScrollGradient = findViewById(R.id.top_gradient);

			mFloorSelectorListView = findViewById( R.id.mapspeople_floor_selector_list );
			mListAdapter = new MapFloorSelectorAdapter( context, R.layout.control_mapsindoors_floor_selector_button );

			ViewTreeObserver observer = mFloorSelectorListView.getViewTreeObserver();

			mFloorSelectorListView.setAdapter( mListAdapter );

			int a= 1;

			observer.addOnGlobalLayoutListener( () -> {
				if (willMyListScroll()) {
					bottomScrollGradient.setVisibility(VISIBLE);
					topScrollGradient.setVisibility(VISIBLE);

				} else{
					bottomScrollGradient.setVisibility(INVISIBLE);
					topScrollGradient.setVisibility(INVISIBLE);
				}

			} );


			mFloorSelectorListView.setOnItemClickListener( ( parent, view, position, id ) -> {
				int newIndex = (int) view.getTag();
				if( newIndex != mCurrentFloorIndex ) {

					setFloorInternal( newIndex );

					if( mFloorSelectedListener != null) {
						mFloorSelectedListener.onFloorSelected( newIndex );
					}
				}
			} );

			show( false, false );
		}
	}

	public void setFlags(int flags)
	{
		mFlags = flags;
	}

	public void addFlags(int flags)
	{
		mFlags |= flags;
	}

	public void clearFlags(int flags)
	{
		mFlags &= ~flags;
	}

	public int getFlags()
	{
		return mFlags;
	}


	//region Implements IFloorSelector
	@Override
	public void setOnFloorSelectedListener( @Nullable OnFloorSelectedListener callback )
	{
		mFloorSelectedListener = callback;
	}

	@Override
	public void populateList( @Nullable Building building ) {

		int testVar = mFlags & FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL;
		if( building != null && building.getFloors() != null ) {
			if( BuildConfig.DEBUG ) {
				if( building.getFloors().isEmpty() ) {
					dbglog.Log( TAG, "" );
				}
			}

			populateListInternal( building.getFloors() );


			if( testVar == 0 ) {
				show( true, true );
			}
			mHasFloorsToShow = true;
		}
		else {
		//	if( testVar == 0 ) {
				show( false, true );
		//	}
			mHasFloorsToShow = false;

		}
	}

	public void onMapZoomLevelChanged(float zoomLevel) {

		currentZoomLevel = zoomLevel;

		if(zoomLevel >= (18f )){

			//setFlags(0);
			if(mHasFloorsToShow)
				show( true, true );
		}
		else{
			//setFlags(1);
			show( false, true );

		}


	}

	@Override
	public void populateList(@Nullable Building building, @Nullable List<Building> buildingList )
	{
		List<FloorBase> floorSelectorEntries = new ArrayList<>();

		if( buildingList != null )
		{
			for( Building b : buildingList )
			{
				for( Floor f : b.getFloors() )
				{
					boolean doAdd = true;
					int cFloorIndex = f.getZIndex();

					for( FloorBase fb : floorSelectorEntries ) {
						if( fb.getZIndex() == cFloorIndex ) {
							doAdd = false;
							break;
						}
					}

					if( doAdd ) {
						floorSelectorEntries.add( f );
					}
				}
			}
		}

		populateListInternal( floorSelectorEntries );

		boolean gotFloors = building!=null;
		if( (mFlags & FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL) == 0 )
		{
			show( gotFloors, true );
		}
		else
		{
			//mHasFloorsToShow = gotFloors;
		}
	}

	@Override
	public void addToView( @NonNull ViewGroup view ){}

	@Override
	public void setFloor( int floorIndex ) {
		setFloorInternal( floorIndex );
	}

	@Override
	public int getCurrentFloorIndex()
	{
		return (mCurrentFloorIndex == Integer.MAX_VALUE) ? 0 : mCurrentFloorIndex;
	}

	@Override
	public boolean isAutoPopulateEnabled() {
		return (mFlags & FLAG_DISABLE_AUTO_POPULATE) == 0;
	}

	@Override
	public boolean isAutoFloorChangeEnabled() {
		return (mFlags & FLAG_DISABLE_AUTO_FLOOR_CHANGE) == 0;
	}


	/**
	 * Note: isAutoPopulateEnabled() and isAutoFloorChangeEnabled() are always true now.
	 *       Take them into account if that changes
	 *
	 * @param show       True to show, false to hide
	 * @param animated
	 */
	@Override
	public void show( boolean show, boolean animated ) {


		// in case the floor selector is aleready in the wished state do nothing
		if(mWillShowView == show || (currentZoomLevel < 18f && show))
			return;

		if( (((mFlags & FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL) != 0) && show)
			//|| !mHasFloorsToShow
				) {
			return;
		}

		float cAlpha = this.getAlpha();
		mWillShowViewPrev = mWillShowView;
		mWillShowView = show;

		if( !animated ) {
			if( show && (cAlpha < 0.1f) ) {
				setAlpha( 1f );
				setVisible( true );
			}
			else if( !show && (cAlpha > 0.9f) ) {
				setAlpha( 0f );
				setVisible( false );
			}
		}
		else {
			mAnimator = (mAnimator != null)
					? mAnimator
					: ViewCompat.animate( this );

			// Check if an ongoing anim has to be cancelled
			//mShowViewCancelled
			//mAnimator.cancel();

			if( show && (cAlpha < 0.1f) ) {
				// Fade in anim setup
				mAnimator.
						alpha( 1f ).
						setDuration( FADE_IN_ANIM_TIME_IN_MS ).
						setListener( mVisAnimatorListener ).
						start();
			}
			else if( !show && (cAlpha > 0.9f) ) {
				// Fade out anim setup
				mAnimator.
						alpha( 0f ).
						setDuration( FADE_OUT_ANIM_TIME_IN_MS ).
						setListener( mVisAnimatorListener ).
						start();
			}
		}
	}

	@Override
	public boolean isVisible() {
		return !((getAlpha() < 0.1) && (getVisibility() == GONE));
	}
	//endregion


	private void setFloorInternal( int floorIndex )
	{
		if( mCurrentFloorIndex != floorIndex ) {
			mCurrentFloorIndex = floorIndex;
		}

		refreshUI();
	}



	private void populateListInternal( List<?> floors )
	{

		ArrayList<FloorBase> fbList = new ArrayList<>();

		int floorCount = floors.size();
		for( int i = 0; i < floorCount; i++ ) {
			fbList.add( (FloorBase)floors.get( i ) );
		}

		if( floorCount == 0 )
		{
			dbglog.Log( TAG,"");
			return;
		}

		Collections.sort( fbList, FloorBase::compareTo );

		mListAdapter.setList( fbList );

		int fbLowestZIndex = fbList.get( 0 ).getZIndex();
		int fbHighestZIndex = fbList.get( floorCount - 1 ).getZIndex();

		if( getCurrentFloorIndex() < fbLowestZIndex )
		{
			//If the current floor is lower than the lowest existing floor in this new building, we need to select a new floor

			//Selecting the lowest possible floor
			setFloorInternal( fbLowestZIndex );
			if( mFloorSelectedListener != null) {
				mFloorSelectedListener.onFloorSelected( fbLowestZIndex );
			}
		}
		else
		{
			//If the current floor is higher than the highest existing floor in this new building, we need to select a new floor
			if( getCurrentFloorIndex() > fbHighestZIndex )
			{
				//Selecting the lowest possible floor
				setFloorInternal( fbHighestZIndex );
				if( mFloorSelectedListener != null) {
					mFloorSelectedListener.onFloorSelected( fbHighestZIndex );
				}
			}
			else
			{
				refreshUI();
			}
		}
	}


	//region UI
	private void refreshUI() {
//		if( isVisible() ) {
			int pos = mListAdapter.setSelectedButtonWithFloorValue( getCurrentFloorIndex() );
			mFloorSelectorListView.smoothScrollToPosition( pos );
//		}
	}

	private ViewPropertyAnimatorListener mVisAnimatorListener = new ViewPropertyAnimatorListener() {
		@Override
		public void onAnimationStart( View view ) {
			if( getVisibility() != View.VISIBLE ) {
				setVisible( true );
			}
		}

		@Override
		public void onAnimationEnd( View view ) {
			if( !mWillShowView && (getVisibility() == View.VISIBLE) ) {
				setVisible( false );
			}
		}

		@Override
		public void onAnimationCancel( View view ) {
			mShowViewCancelled = true;
		}
	};

	private void setVisible( boolean visible ) {
		setVisibility( visible ? View.VISIBLE : View.GONE );
	}

	boolean willMyListScroll() {

		if(mFloorSelectorListView.getChildAt(0) !=  null){
			int realSizeOfListView  = mFloorSelectorListView.getChildAt(0).getHeight() * mListAdapter.getCount();
			int currentSizeOfListView = mFloorSelectorListView.getHeight();

			return realSizeOfListView > currentSizeOfListView;
		}

		return false;

	}

	//endregion
}
