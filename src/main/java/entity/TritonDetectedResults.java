package entity;

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

    public List getListBBoxes() {
        return listBBoxes;
    }

    public void setListBBoxes(List listBBoxes) {
        this.listBBoxes = listBBoxes;
    }
}
