package be.maxgaj.protripbook.data;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static void insertFakeData(SQLiteDatabase db){
        insertFakeTripData(db);
    }

    public static void insertFakeTripData(SQLiteDatabase db){
        if (db == null){
            return;
        }

        List<ContentValues> list = new ArrayList<>();

        ContentValues cv = new ContentValues();
        cv.put(ProtripBookContract.TripEntry.COLUMN_CAR, 1);
        cv.put(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION, "Liège, Belgium");
        cv.put(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION, "Brussels, Belgium");
        cv.put(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP, 1);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DISTANCE, 100.0);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DATE, 0);
        list.add(cv);

        cv = new ContentValues();
        cv.put(ProtripBookContract.TripEntry.COLUMN_CAR, 1);
        cv.put(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION, "Liège, Belgium");
        cv.put(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION, "Brussels, Belgium");
        cv.put(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP, 1);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DISTANCE, 101.0);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DATE, 0);
        list.add(cv);

        cv = new ContentValues();
        cv.put(ProtripBookContract.TripEntry.COLUMN_CAR, 1);
        cv.put(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION, "Liège, Belgium");
        cv.put(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION, "Brussels, Belgium");
        cv.put(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP, 1);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DISTANCE, 102.0);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DATE, 0);
        list.add(cv);

        try {
            db.beginTransaction();
            db.delete(ProtripBookContract.TripEntry.TABLE_NAME, null, null);
            for (ContentValues c:list){
                db.insert(ProtripBookContract.TripEntry.TABLE_NAME, null, c);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e){
            Log.d("TEST UTIL", "insertFakeTripData: " + e.getMessage());
        }
        finally {
            db.endTransaction();
        }




    }

}
