package models;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class UselessCode {

//    public static double[] interpolateArray(double start, double end, int count) {
//        double[] array = new double[count + 1];
//        for (int i = 0; i <= count; ++i) {
//            array[i] = start + i * (end - start) / count;
//        }
//        return array;
//    }
//
//    public static double interpolateValue(double start, double end, int count) {
//        double val = 0;
//        for (int i = 0; i <= count; ++i) {
//            val = start + i * (end - start) / count;
//        }
//        return val;
//    }
//    public void classificationTest(WritableImage image) {
//        RGBControlPoint[] gradient = new RGBControlPoint[256];
//        RGBControlPoint white = new RGBControlPoint(1.0, 1.0, 1.0, 1.0, 0);
//        RGBControlPoint orange = new RGBControlPoint(1.0, 0.6, 0.79, 0.3, 256);
//        gradient = interpolateRGB(white, orange, 256);
//        for (RGBControlPoint color : gradient) {
//            System.out.println(color.getR() + " " + color.getG() + " " + color.getB() + " " + color.getAlpha() + " " + color.getIsovalue());
//        }
//        int w = (int) image.getWidth(), h = (int) image.getHeight();
//        PixelWriter image_writer = image.getPixelWriter();
//        for (int k = 1; k < h - 1; k++) {
//            for (int i = 1; i < w - 1; i++) {
//                float levThreshold = 1000.0F;
//                double transparency = 1;
//                double red = 0;
//                double blue = 0;
//                double green = 0;
//                double opacity;
//                image_writer.setColor(i, k, Color.color(0, 0, 0, 1.0));
//                for (int j = 1; j < 254; j++) {
//                    if (gradientMagnitude(k, j, i) >= levThreshold) {
//                        //opacity = 1.0;
//                        // Color = Color + (transparency * opacity * Light * SpecificColor)
//                        double testRed = gradient[j].getR();
//                        double testGreen = gradient[j].getG();
//                        double testBlue = gradient[j].getB();
//                        double testAlpha = gradient[j].getAlpha();
//                        opacity = testAlpha;
//                        red = clamp((red + (transparency * opacity * 1.0 * testRed)), 0.0, 1.0);
//                        blue = clamp((blue + (transparency * opacity * 1.0 * testGreen)), 0.0, 1.0);
//                        green = clamp((green + (transparency * opacity * 1.0 * testBlue)), 0.0, 1.0);
//                        transparency = transparency * (1 - opacity);
//                    } else {
//                        opacity = 0;
//                    }
//                }
//                image_writer.setColor(i, k, Color.color(red, blue, green, 1));
//            }
//        }
//
//    }
//public RGBControlPoint[] StupidGradient(){
//    RGBControlPoint[] gradient = new RGBControlPoint[256];
//    RGBControlPoint white = new RGBControlPoint(1.0, 1.0, 1.0, 1.0, 0);
//    RGBControlPoint orange = new RGBControlPoint(1.0, 0.6, 0.79, 0.3, 256);
//    RGBControlPoint darkOrange = new RGBControlPoint(0.7, 0.3, 0.49, 0.3, 256);
//    gradient = interpolateRGB256(orange, darkOrange);
//
//    for (RGBControlPoint color : gradient) {
//        System.out.println(color.getR() + " " + color.getG() + " " + color.getB() + " " + color.getAlpha() + " " + color.getIsovalue());
//    }
//    return gradient;
//}
//public void classificationSide(WritableImage image) {
//    RGBControlPoint[] gradient = new RGBControlPoint[256];
//    RGBControlPoint white = new RGBControlPoint(1.0, 1.0, 1.0, 1.0, 0);
//    RGBControlPoint orange = new RGBControlPoint(1.0, 0.6, 0.79, 0.1, 256);
//    gradient = interpolateRGB(white, orange, 256);
//
//    int w = (int) image.getWidth(), h = (int) image.getHeight();
//    PixelWriter image_writer = image.getPixelWriter();
//    for (int k = 1; k < h - 1; k++) {
//        for (int j = 1; j < w - 1; j++) {
//            float levThreshold = (float) 128.0;
//            float levWidth = (float) 2.0;
//            double transparency = 1;
//            double red = 0;
//            double blue = 0;
//            double green = 0;
//            double opacity = 0;
//            image_writer.setColor(j, k, Color.color(0, 0, 0, 1.0));
//            for (int i = 1; i < 254; i++) {
//                int intensity = cthead[k][j][i];
//                if ((intensity >= levThreshold) &&
//                        (intensity < (levThreshold + levWidth))) {
//                    opacity = ((float) intensity - levThreshold) / levWidth;
//                } else if (intensity < levThreshold) {
//                    opacity = 0.0;
//                } else {
//                    opacity = 0.1;
//                    red = clamp((red + (transparency * opacity * 1.0 * 1.0)), 0.0, 1.0);
//                    blue = clamp((blue + (transparency * opacity * 1.0 * 1.0)), 0.0, 1.0);
//                    green = clamp((green + (transparency * opacity * 1.0 * 1.0)), 0.0, 1.0);
//                    transparency = transparency * (1 - opacity);
//                }
//            }
//            image_writer.setColor(j, k, Color.color(red, blue, green, 1));
//        }
//    }
//}
}
