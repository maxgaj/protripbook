package be.maxgaj.protripbook.models;

import java.util.Date;

public class Trip {

    private int id;
    private int idCar;
    private String startingLocation;
    private String destinationLocation;
    private boolean roundTrip;
    private Long distance;
    private Date date;

    public Trip(){}

    public Trip(int id, int idCar, String startingLocation, String destinationLocation, boolean roundTrip, Long distance, Date date){
        this.id = id;
        this.idCar = idCar;
        this.startingLocation = startingLocation;
        this.destinationLocation = destinationLocation;
        this.roundTrip = roundTrip;
        this.distance = distance;
        this.date = date;
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

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
