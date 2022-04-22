package models;

/**
 * This class is the abstract class for composing colors.
 */
public class ColorComposer
{
    public float alpha;
    public float r, g, b;
    /**
     * An alpha-grayscale value for float RGB values.
     * @param alpha the transparency [0-1]
     * @param r the red component [0-1] in float format.
     * @param g the green component [0-1] in float format.
     * @param b the blue component [0-1] in float format.
     */
    public ColorComposer(double alpha, double r, double g, double b)
    {
        this.alpha = (float) alpha;
        this.r = (float)(r/255); this.g = (float)(g/255); this.b = (float) (b/255);
    }
    public int getRed() { return (int) (r); }
    public int getGreen() { return (int) (g); }
    public int getBlue() { return (int) (b); }
}