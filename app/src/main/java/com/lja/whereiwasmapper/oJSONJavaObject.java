package com.lja.whereiwasmapper;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class oJSONJavaObject implements OnMarkerClickListener
{

    public    oStampedLocation    mStampedLocation = null;
    public    double              mDeviceLongitude = 0;
    public    double              mDeviceLatitude = 0;

    public    Marker              mMarker = null;
    public    MarkerOptions       mMarkerOptions = null;
    public    GoogleMap           mMap = null;
    public    LocationManager     mLocman = null;
    public    Boolean             mPermitted = false;
    public    DateFormat          dateFormatter = null;
    public    MapsActivity        mMyMapsActivity;


    public oJSONJavaObject(
            MapsActivity    mapsActivity,
            GoogleMap googleMap,
            long   pTimeSinceEPOCH,
            double pLongitude,
            double pLatitude,
            boolean pPermitted,
            LocationManager pLocman)
    {
        // setup variables
        mMap = googleMap;
        mStampedLocation = new oStampedLocation(pTimeSinceEPOCH, pLongitude, pLatitude);
        mMyMapsActivity = mapsActivity;

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSSXXX");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Setup intent tp alarm too near
        mLocman = pLocman;

        if (mMap != null)
            mMap.setOnMarkerClickListener(this);

    }

    public oStampedLocation getmStampedLocation() {
        return mStampedLocation;
    }

    public void setmStampedLocation(oStampedLocation mStampedLocation) {
        this.mStampedLocation = mStampedLocation;
    }

    public long getTimeSinceEPOCH() {
        return this.mStampedLocation.getTimeSinceEPOCH();
    }

    public void setTimeSinceEPOCH(long mTimeSinceEPOCH) {
        this.mStampedLocation.setTimeSinceEPOCH(mTimeSinceEPOCH);
    }


    public double distance(double lat1,
                           double lon1, double lat2,
                           double lon2)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c * r);
    }

    public double getMarkerDistanceToDevice(LatLng pLatLngDevice)
    {
        double ret = 0;
        if (pLatLngDevice != null) {
            double devLat = pLatLngDevice.latitude;
            double devLng = pLatLngDevice.longitude;

            ret = this.distance(this.mStampedLocation.getLatitude(),
                                this.mStampedLocation.getLongitude(),
                                devLat,
                                devLng);

        }

        return ret;
    }


    public GoogleMap getmMap() {
        return this.mMap;
    }

    public LocationManager getmLocman() {
        return mLocman;
    }

    public MapsActivity getmMyMapsActivity() {
        return mMyMapsActivity;
    }

    public Marker setmMarker(Marker foo) {
        mMarker = foo;
        return mMarker;
    }

    public Marker getMarkerForGmaps()
    {
        LatLng quakePoint = new LatLng( this.mStampedLocation.getLatitude(),
                                        this.mStampedLocation.getLongitude());

        setmMarker(null);

        if ( mMarkerOptions == null )
        {
            mMarkerOptions = new MarkerOptions();
        }


        Date   datequaketime = new Date( this.getTimeSinceEPOCH() );
        String formattedDate = dateFormatter.format(datequaketime);
        String snippet = formattedDate;

        mMarkerOptions.position(quakePoint);
        mMarkerOptions.snippet(snippet);

        GoogleMap myMap = getmMap();

        if (myMap == null)
            return null;

        Marker myMark = myMap.addMarker(mMarkerOptions);

        return setmMarker(myMark);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String UIStr = marker.getSnippet();
        Toast.makeText(getmMyMapsActivity(), UIStr, Toast.LENGTH_LONG).show();

        return false;
    }
}
