package Thread;

import Mongo.CephHandler;
import Mongo.MongoHandler;
import config.Constants;
import entity.BBox;
import entity.PeopleBox;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import utils.Renderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
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
    private int save_hour = -1;
    private HashMap<Integer, BlockingBuffer> bufferTrackBox;
    private List lastListFaceBox;
    private Boolean isSmartRecord;
    private MongoHandler mongoHandler;
    private Java2DFrameConverter biconvert = new Java2DFrameConverter();
    private String path;

    private VideoRecordThread videoRecordThread;
    private Boolean isRecord;
    private Boolean isSave;
    private CephHandler cephHandler;
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
        cephHandler = new CephHandler(Constants.CEPH_ACCESS_KEY, Constants.CEPH_PRIVATE_KEY, Constants.CEPH_HOST_NAME);
        isRecord = Constants.IS_RECORD;
        isSave = Constants.IS_SAVE;


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

        if (isRecord){
            videoRecordThread.startRecordVideo();
        }


        try {

            while (running) {
                Streaming();

                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void Streaming() {
        //Run if have frame
        if (mFrameBuffer.size() > 0) {
            try {

                Frame frameMatUI = mFrameBuffer.pop();

                Frame renderFrame = frameMatUI.clone();
                //push background to ceph in first hour
                if (save_hour == -1) {
                    //get current hour
                    int current_hour = (new Date()).getHours();
                    save_background(frameMatUI, current_hour, System.currentTimeMillis());
                    save_hour = current_hour;
                } else {

                    //get current hour
                    int current_hour = (new Date()).getHours();
                    if (current_hour != save_hour) {
                        save_background(frameMatUI, current_hour, System.currentTimeMillis());
                        save_hour = current_hour;
                    }
                }

                // pass if frame do not have image
                if (frameMatUI.image == null) {
                    return;
                }

                //Save to mongo if detect face
                if (mFaceBuffer.size() > 0) {

                    PeopleBox faces = mFaceBuffer.pop();

                    long startTotalTime = System.currentTimeMillis();


                    listFaceBox = faces.getbBoxes();

                    if (listFaceBox.size() > 0) {
                        if (isRecord){
                            if (Constants.ModelType.equals("scrfd")){
                                renderFrame = Renderer.renderAllBox(frameMatUI, listFaceBox);
                            }else {
                                renderFrame = Renderer.renderALLPolygon(frameMatUI, listFaceBox);
                                if (isSave){
                                    StreamingOut(renderFrame);
                                }

                            }
                        }


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

                    if (duration >= Constants.WAITTIME * 1000) {
                        resetTimer = true;
                        StreamingOutMongo(faces);
//                        StreamingOut(frameMatUI);

                    }


                }else{
                    if (isRecord){
                        if (Constants.ModelType.equals("scrfd")){
                            renderFrame = Renderer.renderAllBox(frameMatUI, lastListFaceBox);
                        }else {
                            renderFrame = Renderer.renderALLPolygon(frameMatUI, lastListFaceBox);
                            if (isSave){
                                StreamingOut(renderFrame);
                            }

                        }
                    }

                }



                //record video
                if (isRecord) {
                    HandelVideo(renderFrame);
                }
                listFaceBox.clear();
                frameMatUI.close();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }


        }

    }

    public void save_background(Frame frame, int hour, long time) {
        try {
            //create save path
            String key = String.format("%s/%s", Constants.CEPH_FOLDER, devicename);
            path = String.format("%s/%d.%s", key, hour, Constants.CEPH_BACKGROUND_TYPE);
            //push mongo
            mongoHandler.addBackground(devicename, path, hour, time);
            //convert to Bufferimage
            BufferedImage image = biconvert.convert(frame);

            //convert to Input Stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, Constants.CEPH_BACKGROUND_TYPE, baos);
            byte[] buffer = baos.toByteArray();
            InputStream is = new ByteArrayInputStream(buffer);

            //push ceph
            cephHandler.addBackground(Constants.CEPH_BUCKET, path, Constants.CEPH_BACKGROUND_TYPE, buffer.length, is);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void HandelVideo(Frame frame) {
        if (videoRecordThread.ismIsRecording()) {
            videoRecordThread.pushRecordDate(frame.clone());
        }
    }

    public void StreamingOutMongo(PeopleBox peopleBox) throws InterruptedException {

//        mongoHandler.addPeople(peopleBox, videoRecordThread.getRecorder().getFrameNumber()+1);
        mongoHandler.addPeople(peopleBox, path);
    }


    public void StreamingOut(Frame frame) {
        if (isStreaming) {

            OpenCVFrameConverter.ToMat convToMat = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
            org.bytedeco.opencv.opencv_core.Mat mat = convToMat.convert(frame.clone());
            Mat imgMat = converter2.convert(converter1.convert(mat.clone()));

            String directoryName = String.format("%s/%s", Constants.output_folder, devicename);


            int idx = new File(directoryName).list().length;
            Imgcodecs.imwrite(String.format("%s/%s/%s.jpg", Constants.output_folder, devicename, idx), imgMat);

        }

    }

    public void StreamingOutHeatMap(BufferedImage heatmap) {
        if (isStreaming) {
            if (heatmap == null) {
                return;
            }
            String directoryName = String.format("%s/%s", Constants.output_folder, devicename);


            int idx = new File(directoryName).list().length;

            try {

                ImageIO.write(heatmap, "jpg", new File(String.format("%s/%s/%s.jpg", Constants.output_folder, devicename, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
