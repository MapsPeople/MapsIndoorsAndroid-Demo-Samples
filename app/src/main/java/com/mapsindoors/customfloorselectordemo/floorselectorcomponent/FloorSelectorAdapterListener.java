package com.mapsindoors.customfloorselectordemo.floorselectorcomponent;

import android.support.annotation.NonNull;

import com.mapsindoors.mapssdk.Floor;

interface FloorSelectorAdapterListener {
    void onFloorSelectionChanged(@NonNull Floor newFloor);
}
