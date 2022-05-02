package models;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

/**
 * Represents a volume renderer with 3 available transfer functions, linear interpolation , shading algorithm and a front-to-back composition algorithm.
 */
public class VolumeRender {
    //top view width
    private final int TOP_WIDTH;
    //top view height
    private final int TOP_HEIGHT;
    //front view width
    private final int FRONT_WIDTH;
    //front view height
    private final int FRONT_HEIGHT;
    //side view width
    private final int SIDE_WIDTH;
    //side view height
    private final int SIDE_HEIGHT;
    //reference to the volume
    private final Volume volume;
    //color classifier
    RGBClassifier colorClassifier = new RGBClassifier();
    //generate a color lookup table
    byte[] lut = colorClassifier.defaultLUT();
    //apply color transfer function?
    private boolean tfColor = false;
    //opacity slider variable
    private double opacity = 0.12;
    //apply gradient shading?
    private boolean isGradient = false;
    //apply interpolation?
    private boolean isGradientInterpolation = false;
    //light source x-axis
    private double lightX = 83;
    //threshold slider variable
    private double threshold = 800.0;
    private double levWidth;

    /**
     * Creates a CT viewer.
     *
     * @param volume The volume to use/display.
     */
    public VolumeRender(Volume volume) {
        this.volume = volume;
        this.TOP_WIDTH = volume.getCT_x_axis();
        this.TOP_HEIGHT = volume.getCT_y_axis();
        this.FRONT_WIDTH = volume.getCT_x_axis();
        this.FRONT_HEIGHT = volume.getCT_z_axis();
        this.SIDE_WIDTH = volume.getCT_x_axis();
        this.SIDE_HEIGHT = volume.getCT_z_axis();
    }

    /**
     * Gets the correct voxel for the direction provided (top, front, side)
     *
     * @param view The direction of the volume
     * @param x    The x value to get.
     * @param y    The y value to get.
     * @param z    The z value to get.
     * @return The voxel.
     */
    public short getVoxel(String view, int x, int y, int z) {
        if (view.equals("top")) {
            return volume.getVoxel(z, y, x);
        } else if (view.equals("side")) {
            return volume.getVoxel(y, x, z);
        } else {
            return volume.getVoxel(y, z, x);
        }
    }

    /**
     * Returns the specified slice of the CT scan.
     *
     * @param image The image to write to.
     * @param view  The direction of the volume.
     * @param slice The slice to display.
     */
    public void drawSlice(WritableImage image, String view, int slice) {
        int width = (int) image.getWidth(), height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double colour;
        short voxel;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                voxel = getVoxel(view, i, j, slice);
                colour = (((float) voxel - (float) volume.getMin()) / ((float) (volume.getMax() - volume.getMin())));
                colour = Math.max(colour, 0);
                image_writer.setColor(i, j, Color.color(colour, colour, colour, 1.0));
            } // column loop
        } // row loop
    }

    /**
     * Performs volume rendering.
     *
     * @param image The image to write to.
     * @param view  The direction of the volume.
     */
    public WritableImage volumeRender(WritableImage image, String view, String transferFunction) {
        //width of the image to write to
        int width = (int) image.getWidth();
        //Height of the image to write to
        int height = (int) image.getHeight();
        //image to write to
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter writer = writableImage.getPixelWriter();
        //if the view is from the top depth = 113, other views = 256
        int depth = (view.equals("top")) ? volume.getCT_z_axis() : volume.getCT_x_axis();

        IntStream.range(1, height - 1).parallel().forEach(j -> {
            IntStream.range(1, width - 1).parallel().forEach(i -> {
                //opacity accumulator
                double alphaAccum = 1;
                //red shade accumulator
                double redAccum = 0;
                //green shade accumulator
                double greenAccum = 0;
                //blue shade accumulator
                double blueAccum = 0;
                //variable to check the light
                boolean done = false;
                //light
                double light = 1;

                for (int k = 1; k < depth - 1 && !done; k++) {
                    short currentVoxel = getVoxel(view, i, j, k);
                    if (currentVoxel >= threshold && isGradient) {
                        light = getDiffuseLighting(view, i, j, k, currentVoxel, height, width, depth);
                        done = true;
                    }
                    //if user chose not to apply gradient shading or if gradient shading has completed
                    if (!isGradient || done) {
                        final int RED = 0;
                        final int GREEN = 1;
                        final int BLUE = 2;
                        double gradientMagnitude = 0.0;
//                        if(tfColor) {
                            gradientMagnitude = getSurfaceNormal(view, i, j, k, height, width, depth).getLength();
//                        } else{
//                            gradientMagnitude = getExactGradient(view, currentVoxel, i, j, k, height, width, depth);
//                        }
                        double secondDerivative = getSecondOrderDerivative(view, i, j, k);
                        double[] colour = getTransferFunction(currentVoxel, transferFunction, gradientMagnitude, secondDerivative);
                        double sigma = colour[3];
                        redAccum = Math.min(Math.max(redAccum + (alphaAccum * sigma * light * colour[RED]), 0), 1);
                        greenAccum = Math.min(Math.max(greenAccum + (alphaAccum * sigma * light * colour[GREEN]), 0), 1);
                        blueAccum = Math.min(Math.max(blueAccum + (alphaAccum * sigma * light * colour[BLUE]), 0), 1);
                        alphaAccum = alphaAccum * (1 - sigma);
                    }
                }
                writer.setColor(i, j, Color.color(redAccum, greenAccum, blueAccum, 1));
            });
        });
        return writableImage;
    }

    /**
     * Calculates and returns an estimate of the gradient magnitude at the specified position, in which the z
     * axis is a non-integer position.
     * @param view direction of the volume.
     * @param currentVoxel voxel at a current position in the volume
     * @param x The x-axis location of the pixel.
     * @param y The y-axis location of the pixel.
     * @param z The z-axis or ray depth location of the pixel.
     * @param height The height of the image.
     * @param width The width of the image.
     * @param depth Maximum depth of the ray.
     * @return a more accurate gradient magnitude.
     */
    public double getExactGradient(String view, short currentVoxel, int x, int y, int z, int height, int width, int depth) {
        double gradientMag = 0;
        Vector surfaceNormal = getSurfaceNormal(view, x, y, z, height, width, depth);
        if (z > 0) {
            int prevRay = z - 1;
            short prevVoxel = getVoxel(view, x, y, prevRay);
            if (prevVoxel != currentVoxel) {
                double exactZ = linearInterpolationPosition(view, threshold, prevVoxel, currentVoxel, prevRay, z);
                surfaceNormal = getSurfaceNormal(view, x, y, exactZ, height, width, depth);
                gradientMag = surfaceNormal.getLength();
            }
        }


        return gradientMag;
    }

    /**
     * Calculates the frequently used second order derivative operator - Laplacian operator
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     * @param x The x axis location of the pixel.
     * @param y The y axis location of the pixel.
     * @param z The z-axis or ray depth location of the pixel.
     * @return the laplacian operator for the specified position.
     */
    public double getSecondOrderDerivative(String view, int x, int y, int z) {
        double laplacianX = getVoxel(view, x, y, z - 1) - 2 * getVoxel(view, x, y, z) + getVoxel(view, x, y, z + 1);
        double laplacianY = getVoxel(view, x, y - 1, z) - 2 * getVoxel(view, x, y, z) + getVoxel(view, x, y + 1, z);
        double laplacianZ = getVoxel(view, x - 1, y, z) - 2 * getVoxel(view, x, y, z) + getVoxel(view, x + 1, y, z);
        return laplacianX + laplacianY + laplacianZ;
    }

    /**
     * Calculates the diffuse lighting/shading of a pixel.
     *
     * @param x            The x axis location of the pixel.
     * @param y            The y axis location of the pixel.
     * @param z            The z or ray depth location of the pixel.
     * @param currentVoxel The voxel at the current position.
     * @param height       The height of the image.
     * @param width        The width of the image.
     * @param depth        Maximum depth of the ray.
     * @return The lighting value for the specified pixel.
     */
    public double getDiffuseLighting(String view, int x, int y, int z, int currentVoxel, int height, int width, int depth) {
        Vector surfaceNormal = getSurfaceNormal(view, x, y, z, height, width, depth);
        Vector intersection = new Vector(x, y, z);
        if (isGradientInterpolation && z > 0) {
            int prevRay = z - 1;
            short prevVoxel = getVoxel(view, x, y, prevRay);
            double exactZ =
                    linearInterpolationPosition(view, threshold, prevVoxel, currentVoxel, prevRay, z);
            surfaceNormal = getSurfaceNormal(view, x, y, exactZ, height, width, depth);
            intersection.setC(exactZ);
        }

        final double LIGHT_SOURCE_Y = (double) volume.getCT_z_axis() / 4;
        final double LIGHT_SOURCE_Z = volume.getCT_x_axis();
        Vector lightSourcePosition = new Vector(lightX, LIGHT_SOURCE_Y, LIGHT_SOURCE_Z);
        Vector lightDirection = lightSourcePosition.subtract(intersection);
        lightDirection.normalize();
        surfaceNormal.normalize();
        return Math.max(0, surfaceNormal.dotProduct(lightDirection));
    }

    /**
     * Calculates and returns an estimate of the gradient/slope at the specified position, in which the z
     * axis is an integer. This calculation uses both central, forward and backward differance.
     *
     * @param view    The direction to view the scan/dataset from. i.e front, side or top.
     * @param current The voxel at the position to find the slope for.
     * @param x1      The x axis position of a voxel before the current.
     * @param y1      The y axis position of a voxel before the current.
     * @param z1      The integer z axis position of a voxel before the current.
     * @param x2      The x axis position of a voxel after the current.
     * @param y2      The y axis position of a voxel after the current.
     * @param z2      The integer z axis position of a voxel after the current.
     * @param min     The minimum value a altered axis could be. (Normally 0).
     * @param max     The maximum value a altered axis could be. (Normally axis length - 1)
     * @param i       The axis you are altering.
     * @return The gradient calculated for the specified position.
     */
    public double getGradient(String view, double current, int x1, int y1, int z1, int x2,
                              int y2, int z2, int min, int max, int i) {
        if (i > min && i < (max - 1)) {
            double prev = getVoxel(view, x1, y1, z1);
            double next = getVoxel(view, x2, y2, z2);
            return next - prev;
        } else if (i <= min) {
            double next = getVoxel(view, x2, y2, z2);
            return next - current;
        } else {
            double prev = getVoxel(view, x1, y1, z1);
            return current - prev;
        }
    }

    /**
     * Calculates and returns an estimate of the gradient/slope at the specified position, in which the z
     * axis is a non integer position. This calculation uses both central, forward and backward differance.
     *
     * @param view    The direction to view the scan/dataset from. i.e front, side or top.
     * @param current The voxel at the position to find the slope for.
     * @param x1      The x axis position of a voxel before the current.
     * @param y1      The y axis position of a voxel before the current.
     * @param z1      The non-integer z axis position of a voxel before the current.
     * @param x2      The x axis position of a voxel after the current.
     * @param y2      The y axis position of a voxel after the current.
     * @param z2      The non-integer z axis position of a voxel after the current.
     * @param min     The minimum value a altered axis could be. (Normally 0).
     * @param max     The maximum value a altered axis could be. (Normally axis length - 1)
     * @param i       The axis you are altering.
     * @return The gradient calculated for the specified position.
     */
    public double getGradient(String view, double current, int x1, int y1, double z1, int x2,
                              int y2, double z2, int min, int max, double i) {
        if (i > min && i < (max - 1)) {
            double prev = getRealVoxel(view, x1, y1, z1);
            double next = getRealVoxel(view, x2, y2, z2);
            return next - prev;
        } else if (i <= min) {
            double next = getRealVoxel(view, x2, y2, z2);
            return next - current;
        } else {
            double prev = getRealVoxel(view, x1, y1, z1);
            return current - prev;
        }
    }


    /**
     * Calculates the surface normal for the current voxel at all integer positions.
     *
     * @param view   The scan direction. i.e top, front or side
     * @param x      The x location of voxel.
     * @param y      The y location of voxel.
     * @param z      The z/ray location of voxel.
     * @param height The height of the image.
     * @param width  The width of the image.
     * @param depth  Maximum depth of the ray.
     * @return The surface normal of the specified voxel
     */
    public Vector getSurfaceNormal(String view, int x, int y, int z, int height, int width, int depth) {
        double xGradient, yGradient, zGradient;
        short currentVoxel = getVoxel(view, x, y, z);
        xGradient = getGradient(view, currentVoxel, x - 1, y, z, x + 1, y, z, 0, (width - 1), x);
        yGradient = getGradient(view, currentVoxel, x, y - 1, z, x, y + 1, z, 0, height - 1, y);
        zGradient = getGradient(view, currentVoxel, x, y, z - 1, x, y, z + 1, 0, depth - 1, z);
        return new Vector(xGradient, yGradient, zGradient);
    }

    /**
     * Calculates the surface normal for the current voxel of a non integer position z.
     *
     * @param view   The scan direction. i.e top, front or side
     * @param x      The x location of voxel.
     * @param y      The y location of voxel.
     * @param z      The exact z/ray location of voxel.
     * @param height The height of the image.
     * @param width  The width of the image.
     * @param depth  Maximum depth of the ray.
     * @return The surface normal of the specified voxel
     */
    public Vector getSurfaceNormal(String view, int x, int y, double z, int height, int width, int depth) {
        double xGradient, yGradient, zGradient;
        double currentVoxel = getRealVoxel(view, x, y, z);
        xGradient = getGradient(view, currentVoxel, x - 1, y, z, x + 1, y, z, 0, width - 1, x);
        yGradient = getGradient(view, currentVoxel, x, y - 1, z, x, y + 1, z, 0, height - 1, y);
        zGradient = getGradient(view, currentVoxel, x, y, z - 1, x, y, z + 1, 1, depth - 2, z);
        return new Vector(xGradient, yGradient, zGradient);
    }


    /**
     * Gets the voxel at a non integer position z.
     *
     * @param view The direction viewing the ct image from.
     * @param x    The x value to get.
     * @param y    The y value to get.
     * @param z    The non integer z value to get.
     * @return Voxel at non-integer position.
     */
    public double getRealVoxel(String view, int x, int y, double z) {
        int z1 = (int) Math.floor(z);
        int z2 = (int) Math.ceil(z);
        short v1 = getVoxel(view, x, y, z1);
        short v2 = getVoxel(view, x, y, z2);
        return linearInterpolationVoxel(v1, v2, z1, z, z2);
    }

    /**
     * Calculates the voxel at a non-integer position along a single axis.
     *
     * @param v1 The voxel at the previous integer position.
     * @param v2 The voxel at the following integer position.
     * @param x1 The integer position before.
     * @param x  The non-integer position to find.
     * @param x2 The integer position after.
     * @return The voxel value at non integer position X.
     */
    public double linearInterpolationVoxel(double v1, double v2, double x1, double x, double x2) {
        return (v1 + (v2 - v1) * ((x - x1) / (x2 - x1)));
    }

    /**
     * Calculates the non-integer position of a specified value within two integer positions.
     *
     * @param v  The value to get the position of.
     * @param v1 The value at the previous integer position.
     * @param v2 The value at the following integer position.
     * @param x1 The previous integer position.
     * @param x2 The following integer position.
     * @return The non integer position of the specified value.
     */
    public double linearInterpolationPosition(String view, double v, double v1, double v2, int x1, int x2) {
        int boundary = 113;
        if (view.equals("top")) {
            boundary = 112;
        } else {
            boundary = 255;
        }
        double exactZ = x1 + (x2 - x1) * ((v - v1) / (v2 - v1));
        return exactZ > 0 && exactZ < boundary ? exactZ : 0;
    }

    /**
     * The default transfer function for the default dataset. Calculates pixel colour from a
     * voxel.
     *
     * @param voxel The voxel to get RGB value for.
     * @return The RGB and opacity value for the pixel.
     */
    private double[] transferFunction(short voxel) {
        double R, G, B, O;
        if ((voxel > -299) && (voxel < 50)) {
            R = 1.0;
            G = 0.79;
            B = 0.6;
            O = opacity;
        } else if (voxel > 300) {
            R = 1;
            G = 1;
            B = 1;
            O = 0.8;
        } else {
            R = 0;
            G = 0;
            B = 0;
            O = 0;
        }
        return new double[]{R, G, B, O};
    }

    /**
     * Implements Levoy's tent function.
     * @param voxel voxel to find the RGBA values for
     * @return RGBA values for the specified voxel
     */
    public double[] transferFunctionTent(short voxel, double threshold) {
        double R, G, B, O;
        if ((voxel >= threshold) &&
                (voxel < (threshold + levWidth))) {
            O = ((float) voxel - threshold) / levWidth;
        } else if (voxel < threshold) {
            O = 0.0;
        } else {
            O = opacity;
        }
            R = 1;
            G = 1;
            B = 1;
        return new double[]{R, G, B, O};
    }

    /**
     * Classify intensities in the color lookup table
     * @param index entry for the lookup table
     * @param gradientMag gradient magnitude
     * @return classified lookup table
     */
    public ColorComposer colorClassifier(int index, double gradientMag) {
        double nrMagnitudeBits = 7347825;
        int nrIntensityBits = 3365;
        int maskMagnitude = (int) Math.pow(2, nrMagnitudeBits) - 1;
        float maxMagnitude = (float) 5828.351;
        float fractionMagnitude = (float) Math.pow(2, nrMagnitudeBits) / maxMagnitude;

        // Code gradient magnitude into int.
        int igradient = (int) (gradientMag * fractionMagnitude) & maskMagnitude;
        // Fit gradient magnitude and intensity into 16 bits.
        int entry = (igradient << nrIntensityBits) | index;
        return new ColorComposer(1.0,
                (lut[index * 3 + 0] & 0xff),
                (lut[index * 3 + 1] & 0xff),
                (lut[index * 3 + 2] & 0xff));
    }

    /**
     * 2-dimensional Intensity - Gradient Magnitude alpha color transfer function
     * @param voxel The voxel to get RGBA value for.
     * @param gradientMagnitude gradient magnitude at a current position.
     * @return RGBA values for a specified pixel.
     */
    public double[] transferFunction2D(short voxel, double gradientMagnitude, double threshold) {
        double R = 0, G = 0, B = 0, O;
        //levWidth = 200;
        if (gradientMagnitude == 0 && voxel == threshold) {
            O = 1.0;

        } else if (gradientMagnitude > 0 && voxel >= (threshold - levWidth * gradientMagnitude) && voxel <= (threshold + levWidth * gradientMagnitude)) {
            O = (1 - (1 / levWidth) * Math.abs((threshold - voxel) / gradientMagnitude));
            R = 1;
            B = 1;
            G = 1;
        } else {
            O = 0.0;
        }
        //if color transfer function should be applied

//        if (tfColor) {
//            try {
//                R = colorClassifier(Math.abs(voxel / 10), gradientMagnitude).getRed();
//                B = colorClassifier(Math.abs(voxel / 10), gradientMagnitude).getBlue();
//                G = colorClassifier(Math.abs(voxel / 10), gradientMagnitude).getGreen();
//            } catch (Exception e) {
//                System.out.println("Error");
//            }
//        } else {
//            return hounsefieldColorTF(voxel, O);
//        }
        return new double[]{R, G, B, O};
    }

    /**
     * 1-dimensional color transfer function derived from the Hounsefield scale
     * @param voxel The voxel to get RGB value for.
     * @param opacity Opacity of the specific voxel derived from the alpha function
     * @return RGB values for the pixel.
     */
    public double[] hounsefieldColorTF(short voxel, double opacity) {
        double R = 0, G = 0, B = 0;

        if ((voxel > -299) && (voxel < 50)) {
            R = 1.0;
            G = 0.79;
            B = 0.6;
        } else if ((voxel > 49) && (voxel < 301)) {
            R = 1;
            G = 1;
            B = 1;
        } else if (voxel > 300) {
            R = 1;
            G = 1;
            B = 1;
        } else {
            R = 0;
            G = 0;
            B = 0;
        }
        double[] colors = {R, G, B, opacity};
        return colors;
    }

    /**
     * Three-dimensional transfer function derived from Levoy and Kniss et al. papers
     * the 2nd dimension is the gradient magnitude
     * the 3rd dimension is the second derivative - the laplacian operator
     * @param voxel The voxel to get alpha value for.
     * @param gradientMag The gradient magnitude of the specified voxel
     * @param secondDerivative the second derivative for the specified voxel
     * @return alpha value for the pixel.
     */
    public double[] transferFunction3D(short voxel, double gradientMag, double secondDerivative) {
        double R = 0, G = 0, B = 0, O;
        voxel = (short) (voxel+ secondDerivative);
        if (gradientMag == 0 && voxel == threshold) { //if 0 change detected at the threshold
            O= 0.0;
        } else if (gradientMag > 0 && voxel >= (threshold - levWidth * gradientMag) && voxel <= (threshold + levWidth * gradientMag)) { //on the boundaries of the threshold
            O = (1.0 -  (Math.abs((threshold - voxel) / gradientMag)));
            R = 1;
            B = 0.2;
            G = 0.2;
        } else if(( voxel <= -800 || voxel >= 1000) && (!isGradient)) {
            O = 0.0;
        }else{
            if (isGradient) {
                O = opacity;
            }else{
                O = opacity/10;
            }
            R = 1;
            B = 1;
            G = 1;
        }
        if(!tfColor){
            R = 1;
            B = 1;
            G = 1;
        }
        return new double[]{R, G, B, O};
    }


    /**
     * Handles which transfer function to use based on the name passed in.
     *
     * @param voxel  The voxel to use in the transfer function.
     * @param tfName The name of the transfer function to use.
     * @return The result from specified TF.
     */
    private double[] getTransferFunction(short voxel, String tfName, double dfxi, double secondDerivative) {
        if (tfName.equals("TF1")) {
            return transferFunctionTent(voxel, threshold);
        } else if (tfName.equals("TF2D")) {
            return transferFunction2D(voxel, dfxi, threshold);
        } else {
            return transferFunction3D(voxel, dfxi, secondDerivative);
        }
    }


    /**
     * Sets the opacity of the skin for the transfer function.
     *
     * @param opacity The opacity of the skin.
     */
    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    /**
     * Sets if color transfer function should be used.
     */
    public void changeColor() {
        tfColor = !this.tfColor;
    }

    /**
     * Sets the threshold for the transfer function
     * @param threshold threshold of the function
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;

    }

    /**
     * Sets if gradient shading should be used.
     *
     * @param isGradient If gradient shading should be used.
     */
    public void setGradientShading(boolean isGradient) {
        this.isGradient = isGradient;
    }

    /**
     * Gets if gradient shading should be used.
     *
     * @return If gradient shading should be used.
     */
    public boolean getGradientShading() {
        return this.isGradient;
    }

    /**
     * Sets if gradient shading interpolation should be used.
     *
     * @param isGradientInterpolation If interpolation should be used.
     */
    public void setGradientInterpolation(boolean isGradientInterpolation) {
        this.isGradientInterpolation = isGradientInterpolation;
    }

    /**
     * Gets if gradient shading should be used.
     *
     * @return If gradient shading should be used.
     */
    public boolean getGradientInterpolation() {
        return this.isGradientInterpolation;
    }

    /**
     * Gets the width of the top image.
     *
     * @return The width of the top image.
     */
    public int getTop_width() {
        return TOP_WIDTH;
    }

    /**
     * Gets the height of the top image.
     *
     * @return The height of the top image.
     */
    public int getTop_height() {
        return TOP_HEIGHT;
    }

    /**
     * Gets the width of the front image.
     *
     * @return The width of the front image.
     */
    public int getFront_width() {
        return FRONT_WIDTH;
    }

    /**
     * Gets the height of the front image.
     *
     * @return The height of the front image.
     */
    public int getFront_height() {
        return FRONT_HEIGHT;
    }

    /**
     * Gets the width of the side image.
     *
     * @return The width of the side image.
     */
    public int getSide_width() {
        return SIDE_WIDTH;
    }

    /**
     * Gets the height of the side image.
     *
     * @return The height of the side image.
     */
    public int getSide_height() {
        return SIDE_HEIGHT;
    }

    /**
     * Sets the location of the light source along the X axis.
     *
     * @param position The location along the X axis.
     */
    public void setLightX(int position) {
        this.lightX = position;
    }

    public void setWidth(double newValue) {
        levWidth = newValue/50;
    }
}
