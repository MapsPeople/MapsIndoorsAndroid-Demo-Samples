package com.mapsindoors.searchmapdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.searchmapdemo.adapter.IconTextListAdapter;

import java.util.ArrayList;

/***
 ---
 title: Create a Search Experience with MapsIndoors - Part 1
 ---

 This is an example of creating a simple search experience using MapsIndoors. We will create a map with a search button that leads to another Fragment that handles the search and selection. On selection of a location, we go back to the map and shows the selected location on the map.

 We will start by creating a simple search controller that handles search and selection of MapsIndoors locations

 Declare a listener for our location selection with a `onUserSelectedLocation` method
 ***/
public class SearchFragment extends Fragment {

    static final int VIEW_FLIPPER_ITEM_LIST    = 0;
    static final int VIEW_FLIPPER_PROGRESS_BAR = 1;

    /***
     Setup member variables for `SearchFragment`:
     * The selection listener
     * A List View to show the search result
     * Some view components
     ***/
    OnFragmentInteractionListener mListener;
    ListView mMainMenuList;


    View mMainView;
    EditText mSearchEditTextView;
    ImageButton mSearchClearBtn;
    IconTextListAdapter mListAdapter;
    ViewFlipper mViewFlipper;
    ImageButton mBackButton;
    //

    boolean mIsMenuCleared = false;

    String mLastSearchText;
    private Handler searchHandler;


    public SearchFragment()
    {
        // Required empty public constructor
    }

    @Override
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {

        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_search, container, false);
        }
        return mMainView;
    }

    @Override
    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewFlipper = view.findViewById(R.id.directionsfullmenu_itemlist_viewflipper);
        mMainMenuList = view.findViewById(R.id.directionsfullmenu_itemlist);

        mSearchEditTextView = view.findViewById(R.id.search_fragment_edittext_search);

        mSearchClearBtn = view.findViewById(R.id.directionsfullmenu_search_clear_btn);

        mBackButton = view.findViewById(R.id.directionsfullmenusearch_back_button);

        init();
    }
    //endregion


    public void init() {
        /***
         Init and setup the listView.
         ***/
        mListAdapter = new IconTextListAdapter(getContext(),new ArrayList<>());

        mMainMenuList.setAdapter(mListAdapter);
        mMainMenuList.setClickable( true );
        mMainMenuList.setOnItemClickListener(mAdapterViewOnItemClickListener);
        mMainMenuList.invalidate();


        /***
         Init and setup the view components for a better search experience.
         ***/
        //
        /*** Note: Creating a TextWatcher as it's needed for software keyboard support. ***/
        mSearchEditTextView.addTextChangedListener(mEditTextViewTextWatcher);
        mSearchEditTextView.setOnFocusChangeListener(mEditTextViewOnFocusChangeListener);

        /*** Close keyboard and search when user presses search on the keyboard: ***/
        mSearchEditTextView.setOnEditorActionListener(mEditTextViewOnEditorActionListener);

        /***Close keyboard and search when user presses enter: ***/
        mSearchEditTextView.setOnKeyListener(mEditTextOnKeyListener);

        /*** Clear search button ***/
        mSearchClearBtn.setOnClickListener(mClearSearchButtonClickListener);
        mSearchClearBtn.setOnFocusChangeListener(mClearSearchButtonFocusChangeListener);

        //

        mIsMenuCleared = true;
    }

    TextWatcher mEditTextViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged( CharSequence s, int start, int before, int count )
        {
            String text = mSearchEditTextView.getText().toString();
            if( !TextUtils.isEmpty( text ) ) {
                if( !text.isEmpty() && text.charAt( 0 ) == ' ' ) {
                    mSearchEditTextView.setText( text.trim() );
                }
            } else {
                runClearSearchButtonClickAction();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

            //Only start searching if the user wrote something to look for
            if (!TextUtils.isEmpty(s)) {

                mIsMenuCleared = false;

                startSearchTimer();
            }
        }
    };

    View.OnFocusChangeListener mEditTextViewOnFocusChangeListener = new View.OnFocusChangeListener()
    {
        @Override
        public void onFocusChange( View view, boolean hasFocus )
        {
            if( hasFocus ) {
                mSearchEditTextView.getText().clear();
                setSearchClearBtnActive( true );
                openKeyboard();
            }
        }
    };

    // Close keyboard and search when user presses search on the keyboard
    TextView.OnEditorActionListener mEditTextViewOnEditorActionListener = ( v, actionId, event ) -> {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            closeKeyboard();
            handled = true;
        }
        return handled;
    };

    //Close keyboard and search when user presses enter
    View.OnKeyListener mEditTextOnKeyListener = ( view, keyCode, keyEvent ) -> {

        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    closeKeyboard();
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    break;
                default:
                    startSearchTimer();
            }
        }
        return false;
    };

    private void setFocusOnSearchBox() {
        mSearchEditTextView.post( () -> {
            mSearchEditTextView.requestFocusFromTouch();

            final Activity context = getActivity();
            if( context != null ) {
                final InputMethodManager lManager = (InputMethodManager) context.getSystemService( Context.INPUT_METHOD_SERVICE );

                if( lManager != null ) {
                    lManager.showSoftInput( mSearchEditTextView, 0 );
                }
            }
        } );
    }
    //endregion


    public void setSearchClearBtnActive(boolean exitActive) {
        mSearchClearBtn.setVisibility(exitActive ? View.VISIBLE : View.INVISIBLE);
    }


    //region Clear Search button
    /**
     *
     */
    View.OnClickListener mClearSearchButtonClickListener = v -> runClearSearchButtonClickAction();

    /**
     *
     */
    View.OnFocusChangeListener mClearSearchButtonFocusChangeListener = ( view, hasFocus ) -> {
        if (hasFocus) {
            //Exit button pressed. Close the keyboard and go back to default - (viewing types)
            runClearSearchButtonClickAction();
        }
    };

    /**
     * Exit button pressed. Close the keyboard and go back to default - (viewing types)
     */
    void runClearSearchButtonClickAction() {
        resetSearchBox();
        setFocusOnSearchBox();
        clearSearchResultList();
        startClearListTimer();
    }

    private void resetSearchBox() {
        mSearchEditTextView.getText().clear();
        setSearchClearBtnActive(false);
    }


    void clearSearchResultList(){
        mListAdapter.clearItems();
    }
    //endregion


    void closeKeyboard()
    {
        final Activity context = getActivity();
        if( context != null ) {
            final InputMethodManager imm = (InputMethodManager) context.getSystemService( Context.INPUT_METHOD_SERVICE );

            if( imm != null ) {
                imm.hideSoftInputFromWindow( mMainView.getWindowToken(), 0 );
            }
        }
    }

    void openKeyboard()
    {
        final Activity context = getActivity();
        if( context != null ) {
            final InputMethodManager imm = (InputMethodManager) context.getSystemService( Context.INPUT_METHOD_SERVICE );

            if( imm != null ) {
                //  imm.hideSoftInputFromWindow(mMainView.getWindowToken(), 0);
                imm.toggleSoftInput( InputMethodManager.SHOW_IMPLICIT, 0 );
            }
        }
    }

    //Only search after a second of delay. Any search requests before one sec should replace the search and restart the timer.
    void startSearchTimer()
    {
        if( searchHandler != null ) {
            searchHandler.removeCallbacks( searchRunner );
        }

        searchHandler = new Handler();
        searchHandler.postDelayed( searchRunner, 1000 );
    }

    void startClearListTimer()
    {
        if( searchHandler != null ) {
            searchHandler.removeCallbacks( searchRunner );

        }
        searchHandler = new Handler();
    }

    /**
     * Non-empty search string
     */
    String mCSearchString;

    private Runnable searchRunner = new Runnable()
    {
        @Override
        public void run()
        {
            final String searchString = mSearchEditTextView.getText().toString();

            if( !TextUtils.isEmpty( searchString ) ) {
                mCSearchString = searchString.trim();
                mLastSearchText = mCSearchString;

                if( !mCSearchString.isEmpty() ) {
                    // Show as busy
                    mViewFlipper.setDisplayedChild( VIEW_FLIPPER_PROGRESS_BAR );

                    final MPQuery q = new MPQuery.Builder().
                            setQuery( mCSearchString ).
                            build();

                    final MPFilter f = new MPFilter.Builder().
                            setTake( 10 ).
                            build();

                    MapsIndoors.getLocationsAsync( q, f, ( locs, err ) -> {
                        mViewFlipper.setDisplayedChild( VIEW_FLIPPER_ITEM_LIST );
                        if( locs != null ) {
                            mListAdapter.setList( locs );
                        }
                    } );
                }
            }
        }
    };

    /***
     Whenever a user clicks a search result the 'onUserSelectedLocation' of the FragmentInteractionListener is called .
     ***/
    AdapterView.OnItemClickListener mAdapterViewOnItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
        {
            closeKeyboard();

            if( mListener != null ) {
                mListener.onUserSelectedLocation( (MPLocation) mListAdapter.getItem( position ) );
            }
        }
    };
//

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        if( context instanceof OnFragmentInteractionListener ) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    /***
     Declare an interface that will handle the communication between the fragment and the activity.
     ***/
    public interface OnFragmentInteractionListener
    {
        void onUserSelectedLocation( @Nullable MPLocation loc );
    }

    @NonNull
    public static SearchFragment newInstance()
    {
        return new SearchFragment();
    }

    /***
     In [Part 2](searchmapdemosearchmapfragment) we will create the map fragment that displays the search result.
     ***/
    //
}

