package Thread;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_BGR24;

public class RtspCaptureThread implements Runnable{
    private boolean running = true;
    private RtspStreamThread mUIThread;
    private String rtsp;
    private int preview_width;
    private int preview_height;
    private int frameRate;
    private boolean initName = false;
    private String deviceId;
    private FFmpegFrameGrabber streamGrabber;
    private Object lockStreamCapture = new Object();
    private BlockingBuffer mFrameBuffer;
    private boolean isGPU = false;
    OpenCVFrameConverter.ToMat convToMat = new OpenCVFrameConverter.ToMat();
    OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
    OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
    org.bytedeco.javacv.Frame frame = null;

    public RtspCaptureThread(RtspStreamThread mUIThread, String rtsp, int preview_width, int preview_height, int frameRate, String deviceId) {
        this.mUIThread = mUIThread;
        this.rtsp = rtsp;
        this.preview_width = preview_width;
        this.preview_height = preview_height;
        this.frameRate = frameRate;
        this.deviceId = deviceId;
        mFrameBuffer = new BlockingBuffer(10);

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void createRtspGrabber(String rtspInput){
        synchronized (this.lockStreamCapture){
            if (streamGrabber!=null){
                try{
                    streamGrabber.stop();
                } catch (FFmpegFrameGrabber.Exception e) {
                    e.printStackTrace();
                }

            }
            streamGrabber = new FFmpegFrameGrabber(rtspInput);
            streamGrabber.setFormat("RTSP");
            if(this.isGPU){
                streamGrabber.setOption("hwaccel", "cuvid");
                streamGrabber.setVideoCodecName("h264_cuvid");
            }else{
                streamGrabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            }

            streamGrabber.setOption("rtsp_transport", "tcp");
            streamGrabber.setOption("hwaccel", "nvdec");
            streamGrabber.setImageWidth(preview_width);
            streamGrabber.setImageHeight(preview_height);
            streamGrabber.setOption("tune", "zerolatency");
            streamGrabber.setOption("an", "");
            streamGrabber.setOption("sn", "");
            streamGrabber.setOption("dn", "");
            streamGrabber.setOption("fflags", "nobuffer");
            streamGrabber.setOption("flags", "low_delay");
            streamGrabber.setOption("framedrop", "");
            streamGrabber.setOption("avioflags", "direct");
            streamGrabber.setFrameRate(frameRate);
            streamGrabber.setPixelFormat(AV_PIX_FMT_BGR24);// AV_PIX_FMT_RGBA);
            // set timeout
            streamGrabber.setOption("stimeout", "1000000");

        }
    }

    public FFmpegFrameGrabber getStreamGrabber() {
        return streamGrabber;
    }

    @Override
    public void run() {
        if (!initName){
            Thread.currentThread().setName(Thread.currentThread().getName().replace("##",this.getClass()+"-"+deviceId));
            initName = true;
        }
        createRtspGrabber(rtsp);
        try{
            getStreamGrabber().start();
        } catch (FFmpegFrameGrabber.Exception e) {
            System.out.println("heyyy");
            e.printStackTrace();
        }
        while (running){
            decodeFrame();
        }
    }

    public void decodeFrame(){
        synchronized (this.lockStreamCapture){
            try {
                if ((frame=getStreamGrabber().grabImage())!=null){

                    mUIThread.pushFrame(frame.clone());

                    mFrameBuffer.push(frame.clone());
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FFmpegFrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRtsp() {
        return rtsp;
    }

    public void setRtsp(String rtsp) {
        this.rtsp = rtsp;
    }

    public synchronized BlockingBuffer getmFrameBuffer() {
        return mFrameBuffer;
    }

    public void setmFrameBuffer(BlockingBuffer mFrameBuffer) {
        this.mFrameBuffer = mFrameBuffer;
    }
}
