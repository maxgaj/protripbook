package be.maxgaj.protripbook;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.models.ParsedOdometer;
import be.maxgaj.protripbook.models.ParsedTrip;
import be.maxgaj.protripbook.models.Report;
import be.maxgaj.protripbook.models.Trip;
import be.maxgaj.protripbook.utils.ReportUtils;

public class ProtripBookWidgetService extends IntentService {

    public static final String TAG = ProtripBookWidgetService.class.getSimpleName();
    public static final String ACTION_REPORT = "be.maxgaj.protripbook.action.report";

    public ProtripBookWidgetService() {
        super("ProtripBookWidgetService");
    }

    public static void startActionReport(Context context){
        Intent intent = new Intent(context, ProtripBookWidgetService.class);
        intent.setAction(ACTION_REPORT);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent!=null){
            final String action = intent.getAction();
            if (ACTION_REPORT.equals(action)){
                handleActionReport();
            }
        }
    }

    private void handleActionReport(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String idCar = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
        boolean isFirstDate = sharedPreferences.getBoolean(getString(R.string.pref_report_first_odometer_key), true);
        boolean isLastDate = sharedPreferences.getBoolean(getString(R.string.pref_report_last_odometer_key), true);
        String firstPrefDate = sharedPreferences.getString(getString(R.string.pref_report_first_date_key), getString(R.string.pref_report_first_date_default));
        String lastPrefDate = sharedPreferences.getString(getString(R.string.pref_report_last_date_key), getString(R.string.pref_report_last_date_default));
        String unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km));

        if (Float.valueOf(idCar) > -1){

            Uri uri = ContentUris.withAppendedId(ProtripBookContract.CarEntry.CONTENT_URI, Long.parseLong(idCar));
            Cursor carCursor = getContentResolver().query(uri, null, null, null, null);
            String name = "";
            if (carCursor!=null && carCursor.getCount()>0){
                carCursor.moveToFirst();
                name = carCursor.getString(carCursor.getColumnIndex(ProtripBookContract.CarEntry.COLUMN_NAME));
                carCursor.close();
            }

            Cursor odometerCursor= getContentResolver().query(ProtripBookContract.OdometerEntry.CONTENT_URI,
                        null,
                        ProtripBookContract.OdometerEntry.COLUMN_CAR+"=?",
                        new String[]{idCar},
                        ProtripBookContract.OdometerEntry.COLUMN_DATE+" ASC");
            if (odometerCursor!=null && odometerCursor.getCount()>0) {
                ParsedOdometer parsedOdometer;
                parsedOdometer = ReportUtils.parseOdometerCursor(this, odometerCursor, firstPrefDate, lastPrefDate, isFirstDate, isLastDate);
                String firstDate = parsedOdometer.getFirstDate();
                String lastDate = parsedOdometer.getLastDate();
                float odometerDistance = parsedOdometer.getDistance();
                odometerCursor.close();

                Cursor tripCursor = getContentResolver().query(ProtripBookContract.TripEntry.CONTENT_URI,
                        null,
                        ProtripBookContract.TripEntry.COLUMN_CAR + "=?",
                        new String[]{idCar},
                        ProtripBookContract.TripEntry.COLUMN_DATE + " ASC");
                if (tripCursor != null) {
                    ParsedTrip parsedTrip = ReportUtils.parseTripCursor(this, tripCursor, firstDate, lastDate, isFirstDate, isLastDate);
                    float tripDistance = parsedTrip.getTripDistance();
                    List<Trip> tripList = parsedTrip.getTripList();
                    Report report = new Report(firstDate, lastDate, tripDistance, odometerDistance, tripList, unit);
                    String[] widgetDate = new String[]{name, String.format("%.1f", report.getRatio())};

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ProtripBookWidget.class));
                    ProtripBookWidget.updateReportWidgets(this, appWidgetManager, widgetDate, appWidgetIds);
                }
            }
        }
    }
}
