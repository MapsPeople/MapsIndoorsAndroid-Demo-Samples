package com.mapsindoors.customfloorselectordemo.floorselectorcomponent;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mapsindoors.DemoApplication;
import com.mapsindoors.R;
import com.mapsindoors.mapssdk.models.FloorBase;

import java.util.ArrayList;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 2/28/2017.
 */

public class MapFloorSelectorAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<FloorBase> mItemList;
    private int mSelectedButtonIndex;


    MapFloorSelectorAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);

        mContext = context;
        mItemList = new ArrayList<>();
        mSelectedButtonIndex = 0;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.control_mapsindoors_floor_selector_button, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = view.findViewById(R.id.mapspeople_floor_selector_btn);

        Resources res = mContext.getResources();
        Resources.Theme appTheme = DemoApplication.getInstance().getTheme();

        if (position != mSelectedButtonIndex) {

            // Non selected
            //b.setBackgroundColor( ResourcesCompat.getColor(res, R.color.white, appTheme ) );
            textView.setBackgroundColor(ResourcesCompat.getColor(res, R.color.white, appTheme));
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.grey, appTheme));
            textView.setTypeface(null, Typeface.NORMAL);

        } else {
            // Selected
            //b.setBackgroundColor( ResourcesCompat.getColor( res, R.color.cobalt_blue, appTheme ) );
            textView.setBackgroundResource(R.color.secondaryButtonColor);
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.blueGray, appTheme));
            textView.setTypeface(null, Typeface.BOLD);
        }

        String floorValue = getItem(position);
        textView.setText(floorValue);

        int floorIntValue = mItemList.get(position).getZIndex();
        view.setTag(floorIntValue);

        return view;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    public void setList(ArrayList<FloorBase> list) {
        clear();
        mItemList.clear();

        int listLen = list.size();

        ArrayList<String> iList = new ArrayList<>(listLen);

        for (int i = listLen; --i >= 0; ) {
            FloorBase fb = list.get(i);
            iList.add(String.format("%s", fb.getDisplayName()));
            mItemList.add(fb);
        }

        addAll(iList);
    }



    public int setSelectedButtonWithFloorValue(int floorValue) {
        for (int i = 0, aLen = mItemList.size(); i < aLen; i++) {
            if (mItemList.get(i).getZIndex() == floorValue) {
                mSelectedButtonIndex = i;
                notifyDataSetChanged();
                return i;
            }
        }
        return -1;
    }
}
