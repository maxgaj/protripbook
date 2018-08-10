package be.maxgaj.protripbook.models;

import java.util.ArrayList;
import java.util.List;

public class Report {
    private String firstDate;
    private String lastDate;
    private float tripDistance;
    private float odometerDistance;
    private List<Trip> tripList = new ArrayList<>();

    public Report(String firstDate, String lastDate, float tripDistance, float odometerDistance, List<Trip> tripList){
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.tripDistance = tripDistance;
        this.odometerDistance = odometerDistance;
        this.tripList = tripList;
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
}
