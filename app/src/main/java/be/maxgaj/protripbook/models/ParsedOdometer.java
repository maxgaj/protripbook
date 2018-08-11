package be.maxgaj.protripbook.models;

public class ParsedOdometer {
    private String firstDate;
    private String lastDate;
    private float distance;

    public ParsedOdometer(String firstDate, String lastDate, float distance){
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.distance = distance;
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

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
