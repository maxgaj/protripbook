package be.maxgaj.protripbook.models;

import java.util.Date;

public class Odometer {

    private int id;
    private int idCar;
    private Long reading;
    private Date date;

    public Odometer() {}

    public Odometer(int id, int idCar, Long reading, Date date){
        this.id = id;
        this.idCar = idCar;
        this.reading = reading;
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

    public Long getReading() {
        return reading;
    }

    public void setReading(Long reading) {
        this.reading = reading;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
