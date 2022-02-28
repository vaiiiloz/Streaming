package entity;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private List<Coordinate> coords;

    private int level;

    private double score;

    public Polygon(List<Coordinate> coords, int level, double score) {
        this.coords = coords;
        this.level = level;
        this.score = score;
    }

    public Polygon(int x, int y, int w, int h, float a, double score){
        this.score = score;
        this.level = 4;

        //get coords from xywha
        List<Coordinate> coordinateList = new ArrayList<>();
        double cos = Math.cos(a/180*Math.PI);
        double sin = Math.sin(a/180*Math.PI);
//            System.out.println(a+" "+cos+" "+sin);
        double[][] R = new double[][]{new double[]{cos, sin}, new double[]{-sin, cos}};
        double[][] pts = new double[][]{new double[]{-w/2, -h/2},
                new double[]{w/2,-h/2},
                new double[]{w/2, h/2},
                new double[]{-w/2, h/2}};
        for (int i=0; i<pts.length;i++){
            int[] temp = new int[pts[i].length];
            for (int j=0;j<pts[i].length;j++){
                temp[j] = 0;
                for (int z=0;z<pts[i].length;z++){
                    temp[j] += (int) pts[i][z]*R[z][j];
                }

            }
            coordinateList.add(new Coordinate((int)(x +temp[0]),(int)(y+temp[1])));
        }

        this.coords = coordinateList;

    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<Coordinate> getCoords() {
        return coords;
    }

    public void setCoords(List<Coordinate> coords) {
        this.coords = coords;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

//    public int[] getX(){
//        int[] x = new int[coords.size()];
//        for (int i=0;i<coords.size();i++){
//            x[i] = coords.get(i).getX();
//        }
//        return x;
//    }
//
//    public int[] getY(){
//        int[] y = new int[coords.size()];
//        for (int i=0;i<coords.size();i++){
//            y[i] = coords.get(i).getY();
//        }
//        return y;
//    }

    @Override
    public String toString() {
        return "Polygon{" +
                "coords=" + coords +
                ", level=" + level +
                '}';
    }
}
