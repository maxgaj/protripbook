package be.maxgaj.protripbook.utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


public class DistanceUtils {
    private static String DISTANCE_BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static String PARAM_UNITS = "units";
    private static String PARAM_ORIGIN = "origins";
    private static String PARAM_DESTINATION = "destinations";
    private static String JSON_STATUS = "status";
    private static String JSON_ROWS = "rows";
    private static String JSON_VALUE = "value";
    private static String JSON_ELEMENT = "elements";
    private static String JSON_DISTANCE = "distance";

    private static String TAG = DistanceUtils.class.getSimpleName();


    public static URL buildURL(String unit, String origin, String destination){
        Uri builtUri = Uri.parse(DISTANCE_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_UNITS, unit)
                .appendQueryParameter(PARAM_ORIGIN, origin)
                .appendQueryParameter(PARAM_DESTINATION, destination)
                .build();

        URL url = null;
        try{
            url =new URL(builtUri.toString());
        } catch(MalformedURLException e){
            Log.e(TAG, "calculateDistance: Invalid uri", e);
        }

        if (url!=null)
            return url;
        else
            return null;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput)
                return scanner.next();
            else
                return null;
        } finally {
            connection.disconnect();
        }
    }

    public static String getStatusFromJSON(String dataString){
        try {
            JSONObject data = new JSONObject(dataString);
            return data.getString(JSON_STATUS);
        } catch (JSONException e){
            Log.e(TAG, "getStatusFromJSON: ", e);
            return null;
        }
    }

    public static int getDistanceFromJSON(String dataString){
        try {
            JSONObject data = new JSONObject(dataString);
            JSONArray rows = data.getJSONArray(JSON_ROWS);
            JSONObject first_row = rows.getJSONObject(0);
            JSONArray elements = first_row.getJSONArray(JSON_ELEMENT);
            JSONObject first_element = elements.getJSONObject(0);
            JSONObject distance = first_element.getJSONObject(JSON_DISTANCE);
            return distance.getInt(JSON_VALUE);
        } catch (JSONException e){
            Log.e(TAG, "getDistanceFromJSON: ", e);
            return 0;
        }
    }

}
