package com.lja.whereiwasmapper;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


public class SatelliteListener implements LocationListener {

    private MapsActivity mMainUI;
    private Location     mLocation;
    private long         mLocationTimesamp;

    public long getTimestamp()
    {
        return mLocationTimesamp;
    }

    public long setTimestamp(long ts)
    {
        mLocationTimesamp = ts;
        return mLocationTimesamp;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;

        Long tsLong = System.currentTimeMillis();
        this.setTimestamp(tsLong);
        if (mMainUI != null) {
            mMainUI.iGPSFix = true;
            mMainUI.GPSLocation_changed();
        }
    }

    public LatLng getLastPoint() {
        if (mMainUI.iGPSFix)
            return new LatLng(
                            mLocation.getLatitude(),
                            mLocation.getLongitude()
                        );
        else
            return null;
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (mMainUI != null) {
            mMainUI.iGPSFix = false;
            mMainUI.GPSLocation_changed();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (mMainUI != null) {
            mMainUI.iGPSFix = true;
            mMainUI.GPSLocation_changed();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void setUiComponent(MapsActivity mapsAct) {
        mMainUI = mapsAct;
        if (mMainUI != null) {
            mMainUI.GPSLocation_changed();
        }
    }
}
