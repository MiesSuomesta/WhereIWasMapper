package com.lja.whereiwasmapper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public LocationManager locationManager = null;
    Boolean PermissionsAndLocmanOK = false;
    private Boolean iMapShowed = false;
    public Boolean iGPSFix = false;
    private SatelliteListener mGnssListener;

    private ArrayList<oWhereObject> maJavaObjects = null;

    private LatLng lastGpsPoint;

    public void cleanupObjectArray()
    {
        int  howManyDaysToKeepData = 21;
        Long tsNow = System.currentTimeMillis()/1000;
        Long tsOldest = tsNow - (howManyDaysToKeepData*24*60*60);

        for (oWhereObject job: maJavaObjects)
        {
            long oTS = job.getTimeSinceEPOCH();
            if (oTS < tsOldest)
            {
                maJavaObjects.remove(job);
            }
        }
    }

    public void refreshMapOfObjects()
    {

        cleanupObjectArray();

        if (mMap == null)
            return;

        mMap.clear();

        for (oWhereObject job: maJavaObjects)
        {
            Marker dp = job.getMarkerForGmaps();
//            mMap.addMarker(dp.get);
        }
    }

    public boolean doesAppHaveNeededPermissions(String permissionID)
    {
        if (ContextCompat.checkSelfPermission(this,
                permissionID)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permissionID)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGnssListener = new SatelliteListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        maJavaObjects = new ArrayList<oWhereObject>();

        int checkF = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int checkC = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        boolean bFineOK   = (checkF == PackageManager.PERMISSION_GRANTED);
        boolean bCoarseOK = (checkC == PackageManager.PERMISSION_GRANTED);

        PermissionsAndLocmanOK  = (locationManager != null);
        PermissionsAndLocmanOK &= (bFineOK || bCoarseOK);

        mGnssListener.setUiComponent(this);

        if (PermissionsAndLocmanOK)
        {

            Location tmpLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (tmpLoc != null)
                mGnssListener.onLocationChanged(tmpLoc);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                            600, 5, mGnssListener);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        LatLng place = mGnssListener.getLastPoint();
        reloadData();
        this.refreshMapOfObjects();

        if (this.iGPSFix && place != null) {
            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        }
    }

    public void addRandomPositions() {

        int count = 10;
        int longitude,latitude;
        oWhereObject jobj;
        long ts = 0;
        Random r = new Random();

        while (count-- > 0)
        {

            ts = mGnssListener.getTimestamp();

            longitude = r.nextInt(360) - 180;
            latitude  = r.nextInt(180) - 90;

            jobj = new oWhereObject(
                    this,
                    this.mMap,
                    ts,
                    longitude,
                    latitude,
                    PermissionsAndLocmanOK,
                    locationManager
            );

            maJavaObjects.add(jobj);
        }

    }

    public oWhereObject newLocationObjForPositions(long ts, double longitude, double latitude) {
        oWhereObject jobj;

        jobj = new oWhereObject(
                this,
                this.mMap,
                ts,
                longitude,
                latitude,
                PermissionsAndLocmanOK,
                locationManager
        );

        return jobj;
    }

    public ArrayList<oWhereObject> readPlacesData(String pFilename) {
        ArrayList<oStampedLocation> tmpST = null;
        ArrayList<oWhereObject>  tmpJO = null;

        try {
            FileInputStream fileIn = new FileInputStream(pFilename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tmpST = (ArrayList<oStampedLocation>) in.readObject();
            in.close();
            fileIn.close();
            System.out.printf("Serialized data is loaded from " + pFilename);
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }

        if (tmpST != null) {
            tmpJO = new ArrayList<oWhereObject>();
            for (oStampedLocation job : tmpST) {
                tmpJO.add(
                        newLocationObjForPositions(
                                job.getTimeSinceEPOCH(),
                                job.getLongitude(),
                                job.getLatitude())
                );
            }
        }

        return tmpJO;
    }

    public ArrayList<oWhereObject> writePlacesData(String pFilename)  {
        ArrayList<oWhereObject> tmp = maJavaObjects;

        ArrayList<oStampedLocation> tmpST = null;
        ArrayList<oWhereObject>  tmpJO = maJavaObjects;

        if (maJavaObjects != null) {
            tmpST = new ArrayList<oStampedLocation>();

            for (oWhereObject job : maJavaObjects) {
                tmpST.add(job.getmStampedLocation());
            }
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(pFilename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(tmpST);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + pFilename);
        } catch (IOException i) {
            i.printStackTrace();
        }

        return tmp;
    }

    public ArrayList<oWhereObject> reloadData()
    {
        String theFilenameStr = getFilesDir() + "/whereiwasmapper.bin";
        ArrayList<oWhereObject> oldData = null;
        ArrayList<oWhereObject> currentData = maJavaObjects;
        ArrayList<oWhereObject> newData = new ArrayList<oWhereObject>();

        oldData = readPlacesData(theFilenameStr);

        if (oldData != null) {
            for (oWhereObject job : oldData) {
                newData.add(job);
            }
        }

        if (currentData != null) {
            for (oWhereObject job : currentData) {
                newData.add(job);
            }
        }

        maJavaObjects = newData;

        this.refreshMapOfObjects();

        writePlacesData(theFilenameStr);

        return newData;
    }

    public void GPSLocation_changed() {
        LatLng place = mGnssListener.getLastPoint();

        oWhereObject jobj;
        long ts = 0;

        if (place == null)
            return;

        ts = mGnssListener.getTimestamp();

        jobj = new oWhereObject(
                this,
                this.mMap,
                ts,
                place.longitude,
                place.latitude,
                PermissionsAndLocmanOK,
                locationManager
            );

        maJavaObjects.add(jobj);

//        this.addRandomPositions();

        this.refreshMapOfObjects();

        if (mMap != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place));

    }

}
