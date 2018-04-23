package com.mapsindoors.searchmapdemo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.LocationQuery;
import com.mapsindoors.mapssdk.MPLocationsProvider;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.searchmapdemo.adapter.IconTextListAdapter;

import java.util.ArrayList;
import java.util.List;

/***
 ---
 title: Create a Search Experience with MapsIndoors - Part 1
 ---

 This is an example of creating a simple search experience using MapsIndoors. We will create a map with a search button that leads to another view controller that handles the search and selection. On selection of a location, we go back to the map and shows the selected location on the map.

 We will start by creating a simple search controller that handles search and selection of MapsIndoors locations

 Declare a protocol for our location selection with a `didSelectLocation` method
 ***/
public class SearchFragment extends Fragment {

    /***
     Setup member variables for `SearchFragment`:

     * An instance of type `LocationQuery`
     * The selection listener
     * A List View
     * Some view components
     ***/
    LocationQuery.Builder iLocsQueryBuilder;
    LocationQuery mSearchQuery;
    private OnFragmentInteractionListener mListener;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_search, container, false);
        }
        return mMainView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewFlipper = view.findViewById(R.id.directionsfullmenu_itemlist_viewflipper);
        mMainMenuList = view.findViewById(R.id.directionsfullmenu_itemlist);


        mSearchEditTextView = view.findViewById(R.id.search_fragment_edittext_search);

        mSearchClearBtn = view.findViewById(R.id.directionsfullmenu_search_clear_btn);

        mBackButton = view.findViewById(R.id.directionsfullmenusearch_back_button);

        init();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        //Note: Creating a textwatcher as it's needed for software keyboard support.
        mSearchEditTextView.addTextChangedListener(mEditTextViewTextWatcher);
        mSearchEditTextView.setOnFocusChangeListener(mEditTextViewOnFocusChangeListener);

        //Close keyboard and search when user presses search on the keyboard:
        mSearchEditTextView.setOnEditorActionListener(mEditTextViewOnEditorActionListener);

        //Close keyboard and search when user presses enter:
        mSearchEditTextView.setOnKeyListener(mEditTextOnKeyListener);

        // Clear search button
        mSearchClearBtn.setOnClickListener(mClearSearchButtonClickListener);
        mSearchClearBtn.setOnFocusChangeListener(mClearSearchButtonFocusChangeListener);

        mCSearchLocationsProvider = new MPLocationsProvider();

        // Setup the query; the search string will be set where needed
        iLocsQueryBuilder = new LocationQuery.Builder();

        mIsMenuCleared = true;
        
    }



    TextWatcher mEditTextViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = mSearchEditTextView.getText().toString();
            if (!text.isEmpty()) {
                if (text.startsWith(" ")) {
                    mSearchEditTextView.setText(text.trim());
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


    View.OnFocusChangeListener mEditTextViewOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                mSearchEditTextView.getText().clear();
                setSearchClearBtnActive(true);
                openKeyboard();
            }
        }
    };


    // Close keyboard and search when user presses search on the keyboard
    TextView.OnEditorActionListener mEditTextViewOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                closeKeyboard();
                handled = true;
            }
            return handled;
        }
    };

    //Close keyboard and search when user presses enter
    View.OnKeyListener mEditTextOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER: {
                        closeKeyboard();
                        return true;
                    }
                    case KeyEvent.KEYCODE_BACK: {
                        break;
                    }
                    default:
                        startSearchTimer();
                }
            }
            return false;
        }
    };

    private void setFocusOnSearchBox() {
        mSearchEditTextView.post( () -> {
            mSearchEditTextView.requestFocusFromTouch();
            InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            lManager.showSoftInput(mSearchEditTextView, 0);
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
    View.OnClickListener mClearSearchButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            runClearSearchButtonClickAction();

        }
    };

    /**
     *
     */
    View.OnFocusChangeListener mClearSearchButtonFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                //Exit button pressed. Close the keyboard and go back to default - (viewing types)
                runClearSearchButtonClickAction();
            }
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


    void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mMainView.getWindowToken(), 0);
    }

    void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      //  imm.hideSoftInputFromWindow(mMainView.getWindowToken(), 0);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

    }


    //Only search after a second of delay. Any search requests before one sec should replace the seach and restart the timer.
    void startSearchTimer() {
        if (searchHandler != null) {
            searchHandler.removeCallbacks(searchRunner);
        }

        searchHandler = new Handler();
        searchHandler.postDelayed(searchRunner, 1000);
    }

    void startClearListTimer() {
        if (searchHandler != null) {
            searchHandler.removeCallbacks(searchRunner);

        }
        searchHandler = new Handler();
    }


    /**
     * Non-empty search string
     */
    String mCSearchString;
    MPLocationsProvider mCSearchLocationsProvider;

    private Runnable searchRunner = new Runnable()
    {
        @Override
        public void run()
        {
            String searchString = mSearchEditTextView.getText().toString();

            if( !TextUtils.isEmpty( searchString ) )
            {

                mCSearchString = searchString.trim();
                mLastSearchText = mCSearchString;

                if( !mCSearchString.isEmpty() )
                {
                    // Show as busy
                    mViewFlipper.setDisplayedChild(1);

                            iLocsQueryBuilder.
                                    setCategories( null ).
                                    setOrderBy( LocationQuery.RELEVANCE ).
                                    setQueryMode( LocationQuery.MODE_PREFER_ONLINE );


                    mSearchQuery = iLocsQueryBuilder.build();
                    // Indoor locations - setup the search query
                    mSearchQuery.setQuery( mCSearchString );

                    mCSearchLocationsProvider.getLocationsAsync( mSearchQuery, mSearchLocationsReadyListener );
                }
            }

        }
    };





    OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener() {

        @Override
        public void onLocationsReady(@Nullable List<Location> locations, @Nullable MIError error) {

            if(locations != null){

                getActivity().runOnUiThread( () -> {
                    mViewFlipper.setDisplayedChild(0);

                    mListAdapter.setList(locations);
                } );
            }
        }

    };


    /***
     Whenever a user clicks a search result the 'onUserSelectedLocation' of the FragmentInteractionListener is called .
     ***/
    AdapterView.OnItemClickListener mAdapterViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            closeKeyboard();

            mListener.onUserSelectedLocation((Location) mListAdapter.getItem(position));

            }

    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }


    /***
     Declare an interface that will handle the communication between the fragment and the activity.
     ***/
    public interface OnFragmentInteractionListener {
        void onUserSelectedLocation(Location loc);
    }


    public static SearchFragment newInstance( ) {
        SearchFragment fragment = new SearchFragment();

        return fragment;
    }

}
