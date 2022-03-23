package entity;

import java.util.ArrayList;
import java.util.List;

public class Polygon implements Entity{
    private List<Coordinate> coords;

    private int level;

    private double score;

    private int x;

    private int y;

    private int w;

    private int h;

    public Polygon(List<Coordinate> coords, int level, double score) {
        this.coords = coords;
        this.level = level;
        this.score = score;
    }

    public Polygon(int x, int y, int w, int h, float a, double score){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
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

    public int getX1(){
        return (x-w/2);
    }

    public int getX2(){
        return (x+w/2);
    }

    public int getY1(){
        return (y-h/2);
    }

    public int getY2(){
        return (y+h/2);
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    @Override
    public String toString() {
        return "Polygon{" +
                "coords=" + coords +
                ", level=" + level +
                '}';
    }
}
