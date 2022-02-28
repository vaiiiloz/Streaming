package entity;

public class TritonInputData {
    private String deviceId;
    private int[] data;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public TritonInputData(String deviceId, int[] data) {
        this.deviceId = deviceId;
        this.data = data;
    }
}
