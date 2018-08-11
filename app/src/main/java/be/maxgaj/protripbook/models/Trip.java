package be.maxgaj.protripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Trip implements Parcelable {

    private int id;
    private int idCar;
    private String startingLocation;
    private String destinationLocation;
    private boolean roundTrip;
    private float distance;
    private String date;

    public Trip(){}

    public Trip(int id, int idCar, String startingLocation, String destinationLocation, boolean roundTrip, float distance, String date){
        this.id = id;
        this.idCar = idCar;
        this.startingLocation = startingLocation;
        this.destinationLocation = destinationLocation;
        this.roundTrip = roundTrip;
        this.distance = distance;
        this.date = date;
    }

    private Trip(Parcel in){
        this.id = in.readInt();
        this.idCar = in.readInt();
        this.startingLocation = in.readString();
        this.destinationLocation = in.readString();
        this.roundTrip = in.readByte() != 0;
        this.distance = in.readFloat();
        this.date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.idCar);
        dest.writeString(this.startingLocation);
        dest.writeString(this.destinationLocation);
        dest.writeByte((byte) (this.roundTrip ? 1 : 0));
        dest.writeFloat(this.distance);
        dest.writeString(this.date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCar() {
        return idCar;
    }

    public void setIdCar(int idCar) {
        this.idCar = idCar;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static final Parcelable.Creator<Trip> CREATOR
            = new Parcelable.Creator<Trip>() {

        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}
