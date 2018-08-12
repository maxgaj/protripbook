package be.maxgaj.protripbook;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//https://developer.android.com/training/location/display-address
public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver receiver;

    private static final String TAG = FetchAddressIntentService.class.getSimpleName();
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int MY_PERMISSION_ACCESS_LOCATION = 2;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";


    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;
        String errorMessage="";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        this.receiver = intent.getParcelableExtra(RECEIVER);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioE){
            errorMessage = getString(R.string.address_error_service_not_available);
            Log.e(TAG, "onHandleIntent: "+errorMessage, ioE);
        } catch (IllegalArgumentException iaE){
            errorMessage = getString(R.string.address_error_invalid_lat_long);
            Log.e(TAG, "onHandleIntent: "+errorMessage+". "+
                    "Latitude = "+location.getLatitude()+
                    ", Longitude = "+location.getLongitude(), iaE);
        }

        if (addresses == null || addresses.size()==0){
            if (errorMessage.isEmpty()){
                errorMessage = getString(R.string.address_error_no_address_found);
                Log.e(TAG, "onHandleIntent: "+errorMessage);
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage);
        }
        else{
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();
            for (int i=0; i<=address.getMaxAddressLineIndex(); i++){
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "onHandleIntent: "+getString(R.string.address_info_address_found));
            deliverResultToReceiver(SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
