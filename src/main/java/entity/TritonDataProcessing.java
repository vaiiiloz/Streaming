package entity;

import inference.GrpcService;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class TritonDataProcessing {


    /**
     * Convert the response from Buffer to array
     * @param buffer
     * input buffer
     * @return array convert from input buffer
     */
    public static float[] convertOutputToFloatArray(FloatBuffer buffer) {

        if (buffer.hasArray()) {// return array if the buffer already have one
            return buffer.array();
        } else {// else put each value in buffer into a new array
            float[] array = new float[buffer.capacity()];
            for (int i = 0; i < buffer.capacity(); i++) {
                array[i] = buffer.get(i);
            }
            return array;
        }
    }

    /**
     * Process the response from model rapid (polygon) triton server to get the list of polygon in each image of the batch size
     * @param response
     * @param listDeviceId
     * @return each image have a list of polygon
     */
    public static List<TritonDetectedResults> peoplePostProcessRapid(GrpcService.ModelInferResponse response, List<String> listDeviceId) {
        // triton model response, output1 is the number of polygon, output0 is the list of polygon of all image
        float[] output1 = convertOutputToFloatArray(response.getRawOutputContentsList().get(0).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());
        float[] output0 = convertOutputToFloatArray(response.getRawOutputContentsList().get(1).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());

        List<TritonDetectedResults> listResult = new ArrayList<>();

        int j = 0;
        // find the list of polygon in each image
        for (int i = 0; i < listDeviceId.size(); i++) {
            //get number of polygon in the corresponding image from output1
            int numOfBox = (int) output1[i];
            List<Polygon> listPolygon = new ArrayList<>();
            //get the numOfBox polygon in output0
            while (numOfBox > 0) {
                Polygon polygon = new Polygon((int) output0[j], (int) output0[j + 1], (int) output0[j + 2], (int) output0[j + 3], output0[j + 4], output0[j + 5]);
                listPolygon.add(polygon);
                j = j + 6;
                numOfBox--;
            }
            TritonDetectedResults result = new TritonDetectedResults(listDeviceId.get(i), listPolygon);
            listResult.add(result);
        }


        return listResult;
    }

    /**
     * Process the response from model scrfd (box) triton server to get the list of box in each image of the batch size
     * @param response
     * @param listDeviceId
     * @return each image have a list of box
     */
    public static List<TritonDetectedResults> peoplePostProcessScrfd(GrpcService.ModelInferResponse response, List<String> listDeviceId) {
        // triton model response, output1 is the number of box, output0 is the list of box of all image
        float[] output1 = convertOutputToFloatArray(response.getRawOutputContentsList().get(0).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());
        float[] output0 = convertOutputToFloatArray(response.getRawOutputContentsList().get(1).asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());

        List<TritonDetectedResults> listResult = new ArrayList<>();

        int j = 0;
        // find the list of box in each image
        for (int i = 0; i < listDeviceId.size(); i++) {
            //get number of box in the corresponding image from output1
            int numOfBox = (int) output1[i];
            List<BBox> listBBox = new ArrayList<>();
            //get the numOfBox box in output0
            while (numOfBox > 0) {
                BBox bBox = new BBox((int) output0[j], (int) output0[j + 1], (int) output0[j + 2], (int) output0[j + 3], output0[j + 4]);
                listBBox.add(bBox);
                j = j + 5;
                numOfBox--;
            }
            TritonDetectedResults result = new TritonDetectedResults(listDeviceId.get(i), listBBox);

            listResult.add(result);
        }
        return listResult;
    }


}
