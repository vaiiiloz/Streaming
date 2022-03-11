package Thread;

import Mongo.MongoHandler;
import config.AppfileConfig;
import config.SpringContext;
import entity.BBox;
import entity.PeopleBox;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RtspStreamThread implements Runnable {

    private final int UIBufferSize;
    private float scaleX;
    private float scaleY;
    private BlockingBuffer mFrameBuffer;
    private BlockingBuffer mFaceBuffer;
    private boolean updated = false;
    int preview_width;
    int preview_height;
    private final String devicename;

    private final Boolean isStreaming;
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private List listFaceBox;

    private OutputStream stdin = null;
    DateTimeFormatter formatter;

    private boolean running = true;
    private boolean initName = false;

    private boolean resetTimer = true;
    private long startTimer = 0;
    private HashMap<Integer, BlockingBuffer> bufferTrackBox;
    private List lastListFaceBox;
    private Boolean isSmartRecord;
    private MongoHandler mongoHandler;
    ;

    private VideoRecordThread videoRecordThread;
    private Boolean isRecord = false;
    AppfileConfig appfileConfig;
//


    public synchronized BlockingBuffer getmFaceBuffer() {
        return mFaceBuffer;
    }

    public void setmFaceBuffer(BlockingBuffer mFaceBuffer) {
        this.mFaceBuffer = mFaceBuffer;
    }

    public BlockingBuffer getmFrameBuffer() {
        return mFrameBuffer;
    }

    public void setmFrameBuffer(BlockingBuffer mFrameBuffer) {
        this.mFrameBuffer = mFrameBuffer;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public RtspStreamThread(Boolean isStreaming, String devicename, int preview_width, int preview_height, int UIBufferSize, MongoHandler mongoHandler) {
        mFrameBuffer = new BlockingBuffer(UIBufferSize);
        mFaceBuffer = new BlockingBuffer(4);
        listFaceBox = new ArrayList<>();
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.mongoHandler = mongoHandler;
        this.isStreaming = isStreaming;

        this.devicename = devicename;
        this.videoRecordThread = new VideoRecordThread(devicename);

        this.preview_height = preview_height;
        this.preview_width = preview_width;
        this.UIBufferSize = UIBufferSize;
        this.lastListFaceBox = new ArrayList<>();
        this.isSmartRecord = isSmartRecord;
        appfileConfig = SpringContext.context.getBean("appfileConfig", AppfileConfig.class);
//        this.isRecord = appfileConfig.isRecord;


    }

    public void pushFrame(Frame framemat) {
        try {
            mFrameBuffer.push(framemat);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pushBox(BBox bbox) {
        try {
            mFaceBuffer.push(bbox);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!initName) {
            Thread.currentThread().setName(Thread.currentThread().getName().replace("##", this.getClass().getSimpleName() + "-" + devicename));
            initName = true;
        }
//        videoRecordThread.startRecordVideo();
        //output ffmepeg erro
        //File ffmpeg_err = new File("ffmpeg_err.log");
        //processBuilder.redirectError(ffmpeg_err);
        try {
//            process = processBuilder.start();
            while (running) {
                Streaming();

                Thread.sleep(10);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void Streaming() {

        if (mFrameBuffer.size() > 0) {
            try {

                Frame frameMatUI = mFrameBuffer.pop();

                Mat imgMat = new Mat();

                BufferedImage heatmap = new BufferedImage(preview_width, preview_height, 6);


                if (frameMatUI.image == null) {
                    return;
                }

                if (mFaceBuffer.size() > 0) {

                    PeopleBox faces = mFaceBuffer.pop();

                    long startTotalTime = System.currentTimeMillis();


                    listFaceBox = faces.getbBoxes();

                    if (listFaceBox.size() > 0) {

//                        switch (appfileConfig.modelType){
//                            case "scrfd":{
//                                imgMat = Renderer.renderAllBox(frameMatUI,listFaceBox);
//                                break;
//                            }
//                            case "rapid":{
//                                imgMat = Renderer.renderALLPolygon(frameMatUI,listFaceBox);
//                                break;
//                            }
//                        }


                        lastListFaceBox = listFaceBox;

                    } else {
                        lastListFaceBox.clear();
                    }
//                    System.out.println(listFaceBox.size());
                    //timer for mongodb

                    if (resetTimer) {
                        startTimer = System.currentTimeMillis();
                        resetTimer = false;
                    }
                    long duration = System.currentTimeMillis() - startTimer;

                    if (duration >= appfileConfig.waittime * 1000) {
                        resetTimer = true;
                        StreamingOutMongo(faces);
//                        StreamingOut(frameMatUI);

                    }
//                    listFaceBox.clear();

                } else {
//                    switch (appfileConfig.modelType){
//                        case "scrfd":{
//                            imgMat = Renderer.renderAllBox(frameMatUI,lastListFaceBox);
//                            break;
//                        }
//                        case "rapid":{
//                            imgMat = Renderer.renderALLPolygon(frameMatUI,lastListFaceBox);
//                            break;
//                        }
//                    }
                }

                if (imgMat.empty()) {
                    return;
                }

                if (isRecord) {
                    HandelVideo(frameMatUI);
                }
//                StreamingOut(imgMat);
                imgMat.release();
                frameMatUI.close();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }


        }

    }

    public void HandelVideo(Frame frame) {
        if (videoRecordThread.ismIsRecording()) {
            videoRecordThread.pushRecordDate(frame.clone());
        }
    }

    public void StreamingOutMongo(PeopleBox peopleBox) throws InterruptedException {

//        mongoHandler.addPeople(peopleBox, videoRecordThread.getRecorder().getFrameNumber()+1);
        mongoHandler.addPeople(peopleBox);
    }


    public void StreamingOut(Frame frame) {
        if (isStreaming) {

            OpenCVFrameConverter.ToMat convToMat = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
            org.bytedeco.opencv.opencv_core.Mat mat = convToMat.convert(frame.clone());
            Mat imgMat = converter2.convert(converter1.convert(mat.clone()));

            String directoryName = String.format("%s/%s", appfileConfig.output_folder, devicename);


            int idx = new File(directoryName).list().length;
//                OutputStream stdin = new FileOutputStream(new File(String.format("%s/%s/%d.jpg", appfileConfig.output_folder, devicename, idx)));
            Imgcodecs.imwrite(String.format("%s/%s/%s.jpg", appfileConfig.output_folder, devicename, idx), imgMat);

        }

    }

    public void StreamingOutHeatMap(BufferedImage heatmap) {
        if (isStreaming) {
            if (heatmap == null) {
                return;
            }
            String directoryName = String.format("%s/%s", appfileConfig.output_folder, devicename);


            int idx = new File(directoryName).list().length;

            try {
//                ImageIO.write(heatmap, "jpg", new File(String.format("%s/%s/%d.jpg", appfileConfig.output_folder, devicename, idx)));
                ImageIO.write(heatmap, "jpg", new File(String.format("%s/%s/%s.jpg", appfileConfig.output_folder, devicename, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
