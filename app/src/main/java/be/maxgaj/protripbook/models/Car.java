package be.maxgaj.protripbook.models;

public class Car {

    private int id;
    private String name;
    private String brand;
    private String plate;
    private Odometer initOdometer;

    public Car() {}

    public Car(int id, String name, String brand, String plate, Odometer initialReading){
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.plate = plate;
        this.initOdometer = initialReading;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Odometer getInitOdometer() {
        return initOdometer;
    }

    public void setInitOdometer(Odometer initOdometer) {
        this.initOdometer = initOdometer;
    }
}
