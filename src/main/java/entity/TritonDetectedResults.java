package entity;

import entity.BBox;

import java.util.List;

public class TritonDetectedResults {
    String deviceId;
    List listBBoxes;

    public <T> TritonDetectedResults(String deviceId, List<T> listBBoxes) {
        this.deviceId = deviceId;
        this.listBBoxes = listBBoxes;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<BBox> getListBBoxes() {
        return listBBoxes;
    }

    public void setListBBoxes(List<BBox> listBBoxes) {
        this.listBBoxes = listBBoxes;
    }
}
