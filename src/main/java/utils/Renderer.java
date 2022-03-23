package utils;


import entity.BBox;
import entity.Coordinate;
import entity.Polygon;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    public static Frame renderAllBox(Frame frame, List<BBox> bboxes){
        OpenCVFrameConverter.ToMat convToMat = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
        org.bytedeco.opencv.opencv_core.Mat mat = convToMat.convert(frame.clone());
        Mat imgMat = converter2.convert(converter1.convert(mat));
        org.opencv.core.Scalar color = new org.opencv.core.Scalar(30,255,255,0);

        for (BBox box:bboxes){
            Rect rect = new Rect(box.getX(), box.getY(),box.getW(), box.getH());
            Imgproc.rectangle(imgMat,rect,color,4,1,0);
        }
//        convToMat.close();
        return converter2.convert(imgMat);



    }


    public static Frame renderALLPolygon(Frame frame, List<Polygon> polygonList){
        OpenCVFrameConverter.ToMat convToMat = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
        org.bytedeco.opencv.opencv_core.Mat imgMat = convToMat.convert(frame).clone();
        Mat mat = converter2.convert(converter1.convert(imgMat));

        org.opencv.core.Scalar color = new org.opencv.core.Scalar(30,255,255,0);

        for (int num_box=0;num_box<polygonList.size();num_box++){
            Polygon polygon = polygonList.get(num_box);
            List<MatOfPoint> list = new ArrayList<>();
            ArrayList<Point> pointsOrdered = new ArrayList<>();

            for (Coordinate coord:polygon.getCoords()){

                pointsOrdered.add(new org.opencv.core.Point( coord.getX(),  coord.getY()));
            }
            MatOfPoint sourceMat = new MatOfPoint();
            sourceMat.fromList(pointsOrdered);
            list.add(sourceMat);
            try {
                Imgproc.polylines(mat, list, true, color,2);

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return converter2.convert(mat);

    }
}
