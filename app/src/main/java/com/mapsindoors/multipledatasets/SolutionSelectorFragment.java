package com.mapsindoors.multipledatasets;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mapsindoors.BuildConfig;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationSourceOnStatusChangedListener;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.errors.MIError;

import java.util.List;


public class SolutionSelectorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    Button buttonMP;
    Button buttonAAU;
    View   mainView;


    private OnFragmentInteractionListener mListener;


    public SolutionSelectorFragment()
    {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    @NonNull
    public static SolutionSelectorFragment newInstance() {
        final SolutionSelectorFragment fragment = new SolutionSelectorFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    @Nullable
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        mainView = inflater.inflate( R.layout.fragment_solution_selector, container, false );

        setupView();

        return mainView;
    }

    @Override
    public void onDestroyView()
    {
        //MapsIndoors.removeLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );

        super.onDestroyView();
    }

    void setupView()
    {
        buttonMP = mainView.findViewById( R.id.btn_mp );
        buttonAAU = mainView.findViewById( R.id.btn_aau );

        buttonMP.setOnClickListener( v -> {

            if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key ) ) ) {
                MapsIndoors.setAPIKey( getString( R.string.mi_api_key ) );
            }

            MapsIndoors.addLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );
            MapsIndoors.synchronizeContent( this::dataSyncDone );
        } );

        buttonAAU.setOnClickListener( v -> {

            if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.aau_api_key ) ) ) {
                MapsIndoors.setAPIKey( getString( R.string.aau_api_key ) );
            }

            MapsIndoors.addLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );
            MapsIndoors.synchronizeContent( this::dataSyncDone );
        } );
    }

    void dataSyncDone( @Nullable MIError error )
    {
        mListener.onSolutionChosen();
    }

    final MPLocationSourceOnStatusChangedListener locationSourceOnStatusChangedListener = ( status, sourceId ) -> {
        if( status == MPLocationSourceStatus.AVAILABLE )
        {
            // Once here, location data will be available
            if( BuildConfig.DEBUG ) {}
        }
    };

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );

        if( context instanceof OnFragmentInteractionListener ) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException( context.toString() + " must implement OnFragmentInteractionListener" );
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        MapsIndoors.removeLocationSourceOnStatusChangedListener( locationSourceOnStatusChangedListener );
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
         void onSolutionChosen();
    }
}
