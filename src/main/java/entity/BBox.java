package entity;

public class BBox implements Entity{
    private int x;
    private int y;
    private int w;
    private int h;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private double score;

    public BBox(int x1, int y1, int x2, int y2, double score) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.w = Math.abs(x1-x2);
        this.h = Math.abs(y1-y2);
        this.x = x1+this.w/2;
        this.y = y1+this.h/2;
        this.score = score;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
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
