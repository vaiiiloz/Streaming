package entity;

import java.util.List;

public class PeopleBox {

    private String deviceID;
    private long date;
    private List bBoxes;

    public PeopleBox(String deviceID, long date, List<Entity> bBox){
        this.deviceID = deviceID;
        this.date = date;
        this.bBoxes = bBox;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public List<Entity> getbBoxes() {
        return bBoxes;
    }


    public void setbBoxes(List<Entity> bBoxes) {
        this.bBoxes = bBoxes;
    }
}
