package com.mapsindoors.searchmapdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapsindoors.R;
import com.mapspeople.Location;


import java.util.ArrayList;
import java.util.List;


public class IconTextListAdapter extends BaseAdapter {
    private static final String TAG = IconTextListAdapter.class.getSimpleName();

    private List<Location> mItemList;
    private Context context;



    public IconTextListAdapter(Context context, List<Location> itemList) {

        mItemList = new ArrayList<>();
        mItemList.addAll(itemList);
        this.context = context;

    }


    public void setList(List<Location> itemList) {
        mItemList.clear();

       mItemList.addAll(itemList);

       notifyDataSetChanged();
    }

    public void addToList(Location newElement) {
        mItemList.add(newElement);
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int index, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mainView = inflater.inflate(R.layout.control_searchmenu_item, null, true);

        Location element = mItemList.get(index);
        TextView locationLabel = mainView.findViewById(R.id.ctrl_mainmenu_textitem) ;
        locationLabel.setText(element.getName());

        return mainView;
    }


    public void clearItems(){
        mItemList.clear();
        notifyDataSetChanged();
    }

}