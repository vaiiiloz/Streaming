package entity;

import java.util.List;

public class PeopleBox {

    private String deviceID;
    private int hour;
    private int time;
    private List bBoxes;

    public <T> PeopleBox(String deviceID, int hour, int time, List<T> bBoxes) {
        this.deviceID = deviceID;
        this.hour = hour;
        this.time = time;
        this.bBoxes = bBoxes;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public <T> List<T> getbBoxes() {
        return (List<T>) bBoxes;
    }

    public <T> void setbBoxes(List<T> bBoxes) {
        this.bBoxes = bBoxes;
    }
}
