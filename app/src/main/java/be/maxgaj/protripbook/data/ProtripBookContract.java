package be.maxgaj.protripbook.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class ProtripBookContract {

    public static final String AUTHORITY = "be.maxgaj.protripbook";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);

    public static final String PATH_CARS = "cars";
    public static final String PATH_TRIPS = "trips";
    public static final String PATH_ODOMETERS = "odometers";

    public static final class CarEntry implements BaseColumns {
        public static final String TABLE_NAME = "car";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BRAND = "brand";
        public static final String COLUMN_PLATE = "plate";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CARS).build();

    }

    public static final class TripEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip";
        public static final String COLUMN_CAR = "idCar";
        public static final String COLUMN_STARTING_LOCATION = "startLocation";
        public static final String COLUMN_DESTINATION_LOCATION = "destLocation";
        public static final String COLUMN_ROUND_TRIP = "isRoundTrip";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_DATE = "tripDate";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIPS).build();
    }

    public static final class OdometerEntry implements BaseColumns {
        public static final String TABLE_NAME = "odometer";
        public static final String COLUMN_CAR = "idCar";
        public static final String COLUMN_READING = "reading";
        public static final String COLUMN_DATE = "date";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ODOMETERS).build();

    }






}
