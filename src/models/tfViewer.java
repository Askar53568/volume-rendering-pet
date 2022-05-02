package models;

import java.io.FileInputStream;

import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.io.*;
import java.lang.Math;



import static java.lang.Math.sqrt;

//the correct file to submit
public class tfViewer {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    int CT_x_axis = 256;
    int CT_y_axis = 256;
    int CT_z_axis = 113;
    RGBClassifier levoyClass = new RGBClassifier();
    byte [] lut = levoyClass.defaultLUT();
    float [] opacityTable;
    int nrMagnitudeBits = 7347825;
    int nrIntensityBits = 3365;
    int maskMagnitude = (int) Math.pow(2, nrMagnitudeBits) - 1;
    int maxIntensity = (int) Math.pow(2, nrIntensityBits);
    int  maskIntensity = (int) Math.pow(2, nrIntensityBits) - 1;
    float maxMagnitude = (float) 5828.351;
    float fractionMagnitude = (float) Math.pow(2, nrMagnitudeBits) / maxMagnitude;
    private boolean color;
    private double angle;
    private Volume v = new Volume(256,256,113);
    private VolumeRender volumeRender = new VolumeRender(v);

    public tfViewer(){
        short cthead[][][]; //store the 3D volume data set
        short min, max; //min/max value in the 3D volume data set
        int CT_x_axis = 256;
        int CT_y_axis = 256;
        int CT_z_axis = 113;
        RGBClassifier levoyClass = new RGBClassifier();
        byte [] lut = levoyClass.defaultLUT();
        //byte [] lut = defaultLUT();
        float [] opacityTable;
        int nrMagnitudeBits = 7347825;
        int nrIntensityBits = 3365;
        int maskMagnitude = (int) Math.pow(2, nrMagnitudeBits) - 1;
        int maxIntensity = (int) Math.pow(2, nrIntensityBits);
        int  maskIntensity = (int) Math.pow(2, nrIntensityBits) - 1;
        float maxMagnitude = (float) 5828.351;
        float fractionMagnitude = (float) Math.pow(2, nrMagnitudeBits) / maxMagnitude;
        boolean color = false;
    }
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }


    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        File file = new File("C:\\Users\\Asus\\IdeaProjects\\volume-rendering-master\\src\\data\\CThead_256_256_113");
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min = Short.MAX_VALUE;
        max = Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[CT_z_axis][CT_y_axis][CT_x_axis]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k = 0; k < CT_z_axis; k++) {
            for (j = 0; j < CT_y_axis; j++) {
                for (i = 0; i < CT_x_axis; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read = (short) ((b2 << 8) | b1); //and swizzle the bytes around
                    //if (read<min) min=read; //update the minimum
                    if (read < min) {
                        min = read;
                        //System.out.println(min+ " minVector x: " + minVector.getX() +" y: "+minVector.getY()+" z: "+ minVector.getZ());
                    }
                    //if (read>max) max=read; //update the maximum
                    if (read > max) {
                        max = read;
                        //System.out.println(max+ " maxVector x: " + maxVector.getX() +" y: "+maxVector.getY()+" z: "+ maxVector.getZ());
                    }
                    cthead[k][j][i] = read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        //System.out.println(min + " " + max); //diagnostic - for CThead this should be -1117, 2248

    }

    public double[][] transformMatrix(double radians) {
        double[][] transform = new double[3][3];
        // Build transform matrix.
        for (int j=0; j<3; j++) {
            for (int i=0; i<3; i++) {
                transform[j][i] = 0;
                if (j == 0 && i == 0) {
                    transform[j][i] = Math.cos(radians);
                } else if (j == 0 && i == 1) {
                    transform[j][i] = -1 * Math.sin(radians);
                } else if (j == 1 && i == 0) {
                    transform[j][i] = Math.sin(radians);
                } else if (j == 1 && i == 1) {
                    transform[j][i] = Math.cos(radians);
                } else if (j == 2 && i == 2) {
                    transform[j][i] = 1;
                }
            }
        }
        return transform;
    }

    public short[][][] rotateData(double angle) {
        try {
            ReadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Make a larger size as 45 degrees takes up the most space.
        int largeX = (CT_x_axis-1)*2+1;
        int largeY = (CT_y_axis-1)*2+1;
        int largeZ = (CT_z_axis-1)*2+1;
        short[][][] newData = new short[largeZ][largeY][largeX];
        int xShift = 0;
        int yShift = 0;
        double radians = Math.toRadians(angle);
        System.out.println("Angle of rotation: " + angle);
        if (angle >= 0 && angle <= 90) {
            xShift = CT_x_axis - 1;
            yShift = 0;
        } else if (angle > 90 && angle <= 180) {
            xShift = 360;
            yShift = CT_y_axis - 1;
        } else if (angle > 180 && angle <= 270) {
            xShift = CT_x_axis - 1;
            yShift = 360;
        } else if (angle > 270 && angle <= 360) {
            xShift = 0;
            yShift = CT_y_axis - 1;
        }

        // Initialise all values of newData with -999.
        for (int k=0; k<CT_z_axis; k++) {
            for (int j=0; j<largeY; j++) {
                for (int i=0; i<largeX; i++) {
                    newData[k][j][i] = -999;
                }
            }
        }

        // Build transform matrix.
        double[][] transform = transformMatrix(radians);

        // Shift each voxel to its transformed index.
        for (int k=0; k<CT_z_axis; k++){
            for (int j=0; j<CT_y_axis; j++) {
                for (int i=0; i<CT_x_axis; i++) {
                    // Result of dot product.
                    int zIndex = 0;
                    int yIndex = 0;
                    int xIndex = 0;
                    int[] coordinates = new int[]{i, j, k};
                    // Product of transform matrix and coordinates matrix.
                    for (int a=0; a<3; a++) {
                        if (a == 0) {
                            for (int b = 0; b < 3; b++) {
                                xIndex += coordinates[b] * transform[a][b];
                            }
                        } else if (a == 1) {
                            for (int b = 0; b < 3; b++) {
                                yIndex += coordinates[b] * transform[a][b];
                            }
                        } else {
                            for (int b = 0; b < 3; b++) {
                                zIndex += coordinates[b] * transform[a][b];
                            }
                        }
                    }
                    //System.out.println(i + " " + j + " " + k);
                    //System.out.println("New: " + xIndex + " " + yIndex + " " + zIndex);
                    // Map transformed index to data at original index.
                    newData[zIndex][yIndex + yShift][xIndex + xShift] = cthead[k][j][i];
                }
            }
        }
        return newData;
    }

    // Volume rendering for front back.
    public void FrontRotate(WritableImage image, double angle, double threshold) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        short datum;
        Double[][][] colours = initializeColours(h, w);

        short[][][] newData = rotateData(angle);

        for (int k=1; k<(CT_y_axis - 1)*2 - 1; k++){
            for (int j=1; j<h-1; j++) {
                for (int i=1; i<w-1; i++) {
                    datum = newData[j][k][i];
                    double dfxi = gradientMagnitude(newData, j,k,i);
                    double[] rgbValues = volumeRender.transferFunction2D(datum, dfxi, threshold);
                    // [r, g, b, opacity]
                    if (rgbValues[3] == 1) {
                        break;
                    }
                    for (int a = 0; a < 3; a ++) {
                        // color * accumulated transparency * opacity
                        colours[j][i][a] += (rgbValues[a] * colours[j][i][3] * rgbValues[3]);
                    }
                    // Set accumulated transparency.
                    colours[j][i][3] *= (1 - rgbValues[3]);
                }
            }
        }
        setImageWriter(image_writer, h, w, colours);
    }

    // Volume rendering for top down.
    public void TopRotate(WritableImage image, double angle) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        short datum;
        Double[][][] colours = initializeColours(h, w);

        short[][][] newData = rotateData(angle);

        for (int k=0; k<CT_z_axis; k++){
            for (int j=0; j<h; j++) {
                for (int i=0; i<w; i++) {
                    datum = newData[k][j][i];
                    double[] rgbValues = volumeRender.hounsefieldColorTF(datum,0.12);
                    // [r, g, b, opacity]
                    if (rgbValues[3] == 1) {
                        break;
                    }
                    for (int a = 0; a < 3; a ++) {
                        // color * accumulated transparency * opacity
                        colours[j][i][a] += (rgbValues[a] * colours[j][i][3] * rgbValues[3]);
                    }
                    // Set accumulated transparency.
                    colours[j][i][3] *= (1 - rgbValues[3]);
                }
            }
        }
        setImageWriter(image_writer, h, w, colours);
    }

    // Volume rendering for left right.
    public void SideRotate(WritableImage image, double angle) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        short datum;
        Double[][][] colours = initializeColours(h, w);

        short[][][] newData = rotateData(angle);

        for (int k=0; k<(CT_x_axis - 1) * 2; k++){
            for (int j=0; j<h; j++) {
                for (int i=0; i<w; i++) {
                    datum = newData[j][i][k];
                    double[] rgbValues = volumeRender.hounsefieldColorTF(datum, 0.12);
                    // [r, g, b, opacity]
                    if (rgbValues[3] == 1) {
                        break;
                    }
                    for (int a = 0; a < 3; a ++) {
                        // color * accumulated transparency * opacity
                        colours[j][i][a] += (rgbValues[a] * colours[j][i][3] * rgbValues[3]);
                    }
                    // Set accumulated transparency.
                    colours[j][i][3] *= (1 - rgbValues[3]);
                }
            }
        }
        setImageWriter(image_writer, h, w, colours);
    }

    public Double[][][] initializeColours(int h, int w) {
        Double[][][] colours = new Double[h][w][4];
        // Initialize array with [0, 0, 0, 1].
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                for (int a = 0; a < 3; a ++) {
                    colours[j][i][a] = 0.0;
                }
                colours[j][i][3] = 1.0;
            }
        }

        return colours;
    }

    public void setImageWriter(PixelWriter image_writer, int h, int w, Double[][][] colours) {
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                image_writer.setColor(i, j, Color.color(
                        Math.min(1.0, colours[j][i][0]),
                        Math.min(1.0, colours[j][i][1]),
                        Math.min(1.0, colours[j][i][2]),
                        1.0
                ));
            }
        }
    }

    public void resizePopUp(WritableImage image, int scaling) {
        Stage window = new Stage();
        ImageView resizedImage = new ImageView(biLinear(image, scaling));
        window.setTitle("Resized with Bilinear");
        window.setTitle("Resized");
        FlowPane root = new FlowPane();
        root.getChildren().addAll(resizedImage);
        Scene sizeScene = new Scene(root, resizedImage.getFitWidth() - 1, resizedImage.getFitHeight() - 1);// to remove the extra pixel on the right and bottom
        window.setScene(sizeScene);
        window.show();
    }

    public float gradientMagnitude(short[][][] volume, int k, int j, int i) {

        float I = (float) Math.pow((volume[k][j][i + 1] - volume[k][j][i - 1]), 2);
        float J = (float) Math.pow((volume[k][j + 1][i] - volume[k][j - 1][i]), 2);
        //Dx
        float K = (float) Math.pow((volume[k + 1][j][i] - volume[k - 1][j][i]), 2);
        float gradientM = (float) (sqrt(I + J + K));
        return gradientM;
    }
    public int secondDerivative(int k, int j, int i){
        int I1 = (cthead[k][j][i-1]-2*cthead[k][j][i]+cthead[k][j][i + 1]);
        int J1 = (cthead[k][j-1][i]-2*cthead[k][j][i]+cthead[k][j + 1][i]);
        int K1 = (cthead[k-1][j][i]-2*cthead[k][j][i]+cthead[k + 1][j][i]);

        int laplacian = I1 + J1 + K1;
        return laplacian;
    }
    public void changeColor(){
        color = !this.color;
    }
    public boolean getColor(){
        return color;
    }

    public ColorComposer alphacolor(int index, double gradientMag)
    {
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
                (lut[index*3+0]&0xff),
                (lut[index*3+1]&0xff),
                (lut[index*3+2]&0xff));
    }

    public void opacityComputeSideNoReturn(WritableImage image, double threshold) throws IOException {
        ReadData();
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int k = 1; k < h - 1; k++) {
            for (int j = 1; j < w - 1; j++) {
                double width = 2.0;
                double transparency = 1;
                double red = 0;
                double blue = 0;
                double green = 0;
                double opacity = 0;
                double R = 1.0;
                double B = 1.0;
                double G = 1.0;
                image_writer.setColor(j, k, Color.color(0, 0, 0, 1.0));
                for (int i = 1; i < 254; i++) {
                    int intensity = cthead[k][j][i];
                    double dfxi = gradientMagnitude(cthead, k, j, i);
                    if (dfxi == 0 && intensity == threshold) {
                        opacity = 1.0;
                    } else if (dfxi > 0 &&
                            intensity <= (threshold + width * dfxi) &&
                            intensity >= (threshold - width * dfxi)) {
                        opacity = 1 - (1 / width) * Math.abs((threshold - intensity) / dfxi);
                    } else {
                        opacity = 0.0;
                    }
                    if (color) {
                        R = alphacolor(Math.abs(intensity/10),dfxi).getRed();
                        B = alphacolor(Math.abs(intensity/10),dfxi).getBlue();
                        G = alphacolor(Math.abs(intensity/10),dfxi).getGreen();
                    }
                    red = clamp((red + (transparency * opacity * 1.0 * R)), 0.0, 1.0);
                    blue = clamp((blue + (transparency * opacity * 1.0 * B)), 0.0, 1.0);
                    green = clamp((green + (transparency * opacity * 1.0 * G)), 0.0, 1.0);
                    transparency = transparency * (1 - opacity);
                }
                image_writer.setColor(j, k, Color.color(red, blue, green, 1));
            }
        }
    }

    public void laplacianSide(WritableImage image, double threshold) throws IOException {
        ReadData();
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int k = 1; k < h - 1; k++) {
            for (int j = 1; j < w - 1; j++) {
                double width = 2.0;
                double transparency = 1;
                double red = 0;
                double blue = 0;
                double green = 0;
                double opacity = 0;
                double R = 1.0;
                double B = 1.0;
                double G = 1.0;
                image_writer.setColor(j, k, Color.color(0, 0, 0, 1.0));
                for (int i = 1; i < 254; i++) {
                    int intensity = cthead[k][j][i];
                    int laplacian = secondDerivative(k, j, i);
                    double dfxi = gradientMagnitude(cthead, k, j, i);
                    if (dfxi == 0 && laplacian == threshold) {
                        opacity = 1.0;
                    } else if (dfxi > 0 &&
                            laplacian <= (threshold + width * dfxi) &&
                            laplacian >= (threshold - width * dfxi)) {
                        opacity = 1 - (1 / width) * Math.abs(((threshold - laplacian) / dfxi));
                    } else {
                        opacity = 0.0;
                    }
                    if (color) {
                        R = alphacolor(Math.abs(intensity/10),dfxi).getRed();
                        B = alphacolor(Math.abs(intensity/10),dfxi).getBlue();
                        G = alphacolor(Math.abs(intensity/10),dfxi).getGreen();
                    }
                    red = clamp((red + (transparency * opacity * 1.0 * R)), 0.0, 1.0);
                    blue = clamp((blue + (transparency * opacity * 1.0 * B)), 0.0, 1.0);
                    green = clamp((green + (transparency * opacity * 1.0 * G)), 0.0, 1.0);
                    transparency = transparency * (1 - opacity);
                }
                image_writer.setColor(j, k, Color.color(red, blue, green, 1));

            }
        }
    }
    public void opacityComputeFront(WritableImage image, double threshold) throws IOException {
        ReadData();
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int k = 1; k < h - 1; k++) {
            for (int i = 1; i < w - 1; i++) {
                double width = 2.0;
                double transparency = 1;
                double red = 0;
                double blue = 0;
                double green = 0;
                double opacity = 0;
                double R = 1.0;
                double B = 1.0;
                double G = 1.0;
                double colorDefault = 3.0;
                image_writer.setColor(i, k, Color.color(0, 0, 0, 1.0));
                for (int j = 1; j < 254; j++) {
                    int intensity = cthead[k][j][i];
                    double gradient = gradientMagnitude(cthead, k, j, i);
                    if (gradient == 0 && intensity == threshold) {
                        opacity = 1.0;
                    } else if (gradient > 0 &&
                            intensity <= (threshold + width * gradient) &&
                            intensity >= (threshold - width * gradient)) {
                        opacity = (1 - (1 / width) * Math.abs((threshold - intensity) / gradient));

                    } else {
                        opacity = 0.0;
                    }
                    if (color) {
                        R = alphacolor(Math.abs(intensity / 10), gradient).getRed();
                        B = alphacolor(Math.abs(intensity / 10), gradient).getBlue();
                        G = alphacolor(Math.abs(intensity / 10), gradient).getGreen();
                    }

                    red = clamp((red + (transparency * opacity * 1.0 * R)), 0.0, 1.0);
                    blue = clamp((blue + (transparency * opacity * 1.0 * B)), 0.0, 1.0);
                    green = clamp((green + (transparency * opacity * 1.0 * G)), 0.0, 1.0);
                    transparency = transparency * (1 - opacity);
                }
                image_writer.setColor(i, k, Color.color(red, blue, green, 1));
            }
        }
    }

    public void opacityComputeTop(WritableImage image, double threshold) throws IOException {
        ReadData();
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        for (int j = 1; j < h-1; j++) {
            for (int i = 1; i < w-1; i++) {
                double width = 2.0;
                double transparency = 1;
                double red = 0;
                double blue = 0;
                double green = 0;
                double opacity = 0;
                double R = 1.0;
                double B = 1.0;
                double G = 1.0;
                image_writer.setColor(i, j, Color.color(0, 0, 0, 1.0));
                for (int k = 1; k < 112; k++) {
                    int intensity = cthead[k][j][i];
                    double dfxi = gradientMagnitude(cthead, k, j, i);
                    if (dfxi == 0 && intensity == threshold) {
                        opacity = 1.0;
                    } else if (dfxi > 0 &&
                            intensity <= (threshold + width * dfxi) &&
                            intensity >= (threshold - width * dfxi)) {
                        opacity = (1 - (1 / width) * Math.abs((threshold - intensity) / dfxi));

                    } else {
                        opacity = 0.0;
                    }
                    if(color) {
                        R = alphacolor(Math.abs(intensity / 10), dfxi).getRed();
                        B = alphacolor(Math.abs(intensity / 10), dfxi).getBlue();
                        G = alphacolor(Math.abs(intensity / 10), dfxi).getGreen();
                    }
                    red = clamp((red + (transparency * opacity * 1.0 * R)), 0.0, 1.0);
                    blue = clamp((blue + (transparency * opacity * 1.0 * B)), 0.0, 1.0);
                    green = clamp((green + (transparency * opacity * 1.0 * G)), 0.0, 1.0);
                    transparency = transparency * (1 - opacity);
                }
                image_writer.setColor(i, j, Color.color(red, blue, green, 1));
            }
        }
    }



    public WritableImage biLinear (WritableImage image, double scaling){
        int finalHeight = (int) (image.getHeight() * scaling);
        int finalWidth = (int) (image.getWidth() * scaling);
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage newImage = new WritableImage(finalWidth,finalHeight); //new Writable image initialized for resized image
        PixelWriter writer = newImage.getPixelWriter();	//writing to newImage
        PixelReader reader = image.getPixelReader();	//Reading from original image

        for (int x=0; x< finalWidth; x++){
            // zero indexed real position in the src image
            double pixelX = (double) x / finalWidth * (width-1);
            for(int y=0; y<finalHeight; y++){
                double pixelY = (double)y / finalHeight * (height-1);

                int x1  = (int) Math.floor(pixelX);
                int y1 =(int) Math.floor(pixelY);
                double a = reader.getColor(x1,y1).getGreen();
                double b = reader.getColor(x1 + 1, y1).getGreen();
                double c = reader.getColor(x1,y1+1).getGreen();
                double d = reader.getColor(x1+1,y1+1).getGreen();

                double deltX = pixelX - x1;
                double deltY = pixelY - y1;

                double top = a * (1-deltX) + (b * deltX);
                double bottom = c * (1-deltX) +  (d * deltX);
                double finY = (top *(1-deltY) + (bottom * deltY)) ;

                writer.setColor(x,y,Color.color(finY,finY,finY,1));

            }
        }
        return newImage;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}

