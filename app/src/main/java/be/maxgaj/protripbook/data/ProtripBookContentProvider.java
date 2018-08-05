package be.maxgaj.protripbook.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ProtripBookContentProvider extends ContentProvider {

    public static final int CARS = 100;
    public static final int CAR_WITH_ID = 101;
    public static final int TRIPS = 200;
    public static final int TRIP_WITH_ID = 201;
    public static final int ODOMETERS = 300;
    public static final int ODOMETER_WITH_ID = 301;

    private ProtripBookDbHelper dbHelper;
    private static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ProtripBookContract.AUTHORITY, ProtripBookContract.PATH_CARS, CARS);
        uriMatcher.addURI(ProtripBookContract.AUTHORITY, ProtripBookContract.PATH_CARS+"/#", CAR_WITH_ID);
        uriMatcher.addURI(ProtripBookContract.AUTHORITY, ProtripBookContract.PATH_TRIPS, TRIPS);
        uriMatcher.addURI(ProtripBookContract.AUTHORITY, ProtripBookContract.PATH_TRIPS+"/#", TRIP_WITH_ID);
        uriMatcher.addURI(ProtripBookContract.AUTHORITY, ProtripBookContract.PATH_ODOMETERS, ODOMETERS);
        uriMatcher.addURI(ProtripBookContract.AUTHORITY, ProtripBookContract.PATH_ODOMETERS+"/#", ODOMETER_WITH_ID);
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        this.dbHelper = new ProtripBookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        String id;
        String mSelection;
        String[] mSelectionArgs;
        switch(match){
            case CARS:
                retCursor = db.query(ProtripBookContract.CarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CAR_WITH_ID:
                id = uri.getPathSegments().get(1);
                mSelection = "_id=?";
                mSelectionArgs = new String[]{id};
                retCursor = db.query(ProtripBookContract.CarEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRIPS:
                retCursor = db.query(ProtripBookContract.TripEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRIP_WITH_ID:
                id = uri.getPathSegments().get(1);
                mSelection = "_id=?";
                mSelectionArgs = new String[]{id};
                retCursor = db.query(ProtripBookContract.TripEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ODOMETERS:
                retCursor = db.query(ProtripBookContract.OdometerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ODOMETER_WITH_ID:
                id = uri.getPathSegments().get(1);
                mSelection = "_id=?";
                mSelectionArgs = new String[]{id};
                retCursor = db.query(ProtripBookContract.OdometerEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                 throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case CARS:
                return "vnd.android.cursor.dir" + "/" + ProtripBookContract.AUTHORITY + "/" + ProtripBookContract.PATH_CARS;
            case CAR_WITH_ID:
                return "vnd.android.cursor.item" + "/" + ProtripBookContract.AUTHORITY + "/" + ProtripBookContract.PATH_CARS;
            case TRIPS:
                return "vnd.android.cursor.dir" + "/" + ProtripBookContract.AUTHORITY + "/" + ProtripBookContract.PATH_TRIPS;
            case TRIP_WITH_ID:
                return "vnd.android.cursor.item" + "/" + ProtripBookContract.AUTHORITY + "/" + ProtripBookContract.PATH_TRIPS;
            case ODOMETERS:
                return "vnd.android.cursor.dir" + "/" + ProtripBookContract.AUTHORITY + "/" + ProtripBookContract.PATH_ODOMETERS;
            case ODOMETER_WITH_ID:
                return "vnd.android.cursor.item" + "/" + ProtripBookContract.AUTHORITY + "/" + ProtripBookContract.PATH_ODOMETERS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        long id;
        switch(match){
            case CARS:
                id = db.insert(ProtripBookContract.CarEntry.TABLE_NAME, null, values);
                if (id>0)
                    returnUri = ContentUris.withAppendedId(ProtripBookContract.CarEntry.CONTENT_URI, id);
                else
                    throw new SQLException("Failed to insert row into"+uri);
                break;
            case TRIPS:
                id = db.insert(ProtripBookContract.TripEntry.TABLE_NAME, null, values);
                if (id>0)
                    returnUri = ContentUris.withAppendedId(ProtripBookContract.TripEntry.CONTENT_URI, id);
                else
                    throw new SQLException("Failed to insert row into"+uri);
                break;
            case ODOMETERS:
                id = db.insert(ProtripBookContract.OdometerEntry.TABLE_NAME, null, values);
                if (id>0)
                    returnUri = ContentUris.withAppendedId(ProtripBookContract.OdometerEntry.CONTENT_URI, id);
                else
                    throw new SQLException("Failed to insert row into"+uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksDeleted;
        String id;
        switch(match){
            case CAR_WITH_ID:
                id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(ProtripBookContract.CarEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            case TRIPS:
                tasksDeleted = db.delete(ProtripBookContract.TripEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRIP_WITH_ID:
                id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(ProtripBookContract.TripEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            case ODOMETERS:
                tasksDeleted = db.delete(ProtripBookContract.OdometerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ODOMETER_WITH_ID:
                id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(ProtripBookContract.OdometerEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        if (tasksDeleted !=0)
            getContext().getContentResolver().notifyChange(uri, null);
        return tasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksUpdated;
        String id;
        switch(match){
            case CAR_WITH_ID:
                id = uri.getPathSegments().get(1);
                tasksUpdated = db.update(ProtripBookContract.CarEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            case TRIP_WITH_ID:
                id = uri.getPathSegments().get(1);
                tasksUpdated = db.update(ProtripBookContract.TripEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            case ODOMETER_WITH_ID:
                id = uri.getPathSegments().get(1);
                tasksUpdated = db.update(ProtripBookContract.OdometerEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        if (tasksUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksUpdated;
    }
}
