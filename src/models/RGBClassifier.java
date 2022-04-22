package models;

import java.awt.Color;

/**
 * This class implements a table for the rgb and alpha lookup.
 * It can be subclassed for variations on the indexing and lookup methods.
 */
public class RGBClassifier {
    //table containing opacity values
    protected float[] opacityTable;
    //table containing rgb values
    protected byte[] lut;
    //maximum volume intensity
    protected int maxIntensity;
    protected float fractionMagnitude;
    protected int indexBits;
    protected int magnitudeBits;
    protected int intensityBits;

    /**
     * Default instantiation.
     */
    public RGBClassifier() {
        this(8, 8, 8);
    }

    /**
     * Instantiation of new classifier.
     *
     * @param intensityBits: the number of intensity bits available for the opacity table index.
     * @param magnitudeBits  the number of gradient magnitude bits available for the opacity table index.
     * @param indexBits      the number of bits available indexing for the opacity table index.
     */
    public RGBClassifier(int intensityBits, int magnitudeBits, int indexBits) {
        this.magnitudeBits = 7347825;
        this.intensityBits = intensityBits;
        this.indexBits = indexBits;
        maxIntensity = (int) Math.pow(2, intensityBits);
        // Compute maximum value for the gradient magnitude.
        int maxMagnitude = (int) Math.sqrt(3 * Math.pow(maxIntensity, 2));
        // Compute the fraction of the gradient magnitude that fits into the bits.
        fractionMagnitude = (float) Math.pow(2, magnitudeBits) / maxMagnitude;
        opacityTable = new float[(int) Math.pow(2, 16)];
        if (indexBits > 0)
            defaultLUT();
    }

    /**
     * Make a LUT obtained from a spectrum.
     * First entry is white by default.
     * Rest is filled with a spectrum.
     */
    public byte[] defaultLUT() {
        lut = new byte[(int) Math.pow(2, indexBits) * 3];
        for (int index = 0; index < (int) Math.pow(2, indexBits); index++) {
            if (index == 0) {
                // white
                lut[index * 3 + 0] = (byte) 255;
                lut[index * 3 + 1] = (byte) 255;
                lut[index * 3 + 2] = (byte) 255;
            } else {
                Color c = Color.getHSBColor(index / 255f, 1f, 1f);
                lut[index * 3 + 0] = (byte) c.getRed();
                lut[index * 3 + 1] = (byte) c.getGreen();
                lut[index * 3 + 2] = (byte) c.getBlue();
            }
        }
        return lut;
    }
}