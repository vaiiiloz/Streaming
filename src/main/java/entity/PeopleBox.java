package entity;

import java.util.Date;
import java.util.List;

public class PeopleBox {

    private String deviceID;
    private Date date;
    private List bBoxes;

    public <T> PeopleBox(String deviceID, Date date, List<T> bBoxes) {
        this.deviceID = deviceID;
        this.date = date;
        this.bBoxes = bBoxes;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public <T> List<T> getbBoxes() {
        return (List<T>) bBoxes;
    }


    public <T> void setbBoxes(List<T> bBoxes) {
        this.bBoxes = bBoxes;
    }
}
