package Thread;

import Mongo.MongoHandler;

import java.util.concurrent.ExecutorService;

public class AiService {
    private RtspStreamThread rtspStreamThread;
    private RtspCaptureThread rtspCaptureThread;
    private String rtsp;
    private int preview_width;
    private int preview_height;
    private int frameRate;
    private Boolean isStreaming;
    private String deviceId;
    private Integer frameBufferMaxSize;

    /**
     * Contruct AIService with args of Streaming and Capture Thread
     * @param rtsp
     * @param preview_width
     * @param preview_height
     * @param frameRate
     * @param deviceId
     * @param isStreaming
     * @param frameBufferMaxSize
     * @param UIBufferSize
     * @param mongoHandler
     */
    public AiService(String rtsp, int preview_width, int preview_height, int frameRate, String deviceId, Boolean isStreaming, Integer frameBufferMaxSize, Integer UIBufferSize, MongoHandler mongoHandler) {
        this.rtsp = rtsp;
        this.preview_width = preview_width;
        this.preview_height = preview_height;
        this.frameRate = frameRate;
        this.deviceId = deviceId;
        rtspStreamThread = new RtspStreamThread(isStreaming, deviceId, preview_width, preview_height, UIBufferSize, mongoHandler);
        rtspCaptureThread = new RtspCaptureThread(rtspStreamThread, rtsp, preview_width, preview_height, frameRate, deviceId);

    }

    /**
     * Start both Capture and Stream thread
     * @param executorService
     */
    public void startAll(ExecutorService executorService) {
        executorService.execute(rtspStreamThread);
        executorService.execute(rtspCaptureThread);
    }

    /**
     * Stop both Capture and Stream Thread
     */
    public void stopAll() {
        rtspCaptureThread.setRunning(false);
        rtspStreamThread.setRunning(false);
    }

    public RtspStreamThread getRtspStreamThread() {
        return rtspStreamThread;
    }

    public void setRtspStreamThread(RtspStreamThread rtspStreamThread) {
        this.rtspStreamThread = rtspStreamThread;
    }

    public RtspCaptureThread getRtspCaptureThread() {
        return rtspCaptureThread;
    }

    public void setRtspCaptureThread(RtspCaptureThread rtspCaptureThread) {
        this.rtspCaptureThread = rtspCaptureThread;
    }

    public String getRtsp() {
        return rtsp;
    }

    public void setRtsp(String rtsp) {
        this.rtsp = rtsp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
