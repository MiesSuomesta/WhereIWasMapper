package com.lja.whereiwasmapper;

import java.io.Serializable;

public class oStampedLocation implements Serializable {


    public    long                mTimeSinceEPOCH = 0;
    public    double              mLongitude = 0;
    public    double              mLatitude = 0;

    public oStampedLocation(long mTimeSinceEPOCH, double mLongitude, double mLatitude) {
        this.mTimeSinceEPOCH = mTimeSinceEPOCH;
        this.mLongitude = mLongitude;
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double pLongitude) {
        this.mLongitude = pLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double pLatitude) {
        this.mLatitude = pLatitude;
    }

    public long getTimeSinceEPOCH() {
        return mTimeSinceEPOCH;
    }

    public void setTimeSinceEPOCH(long mTimeSinceEPOCH) {
        this.mTimeSinceEPOCH = mTimeSinceEPOCH;
    }
}
