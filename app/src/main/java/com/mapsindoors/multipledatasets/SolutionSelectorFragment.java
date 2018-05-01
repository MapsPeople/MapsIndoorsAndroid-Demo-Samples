package com.mapsindoors.multipledatasets;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mapsindoors.R;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.errors.MIError;


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
    public static SolutionSelectorFragment newInstance() {
        final SolutionSelectorFragment fragment = new SolutionSelectorFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mainView = inflater.inflate( R.layout.fragment_solution_selector, container, false );

        setupView();

        return mainView;
    }

    void setupView()
    {
        buttonMP  = mainView.findViewById( R.id.btn_mp );
        buttonAAU  = mainView.findViewById( R.id.btn_aau );

        buttonMP.setOnClickListener( v -> {

            if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.mi_api_key) ) )
            {
                MapsIndoors.setAPIKey( getString( R.string.mi_api_key)  );
            }

            MapsIndoors.synchronizeContent( this::dataSyncDone);
        });

        buttonAAU.setOnClickListener( v -> {

            if( !MapsIndoors.getAPIKey().equalsIgnoreCase( getString( R.string.aau_api_key) ) )
            {
                MapsIndoors.setAPIKey( getString( R.string.aau_api_key)  );
            }

            MapsIndoors.synchronizeContent( this::dataSyncDone);
        });

    }

    void dataSyncDone( MIError error )
    {
        new Handler( Looper.getMainLooper() ).post( () -> {

            // if(error == null){
            mListener.onSolutionChoosen();
           /* }else {
                Toast.makeText(getContext() , "An error occurred with the message: " + error.message, Toast.LENGTH_SHORT).show();
            }*/

        } );
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
         void onSolutionChoosen();
    }
}
