package com.lja.whereiwasmapper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import static com.google.android.gms.common.util.ArrayUtils.appendToArray;
import static com.google.android.material.snackbar.Snackbar.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnRequestPermissionsResultCallback {

    private static final int REQUEST_GPS_FINE = 1;
    private static final int REQUEST_GPS_COARSE = 2;
    private static final int REQUEST_STORAGE_RW = 4;
    private static final int REQUEST_STORAGE_WR = 8;
    private static final int REQUEST_INTERNET = 16;

    private GoogleMap mMap;

    public HashMap<String, Integer> mHMappPermission = new HashMap<>();
    public Integer  mIntPermissions = 0;
    public LocationManager locationManager = null;
    Boolean PermissionsAndLocmanOK = false;
    private Boolean iMapShowed = false;
    public Boolean iGPSFix = false;
    private SatelliteListener mGnssListener;

    private ArrayList<oWhereObject> maJavaObjects = null;

    private boolean bFineOK       = false;
    private boolean bCoarseOK     = false;
    private boolean bInternetOK   = false;
    private boolean bRWStorageOK  = false;
    private boolean bWRStorageOK  = false;

    private LatLng lastGpsPoint;

    public View mMainView = null;

    public void cleanupObjectArray()
    {
        int  howManyDaysToKeepData = 21;
        Long tsNow = System.currentTimeMillis();
        Long tsOldest = tsNow - (howManyDaysToKeepData*24*60*60)*1000;

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

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {


        int checkF  = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int checkC  = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        int checkWR = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int checkRW = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        int checkI  = checkSelfPermission(Manifest.permission.INTERNET);

        bFineOK       |= (checkF  == PackageManager.PERMISSION_GRANTED);
        bCoarseOK     |= (checkC  == PackageManager.PERMISSION_GRANTED);
        bInternetOK   |= (checkI  == PackageManager.PERMISSION_GRANTED);
        bRWStorageOK  |= (checkRW == PackageManager.PERMISSION_GRANTED);
        bWRStorageOK  |= (checkWR == PackageManager.PERMISSION_GRANTED);

        PermissionsAndLocmanOK  = (bFineOK || bCoarseOK);
        PermissionsAndLocmanOK &= (bInternetOK && bRWStorageOK && bWRStorageOK);

        if (PermissionsAndLocmanOK)
        {

            Location tmpLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (tmpLoc != null)
                mGnssListener.onLocationChanged(tmpLoc);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    60000, 2, mGnssListener);
        }
    }

    public boolean checkAndReqPermissions()
    {
        boolean rv = true;

        String[] perms= {};

        for(Map.Entry<String, Integer> perm : mHMappPermission.entrySet())
        {
            String permName = perm.getKey();
            Integer permInt = perm.getValue();
            perms = appendToArray(perms, permName);
            mIntPermissions |= permInt;
        }

        getPermission(perms, mIntPermissions);

    return  rv;
    }


    public void  getPermission(String[] permissionIDs, int permissionIntID)
    {
        ActivityCompat.requestPermissions(MapsActivity.this,
                                        permissionIDs,
                                        permissionIntID);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGnssListener = new SatelliteListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mHMappPermission.put(Manifest.permission.ACCESS_FINE_LOCATION,   REQUEST_GPS_FINE);
        mHMappPermission.put(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_GPS_COARSE);
        mHMappPermission.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_STORAGE_WR);
        mHMappPermission.put(Manifest.permission.READ_EXTERNAL_STORAGE,  REQUEST_STORAGE_RW);
        mHMappPermission.put(Manifest.permission.INTERNET,               REQUEST_INTERNET);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mMainView = mapFragment.getView();

        mapFragment.getMapAsync(this);

        maJavaObjects = new ArrayList<oWhereObject>();

        checkAndReqPermissions();

        mGnssListener.setUiComponent(this);

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
