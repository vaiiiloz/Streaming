package entity;

import inference.GrpcService;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.resize;

public class TritonDataProcessing {

    public static float[] converInputMatToFloatArray(Mat input, int inputW, int inputH, int ch){
        input = input.clone();
        int orgH = input.rows();
        int orgW = input.cols();
        resize(input, input, new Size(inputH, inputW));

        byte[] bytes = new byte[(int) (input.total()*input.channels())];
        input.get(0,0,bytes);
        float[] floatArray = new float[bytes.length];

        for (int c = 0;c<ch;c++){
            for(int wh = 0; wh <inputW*inputH;wh++){
                int idxOutput = c*inputW*inputH+wh;
                int idxInput = wh*ch+c;
                byte b = bytes[idxInput];
                floatArray[idxOutput] = (float) (b & 0xFF)/255;
            }
        }
        return floatArray;
    }

    public static float[] convertOutputToFloatArray(FloatBuffer buffer) {

        if (buffer.hasArray()) {
            return buffer.array();
        } else {
            float[] array = new float[buffer.capacity()];
            for (int i = 0; i < buffer.capacity(); i++) {
                array[i] = buffer.get(i);
            }
            return array;
        }
    }


    public static List<TritonDetectedResults> peoplePostProcessRapid(GrpcService.ModelInferResponse response, List<String> listDeviceId){
        float[] output1 = convertOutputToFloatArray(response.getRawOutputContentsList().get(0).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());
        float[] output0 = convertOutputToFloatArray(response.getRawOutputContentsList().get(1).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());

        List<TritonDetectedResults> listResult = new ArrayList<>();

        int j = 0;
        for (int i=0;i<listDeviceId.size();i++){
            int numOfBox = (int) output1[i];
            List<Polygon> listPolygon = new ArrayList<>();
            while (numOfBox>0){
                Polygon polygon = new Polygon((int) output0[j], (int) output0[j+1], (int) output0[j+2], (int) output0[j+3], output0[j+4], output0[j+5]);
                listPolygon.add(polygon);
                j=j+6;
                numOfBox--;
            }
            TritonDetectedResults result = new TritonDetectedResults(listDeviceId.get(i),listPolygon);

            listResult.add(result);
        }
        return listResult;
    }

    public static List<TritonDetectedResults> peoplePostProcessScrfd(GrpcService.ModelInferResponse response, List<String> listDeviceId){

        float[] output1 = convertOutputToFloatArray(response.getRawOutputContentsList().get(0).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());
        float[] output0 = convertOutputToFloatArray(response.getRawOutputContentsList().get(1).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());

        List<TritonDetectedResults> listResult = new ArrayList<>();

        int j = 0;
        for (int i=0;i<listDeviceId.size();i++){
            int numOfBox = (int) output1[i];
            List<BBox> listBBox = new ArrayList<>();
            while (numOfBox>0){
                BBox bBox = new BBox((int) output0[j], (int) output0[j+1], (int) output0[j+2], (int) output0[j+3], output0[j+4]);
                listBBox.add(bBox);
                j=j+5;
                numOfBox--;
            }
            TritonDetectedResults result = new TritonDetectedResults(listDeviceId.get(i),listBBox);

            listResult.add(result);
        }
        return listResult;
    }


}
