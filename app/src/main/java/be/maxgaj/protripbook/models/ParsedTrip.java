package be.maxgaj.protripbook.models;

import java.util.ArrayList;
import java.util.List;

public class ParsedTrip {
    private float tripDistance;
    private List<Trip> tripList = new ArrayList<>();

    public ParsedTrip(float tripDistance, List<Trip> tripList){
        this.tripDistance = tripDistance;
        this.tripList = tripList;
    }

    public float getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(float tripDistance) {
        this.tripDistance = tripDistance;
    }

    public List<Trip> getTripList() {
        return tripList;
    }

    public void setTripList(List<Trip> tripList) {
        this.tripList = tripList;
    }
}
