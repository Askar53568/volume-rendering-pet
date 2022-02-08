public class AlphaControlPoint {
    private double alpha;
    private int isovalue;

    public AlphaControlPoint(int isovalue, double alpha){
        this.alpha=alpha;
        this.isovalue = isovalue;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setIsovalue(int isovalue) {
        this.isovalue = isovalue;
    }

}
