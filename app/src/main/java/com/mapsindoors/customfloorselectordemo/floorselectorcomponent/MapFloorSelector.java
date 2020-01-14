package com.mapsindoors.customfloorselectordemo.floorselectorcomponent;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.mapsindoors.mapssdk.FloorSelectorInterface;
import com.mapsindoors.mapssdk.OnFloorSelectionChangedListener;

import com.mapsindoors.mapssdk.Floor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MapFloorSelector extends FrameLayout implements FloorSelectorInterface
{
    boolean mWillShowView;

    private static final int FADE_IN_TIME_MS = 500;
    private static final int FADE_OUT_TIME_MS = 500;

    public static final float SHOW_ON_ZOOM_LEVEL = 12;

    OnFloorSelectionChangedListener mOnFloorSelectionChangedListener;
    private ListView mLvFloorSelector;
    ImageView mIvBottomGradient;
    ImageView mIvTopGradient;

    private List<Floor> mFloors;
    private FloorSelectorAdapter mFloorSelectorAdapter;



    /**
     * Required default constructor - just forwarding to private {@link #init()}
     *
     * @param context Default Context.
     */
    public MapFloorSelector( @NonNull Context context )
    {
        super( context );
        init();
    }

    public MapFloorSelector( @NonNull Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
        init();
    }

    public MapFloorSelector( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        init();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public MapFloorSelector( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes )
    {
        super( context, attrs, defStyleAttr, defStyleRes );
        init();
    }

    /**
     * Inflates the layout and does view finding as well as setting the adapter for the ListView
     */
    private void init(){
        inflate(getContext(), com.mapsindoors.mapssdk.R.layout.misdk_control_floor_selector,this);

        mFloors = new ArrayList<>();

        mLvFloorSelector = findViewById(com.mapsindoors.mapssdk.R.id.misdk_floor_selector_list);
        mIvBottomGradient = findViewById(com.mapsindoors.mapssdk.R.id.misdk_bottom_gradient);
        mIvTopGradient = findViewById(com.mapsindoors.mapssdk.R.id.misdk_top_gradient);

        mFloorSelectorAdapter = new FloorSelectorAdapter(getContext(), com.mapsindoors.mapssdk.R.layout.misdk_control_floor_selector_button);
        mFloorSelectorAdapter.setCallback(mFloorSelectorAdapterListener);
        mLvFloorSelector.setAdapter(mFloorSelectorAdapter);
        mLvFloorSelector.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        mLvFloorSelector.setOnItemClickListener((parent, view, position, id) -> {
            mOnFloorSelectionChangedListener.onFloorSelectionChanged(mFloors.get(position));
            mFloorSelectorAdapter.setSelectedListPosition(position);
            mFloorSelectorAdapter.notifyDataSetChanged();
        });
    }

    //region IMPLEMENTS FloorSelectorInterface
    /**
     * Returns the floor selector View, if any
     */
    @Nullable
    @Override
    public View getView()
    {
        return this;
    }

    /**
     * Sets the {@link OnFloorSelectionChangedListener} to invoke when changes occur in this view.
     * I.e. a new floor has been selected.
     * @param onFloorSelectionChangedListener - Listener to be invoked
     */
    @Override
    public void setOnFloorSelectionChangedListener(@Nullable OnFloorSelectionChangedListener onFloorSelectionChangedListener) {
        mOnFloorSelectionChangedListener = onFloorSelectionChangedListener;
    }

    /**
     * Sets the list of {@link Floor} to show in the FloorSelector
     * @param floors - List of Floors to show.
     */
    @Override
    public void setList(@Nullable List<Floor> floors) {
        mFloors.clear();
        if(floors == null){
            return;
        }
        mFloors.addAll(floors);

        Collections.reverse(mFloors);

        mFloorSelectorAdapter.setFloors(mFloors);
        mFloorSelectorAdapter.notifyDataSetChanged();
    }

    /**
     * Shows or hides the view, with/without animation, based on the implementation
     * @param show - Should the view be shown?
     * @param animated - Should the transition be animated?
     */
    @Override
    public void show(boolean show, boolean animated){
        mWillShowView = show;
        float currentAlpha = this.getAlpha();
        if(animated){
            if(show && (currentAlpha < 1.0f)){
                this.animate()
                        .alpha(1f)
                        .setDuration(FADE_IN_TIME_MS)
                        .setListener(mAnimationListener)
                        .start();
            } else if(!show && (currentAlpha > 0.1)){
                this.animate()
                        .alpha(0f)
                        .setDuration(FADE_OUT_TIME_MS)
                        .setListener(mAnimationListener)
                        .start();
            }
        } else {
            if(show && (currentAlpha < 1.0f)){
                this.setVisibility(VISIBLE);
                this.setAlpha(1.0f);
            } else {
                this.setVisibility(INVISIBLE);
                this.setAlpha(0.0f);
            }
        }
    }

    /**
     * Sets the floor selected and forwards it to the {@link FloorSelectorAdapter}
     * @param floor  Floor selected
     */
    @Override
    public void setSelectedFloor(@NonNull Floor floor){
        mFloorSelectorAdapter.setSelectedFloor(floor);
    }

    /**
     * Sets the selected floor based on a Z-index
     * @param zIndex - Z index of the new Floor to be selected
     */
    @Override
    public void setSelectedFloorByZIndex(int zIndex) {
        if((mFloors == null) || mFloors.isEmpty()){
            return;
        }
        for(Floor floor : mFloors){
            if(floor.getZIndex() == zIndex){
                setSelectedFloor(floor);
                return;
            }
        }
    }

    /**
     * Invoked when the Zoom level changes - Checks if there is a reason to show the FloorSelector
     * @param newZoomLevel - Zoom Level received by  FloorSelectorManager
     */
    @Override
    public void zoomLevelChanged(float newZoomLevel) {
        show(newZoomLevel >= SHOW_ON_ZOOM_LEVEL && !mFloors.isEmpty(), true);
    }

    /**
     * Should the floor selection change automatically
     * @return - true if the floor should change automatically, false if not.
     */
    @Override
    public boolean isAutoFloorChangeEnabled() {
        return true;
    }

    @Override
    public void setUserPositionFloor(int zIndex) {
        mFloorSelectorAdapter.setUserPositionFloor(zIndex);
        mFloorSelectorAdapter.notifyDataSetChanged();
    }
    //endregion


    /**
     * Measures the total height of children in the List
     * @return true if the combined height exceeds the height of the view itself, false if not
     */
    boolean isListScrollable(){
        View listViewChild = mLvFloorSelector.getChildAt(0);
        if(listViewChild != null){
            int totalHeightOfChildren = listViewChild.getHeight() * mFloors.size();
            int heightOfView = mLvFloorSelector.getHeight();

            return totalHeightOfChildren > heightOfView;
        }
        return false;
    }

    /**
     * Private Adapter.Callback - Basically just forwarding messages from the {@link FloorSelectorAdapter}
     * to any {@link OnFloorSelectionChangedListener} registered
     */
    private FloorSelectorAdapterListener mFloorSelectorAdapterListener = new FloorSelectorAdapterListener() {
        @Override

        public void onFloorSelectionChanged(@NonNull Floor newFloor) {
            if(mOnFloorSelectionChangedListener != null) {
                mOnFloorSelectionChangedListener.onFloorSelectionChanged(newFloor);
            }
        }
    };

    /**
     * GlobalLayoutListener used to check if the ListView in the FloorSelector is scrollable
     * and thereby should have gradient scroll indicators
     */
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mIvTopGradient.setVisibility(isListScrollable() ? VISIBLE : INVISIBLE);
            mIvBottomGradient.setVisibility(isListScrollable() ? VISIBLE : INVISIBLE);
        }
    };

    /**
     * AnimationListener used to set properties of the view before/after the animation.
     */
    private Animator.AnimatorListener mAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if(getVisibility() != View.VISIBLE){
                setVisibility(VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if(!mWillShowView && !(getVisibility() == VISIBLE)){
                setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            //Auto generated method stub
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            //Auto generated method stub
        }
    };

	//endregion
}
