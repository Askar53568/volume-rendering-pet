package models;

public class RGBControlPoint {
private double r;
private double g;
private double b;

    public RGBControlPoint(double r, double g, double b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }
}
