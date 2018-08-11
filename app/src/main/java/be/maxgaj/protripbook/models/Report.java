package be.maxgaj.protripbook.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Report implements Parcelable {
    private String firstDate;
    private String lastDate;
    private float tripDistance;
    private float odometerDistance;
    private List<Trip> tripList = new ArrayList<>();
    private String unit;

    public Report(String firstDate, String lastDate, float tripDistance, float odometerDistance, List<Trip> tripList, String unit){
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.tripDistance = tripDistance;
        this.odometerDistance = odometerDistance;
        this.tripList = tripList;
        this.unit = unit;
    }

    private Report(Parcel in){
        this.firstDate = in.readString();
        this.lastDate = in.readString();
        this.tripDistance = in.readFloat();
        this.odometerDistance = in.readFloat();
        in.readTypedList(this.tripList, Trip.CREATOR);
        this.unit = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.firstDate);
        dest.writeString(this.lastDate);
        dest.writeFloat(tripDistance);
        dest.writeFloat(odometerDistance);
        dest.writeTypedList(tripList);
        dest.writeString(unit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public float getRatio(){
        if (odometerDistance > 0){
            return (tripDistance/odometerDistance)*100;
        }
        else {
            return 0;
        }
    }

    public String getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(String firstDate) {
        this.firstDate = firstDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public float getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(float tripDistance) {
        this.tripDistance = tripDistance;
    }

    public float getOdometerDistance() {
        return odometerDistance;
    }

    public void setOdometerDistance(float odometerDistance) {
        this.odometerDistance = odometerDistance;
    }

    public List<Trip> getTripList() {
        return tripList;
    }

    public void setTripList(List<Trip> tripList) {
        this.tripList = tripList;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public static final Parcelable.Creator<Report> CREATOR
            = new Parcelable.Creator<Report>() {

        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };
}
