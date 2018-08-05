package be.maxgaj.protripbook.data;

import android.provider.BaseColumns;

public class ProtripBookContract {

    public static final class CarEntry implements BaseColumns {
        public static final String TABLE_NAME = "car";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BRAND = "brand";
        public static final String COLUMN_PLATE = "plate";
        public static final String COLUMN_INITIAL_ODOMETER = "initOdometer";
    }

    public static final class TripEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip";
        public static final String COLUMN_CAR = "idCar";
        public static final String COLUMN_STARTING_LOCATION = "startLocation";
        public static final String COLUMN_DESTINATION_LOCATION = "destLocation";
        public static final String COLUMN_ROUND_TRIP = "isRoundTrip";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_DATE = "tripDate";
    }

    public static final class OdometerEntry implements BaseColumns {
        public static final String TABLE_NAME = "odometer";
        public static final String COLUMN_CAR = "idCar";
        public static final String COLUMN_READING = "reading";
        public static final String COLUMN_DATE = "date";
    }






}
