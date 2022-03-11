package entity;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

public class BBox {
    private int x;
    private int y;
    private int w;
    private int h;
    private double score;

    public BBox(int x1, int y1, int x2, int y2, double score) {

        this.w = Math.abs(x1-x2);
        this.h = Math.abs(y1-y2);
        this.x = x1+this.w/2;
        this.y = y1+this.h/2;
        this.score = score;
    }

//    public BBox(Document document) {
//        x1 = (int) document.get("x1");
//        y1 = (int) document.get("y1");
//        w = (int) document.get("w");
//        h = (int) document.get("h");
//        score = (double) document.get("score");
//    }

    public DBObject toDBObject(){
        return new BasicDBObject("x1",x).append("y1",y).append("w",w).append("h",h).append("score", score);
    }

    public void converFromDBObject(BasicDBObject DBObject){
        x = (int) DBObject.get("x");
        y = (int) DBObject.get("y");
        w = (int) DBObject.get("w");
        h = (int) DBObject.get("h");
        score = (double) DBObject.get("score");
    }

    public void converFromDocument(Document DBObject){
        x = (int) DBObject.get("x");
        y = (int) DBObject.get("y");
        w = (int) DBObject.get("w");
        h = (int) DBObject.get("h");
        score = (double) DBObject.get("score");
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

    public void setY(int y1) {
        this.y = y1;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "BBox{" +
                "x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", score=" + score +
                '}';
    }
}
