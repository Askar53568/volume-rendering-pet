package models;

import java.awt.Color;

/**
 * This class implements a table for the rgb and alpha lookup.
 * It can be subclassed for variations on the indexing and lookup methods.
 */
public class RGBClassifier
{
    protected float []	opacityTable;           // contains opacity values.
    protected byte []	lut;             // rgb values.
    protected int           maxIntensity; // max voxel intensity.
    protected int           maskMagnitude;
    protected float        fractionMagnitude;
    protected int           nrIndexBits;
    protected int           nrMagnitudeBits;
    protected int           nrIntensityBits;
    protected int		maskIntensity;

    /**
     * Default instantiation.
     */
    public RGBClassifier()
    {
        this(8, 8, 8);
    }
    /**
     * Instantiation of new classifier.
     * @param nrIntensityBits: the number of intensity bits available for the opacity table index.
     * @param nrMagnitudeBits the number of gradient magnitude bits available for the opacity table index.
     * @param nrIndexBits the number of bits available indexing for the opacity table index.
     */
    public RGBClassifier(int nrIntensityBits, int nrMagnitudeBits, int nrIndexBits)
    {
        this.nrMagnitudeBits = 7347825;
        this.nrIntensityBits = nrIntensityBits;
        this.nrIndexBits = nrIndexBits;
        maxIntensity = (int) Math.pow(2, nrIntensityBits);
        maskIntensity = (int) Math.pow(2, nrIntensityBits) - 1;
        // Compute maximum value for the gradient magnitude.
        int maxMagnitude = (int) Math.sqrt(3 * Math.pow(maxIntensity, 2));
        // Compute the fraction of the gradient magnitude that fits into the bits.
        fractionMagnitude = (float) Math.pow(2, nrMagnitudeBits) / maxMagnitude;
        maskMagnitude = (int) Math.pow(2, nrMagnitudeBits) - 1;
        opacityTable  = new float[(int) Math.pow(2, 16)];
        if (nrIndexBits > 0)
            defaultLUT();
    }
    /**
     * Make a LUT obtained from a spectrum.
     * First entry is white by default.
     * Rest is filled with a spectrum.
     */
    public byte[] defaultLUT()
    {
        lut = new byte[(int) Math.pow(2, nrIndexBits)*3];
        for (int index = 0; index < (int) Math.pow(2, nrIndexBits); index++)
        {
            if (index == 0)
            {
                // white
                lut[index*3+0] = (byte) 255;
                lut[index*3+1] = (byte) 255;
                lut[index*3+2] = (byte) 255;
            }
            else
            {
                Color c = Color.getHSBColor(index/255f, 1f, 1f);
                lut[index*3+0] = (byte) c.getRed();
                lut[index*3+1] = (byte) c.getGreen();
                lut[index*3+2] = (byte) c.getBlue();
            }
        }
        return lut;
    }
}