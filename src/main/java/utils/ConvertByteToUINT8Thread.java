package utils;

import config.AppfileConfig;
import config.SpringContext;
import entity.TritonInputData;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ConvertByteToUINT8Thread implements Runnable{
    private Frame frame = null;
    private String deviceId;
    private int[] outputArray = null;
    private List<TritonInputData> listInputData;
    private List<Integer> listInputSize;
    private CountDownLatch countDownLatch;
    AppfileConfig appfileConfig;

    public ConvertByteToUINT8Thread(Frame frame, String deviceId, List<TritonInputData> listInputData, List<Integer> listInputSize, CountDownLatch countDownLatch) {
        this.frame = frame;
        this.deviceId = deviceId;
        this.listInputData = listInputData;
        this.listInputSize = listInputSize;
        this.countDownLatch = countDownLatch;
        appfileConfig = SpringContext.context.getBean("appfileConfig", AppfileConfig.class);
    }

    @Override
    public void run()  {
        try{
            //to ByteBuffer
            BufferedImage bi = new Java2DFrameConverter().convert(frame);;
            //to byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            byte[] ba = baos.toByteArray();
            //to int8
            int[] intArray = new int[ba.length];
            for (int i=0;i<ba.length;i++){
                intArray[i] = ba[i] & 0xFF;
            }

            listInputSize.add(intArray.length);
            listInputData.add(new TritonInputData(deviceId, intArray.clone()));
            countDownLatch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
