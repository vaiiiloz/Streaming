package Thread;


import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import utils.DateUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static config.Constants.THREADSCHEDULED_BACKGROUND;

public class VideoRecordThread implements Runnable {
    private final int RECORDBUFFER_SIZE = 3;
    private final int NUMBER_OF_FRAME = 30000;
    private final int RECORD_FRAME_PER_ONE_VIDEO = 110;
    private final int RECORD_WIDTH = 768;
    private final int RECORD_HEIGHT = 512;
    private final int RECORD_FPS = 15;
    private BlockingBuffer mRecordBuffer;
    private BlockingBuffer eventVideoBuffer;
    private boolean mIsRecording = false;
    private boolean mIsStop = false;
    private ExecutorService excutor = Executors.newFixedThreadPool(4);
    private FFmpegFrameRecorder recorder;
    private VideoRecordThread mInstance = null;
    //    private EventVideoAiRunner eventVideoAiRunner;
    private String deviceName = "";
    private String accessKey = "";

    public VideoRecordThread(String deviceName) {
        mRecordBuffer = new BlockingBuffer(RECORDBUFFER_SIZE);
        eventVideoBuffer = new BlockingBuffer(5);
        this.deviceName = deviceName;

    }

    public BlockingBuffer getEventVideoBuffer() {
        return eventVideoBuffer;
    }


    @Override
    public void run() {
        while (!mIsStop) {
            try {
                Thread.sleep(THREADSCHEDULED_BACKGROUND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized String startRecordVideo() {
        if (!mIsRecording) {
            mIsRecording = true;
            File video_file = generateVideoFile();
            String video_id = video_file.getName();
//            eventVideoAiRunnable = new EventVideoAiRunnable
            RecordingVideo recordingVideo = new RecordingVideo(video_file);
            excutor.execute(recordingVideo);
            return video_id;
        }
        return null;
    }

    private File generateVideoFile() {
        final Long time = System.currentTimeMillis();
        File dic = new File("output/" + deviceName);
        if (!dic.exists()) {
            dic.mkdirs();
        }
        final String timeString = DateUtils.getTimeStringNow("HH_mm_ss");

        String path = deviceName + "_" + timeString + ".mp4";
        return new File(path);
    }

    public boolean ismIsRecording() {
        return mIsRecording;
    }

    public void setmIsRecording(boolean mIsRecording) {
        this.mIsRecording = mIsRecording;
    }

    public void pushRecordDate(Frame frame) {
        try {
            mRecordBuffer.push(frame);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public FFmpegFrameRecorder newRecorder(String filename, int width, int height, int fps) {
        Frame frame = null;
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(filename, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setFrameRate(fps);
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P10());
        recorder.setVideoBitrate(0);
        return recorder;
    }

    public FFmpegFrameRecorder getRecorder() {
        return recorder;
    }

    public synchronized void stopRecord() {
        mIsStop = true;
    }

    private class RecordingVideo implements Runnable {

        private File file;

        public RecordingVideo(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            int count = 0;

            try {
                recorder = newRecorder(file.getName(), RECORD_WIDTH, RECORD_HEIGHT, RECORD_FPS);
                recorder.start();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }

            while (mIsRecording) {
                try {
                    if (mRecordBuffer.size() > 0) {
                        Frame frame = mRecordBuffer.takeFirst();
                        if (frame.image != null) {
                            count++;
                            recorder.record(frame);
                            frame.close();
                        }
                    }

                    if (count == NUMBER_OF_FRAME) {
                        recorder.stop();
                        recorder.release();
                        System.out.println(count);
                        System.out.println("Save file " + file.getName());
                        mRecordBuffer.clear();
                        mIsRecording = false;
                        break;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (FFmpegFrameRecorder.Exception e) {

                    e.printStackTrace();
                }
            }

            if (mIsRecording == false) {
                try {
                    recorder.stop();
                    recorder.release();
                    mRecordBuffer.clear();

                } catch (FFmpegFrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
