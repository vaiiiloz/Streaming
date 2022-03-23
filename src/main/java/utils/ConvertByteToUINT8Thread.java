package utils;

import entity.TritonInputData;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ConvertByteToUINT8Thread implements Runnable{
    private Frame frame = null;
    private String deviceId;
    private int[] outputArray = null;
    private List<TritonInputData> listInputData;
    private List<Integer> listInputSize;
    private CountDownLatch countDownLatch;

    public ConvertByteToUINT8Thread(Frame frame, String deviceId, List<TritonInputData> listInputData, List<Integer> listInputSize, CountDownLatch countDownLatch) {
        this.frame = frame;
        this.deviceId = deviceId;
        this.listInputData = listInputData;
        this.listInputSize = listInputSize;
        this.countDownLatch = countDownLatch;
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
            try{
                listInputSize.add(intArray.length);
                listInputData.add(new TritonInputData(deviceId, intArray.clone()));
            }catch (Exception e){
                try{
                    listInputSize.add( listInputSize.size(),intArray.length);
                    listInputData.add( listInputData.size(),new TritonInputData(deviceId, intArray.clone()));
                }catch (Exception ee){
                    e.printStackTrace();
                    System.out.println("Still fail");
                }

//                System.out.println("intArray is "+(intArray == null));
//                System.out.println("intArray length"+intArray.length);

            }


//            Thread.sleep(1);
            countDownLatch.countDown();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
