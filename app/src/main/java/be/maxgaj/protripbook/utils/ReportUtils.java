package be.maxgaj.protripbook.utils;

import android.content.Context;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.maxgaj.protripbook.R;
import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.models.ParsedOdometer;
import be.maxgaj.protripbook.models.ParsedTrip;
import be.maxgaj.protripbook.models.Trip;

public class ReportUtils {

    public static ParsedOdometer parseOdometerCursor(Context context, Cursor cursor, String firstPrefDate, String lastPrefDate, boolean isFirstDate, boolean isLastDate) throws RuntimeException {
        // Parse prefDate to Date object
        Date fPDate;
        Date lPDate;
        try {
            fPDate = new SimpleDateFormat(context.getString(R.string.date_pattern)).parse(firstPrefDate);
            lPDate = new SimpleDateFormat(context.getString(R.string.date_pattern)).parse(lastPrefDate);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(context.getString(R.string.report_util_error_pref_date));
        }
        // Check cursor
        if (cursor==null || cursor.getCount()<1)
            throw new RuntimeException(context.getString(R.string.report_util_error_odometer_cursor));
        // initiate loop variable
        String firstDate=null;
        String lastDate=null;
        float firstDistance=-1;
        float lastDistance=0;
        // loop over cursor
        while (cursor.moveToNext()){
            String dateString = cursor.getString(cursor.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_DATE));
            Date odometerDate;
            try {
                odometerDate = new SimpleDateFormat(context.getString(R.string.date_pattern)).parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new RuntimeException(context.getString(R.string.report_util_error_odometer_date));
            }
            if ((isFirstDate && isLastDate) ||
                    (!isFirstDate && isLastDate && (fPDate.before(odometerDate) || fPDate.equals(odometerDate))) ||
                    (isFirstDate && !isLastDate && (lPDate.after(odometerDate) || lPDate.equals(odometerDate))) ||
                    (!isFirstDate && !isLastDate && (fPDate.before(odometerDate) || fPDate.equals(odometerDate)) && (lPDate.after(odometerDate) || lPDate.equals(odometerDate)))){
                if (firstDate==null)
                    firstDate=dateString;
                lastDate=dateString;
                if (firstDistance==-1)
                    firstDistance=cursor.getFloat(cursor.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_READING));
                lastDistance=cursor.getFloat(cursor.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_READING));
            }
        }
        // Check return value
        if (firstDate==null || lastDate==null)
            throw new RuntimeException(context.getString(R.string.report_util_error_odometer));
        float odometerDistance=0;
        if (lastDistance >= firstDistance)
            odometerDistance=lastDistance-firstDistance;
        // return
        return new ParsedOdometer(firstDate, lastDate, odometerDistance);
    }

    public static ParsedTrip parseTripCursor(Context context, Cursor cursor, String firstDate, String lastDate, boolean isFirstDate, boolean isLastDate) throws RuntimeException {
        // parse date String to Date object
        Date tripFirstDate;
        Date tripLastDate;
        try {
            tripFirstDate = new SimpleDateFormat(context.getString(R.string.date_pattern)).parse(firstDate);
            tripLastDate = new SimpleDateFormat(context.getString(R.string.date_pattern)).parse(lastDate);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(context.getString(R.string.report_util_error_limit_date));
        }
        // Check cursor
        if (cursor==null)
            throw new RuntimeException(context.getString(R.string.report_util_error_trip_cursor));
        // initiate loop variable
        List<Trip> tripList = new ArrayList<>();
        float tripDistance = 0;
        // loop over cursor
        while (cursor.moveToNext()){
            String tripDateString = cursor.getString(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DATE));
            Date tripDate;
            try {
                tripDate = new SimpleDateFormat(context.getString(R.string.date_pattern)).parse(tripDateString);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new RuntimeException(context.getString(R.string.report_util_error_trip_date));
            }

            if ((isFirstDate && isLastDate) ||
                    (!isFirstDate && isLastDate && (tripFirstDate.before(tripDate) || tripFirstDate.equals(tripDate))) ||
                    (isFirstDate && !isLastDate && (tripLastDate.after(tripDate) || tripLastDate.equals(tripDate))) ||
                    (!isFirstDate && !isLastDate && (tripFirstDate.before(tripDate) || tripFirstDate.equals(tripDate)) && (tripLastDate.after(tripDate) || tripLastDate.equals(tripDate)))){
                tripDistance += cursor.getFloat(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE));
                tripList.add(new Trip(
                        cursor.getInt(cursor.getColumnIndex(ProtripBookContract.TripEntry._ID)),
                        cursor.getInt(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_CAR)),
                        cursor.getString(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION)),
                        cursor.getString(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION)),
                        cursor.getInt(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP))==1,
                        cursor.getFloat(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE)),
                        cursor.getString(cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DATE))
                ));
            }
        }
        return new ParsedTrip(tripDistance, tripList);
    }
}
