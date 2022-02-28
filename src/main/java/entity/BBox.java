package entity;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;

public class BBox {
    private int x1;
    private int y1;
    private int w;
    private int h;
    private double score;

    public BBox(int x1, int y1, int x2, int y2, double score) {
        this.x1 = x1;
        this.y1 = y1;
        this.w = Math.abs(x1-x2);
        this.h = Math.abs(y1-y2);
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
        return new BasicDBObject("x1",x1).append("y1",y1).append("w",w).append("h",h).append("score", score);
    }

    public void converFromDBObject(BasicDBObject DBObject){
        x1 = (int) DBObject.get("x1");
        y1 = (int) DBObject.get("y1");
        w = (int) DBObject.get("w");
        h = (int) DBObject.get("h");
        score = (double) DBObject.get("score");
    }

    public void converFromDocument(Document DBObject){
        x1 = (int) DBObject.get("x1");
        y1 = (int) DBObject.get("y1");
        w = (int) DBObject.get("w");
        h = (int) DBObject.get("h");
        score = (double) DBObject.get("score");
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
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
                "x1=" + x1 +
                ", y1=" + y1 +
                ", w=" + w +
                ", h=" + h +
                ", score=" + score +
                '}';
    }
}
