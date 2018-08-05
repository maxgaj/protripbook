package be.maxgaj.protripbook.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProtripBookDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "protripbook.db";
    private static final int DATABASE_VERSION = 1;

    public ProtripBookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CAR_TABLE = "CREATE TABLE " +
                ProtripBookContract.CarEntry.TABLE_NAME + " (" +
                ProtripBookContract.CarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProtripBookContract.CarEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProtripBookContract.CarEntry.COLUMN_BRAND + " TEXT, " +
                ProtripBookContract.CarEntry.COLUMN_PLATE + " TEXT, " +
                ProtripBookContract.CarEntry.COLUMN_INITIAL_ODOMETER + " INTEGER" +
                ");";
        db.execSQL(SQL_CREATE_CAR_TABLE);

        final String SQL_CREATE_TRIP_TABLE = "CREATE TABLE " +
                ProtripBookContract.TripEntry.TABLE_NAME + " (" +
                ProtripBookContract.TripEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProtripBookContract.TripEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION + " TEXT NOT NULL, " +
                ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION + " TEXT NOT NULL, " +
                ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP + " INTEGER NOT NULL, " +
                ProtripBookContract.TripEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                ProtripBookContract.TripEntry.COLUMN_DATE + " DATE NOT NULL " +
                ");";
        db.execSQL(SQL_CREATE_TRIP_TABLE);

        final String SQL_CREATE_ODOMETER_TABLE = "CREATE TABLE " +
                ProtripBookContract.OdometerEntry.TABLE_NAME + " (" +
                ProtripBookContract.OdometerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProtripBookContract.OdometerEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                ProtripBookContract.OdometerEntry.COLUMN_READING + " REAL NOT NULL, " +
                ProtripBookContract.OdometerEntry.COLUMN_DATE + "DATE NOT NULL " +
                ");";
        db.execSQL(SQL_CREATE_ODOMETER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProtripBookContract.CarEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProtripBookContract.TripEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProtripBookContract.OdometerEntry.TABLE_NAME);
        onCreate(db);
    }
}
