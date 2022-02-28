package entity;

public class Coordinate {
    private int x;
    private int y;

    public int getX() {
        return Math.round(x);
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return Math.round(y);
    }

    public void setY(int y) {
        this.y = y;
    }

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
