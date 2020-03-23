package com.lja.whereiwasmapper;


import android.location.LocationManager;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


public class oJsonToObject {

    public oJsonToObject() {}

    public oJSONJavaObject jsonObjectToJavaObject(MapsActivity    mapsActivity,
                                                  GoogleMap       googleMap,
                                                  JSONObject      featureObj,
                                                  Boolean         PermissionsOK,
                                                  LocationManager pLocman)
    {
        oJSONJavaObject ret = null;

        try {

            long timeSinceEPOCH         = featureObj.getLong("timeSinceEPOCH");
            double Longitude            = featureObj.getDouble("longitude");
            double Latitude             = featureObj.getDouble("latitude");

            ret = new oJSONJavaObject(
                    mapsActivity,
                    googleMap,
                    timeSinceEPOCH,
                    Longitude,
                    Latitude,
                    PermissionsOK,
                    pLocman);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return ret;
    }

    // "https://prod-earthquake.cr.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson"
    public JSONObject jsonObjectFromUrl(String pUrl)
    {
        JSONObject retobj = null;
        try {

            URL dlurl = new URL(pUrl);
            URLConnection dlurlconn = dlurl.openConnection();
            dlurlconn.connect();

            InputStream iStream = dlurlconn.getInputStream();
            BufferedReader iStreamReader = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = iStreamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            retobj = new JSONObject(responseStrBuilder.toString());

            // Clean up
            dlurlconn = null;
            dlurl = null;
            iStream = null;
            iStreamReader = null;
            responseStrBuilder = null;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retobj;
    }

}
