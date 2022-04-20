package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.CTViewer;
import models.Example;
import views.Menu;

import java.io.IOException;

/**
 * Controller class for the viewer interface.
 * Handles all user input and displays scans.
 *
 * @author Josh Codd.
 */
public class ViewerController {
    @FXML
    public ImageView firstView;
    public ImageView secondView;
    public ImageView thirdView;
    //public Slider firstViewSlider;
    public Slider secondViewSlider;
    public Slider thirdViewSlider;
    public Slider opacitySlider;
    public Slider tresholdSlider;
    public Button volumeRenderButton;
    public Button midSlideButton;
    public StackPane firstViewBackground;
    public StackPane secondViewBackground;
    public StackPane thirdViewBackground;
    public StackPane menuPane;
    public Button openFileButton;
    public Slider lightSource;
    public Button gradientButton;
    public Button gradientInterpolationButton;
    public Button mipButton;
    public Button colorButton;
    public Button popUpButton;
    public VBox volRendMenu;
    public VBox lightMenu;
    public ChoiceBox<String> tfChoice;
    public ScrollPane sc;

    private Stage stage;
    private CTViewer ctViewer;
    private Example example = new Example();
    private boolean isVolumeRendered = false;
    private boolean isMIP = false;
    private String transferFunction = "TF1";

    WritableImage top_image;
    WritableImage front_image;
    WritableImage side_image;

    /**
     * Initialises UI elements to be ready for display.
     */
    public void init() {
        top_image = new WritableImage(ctViewer.getTop_width(), ctViewer.getTop_height());
        front_image = new WritableImage(ctViewer.getFront_width(), ctViewer.getFront_height());
        side_image = new WritableImage(ctViewer.getSide_width(), ctViewer.getSide_height());

        Menu menu = new Menu(stage);
        menuPane.getChildren().add(menu.getRoot());
        menu.getController().setStage(stage);
        menu.getRoot().setVisible(false);

        firstView.setImage(top_image);
        secondView.setImage(front_image);
        thirdView.setImage(side_image);

        secondViewSlider.setMax(4500);
        tresholdSlider.setMax(1200);
        tresholdSlider.setMin(-1000);
        thirdViewSlider.setMax(1500);


        tfChoice.getItems().add("TF1");
        tfChoice.getItems().add("TF2");
        tfChoice.setValue("TF1");

        tfChoice.setOnAction(event -> {
                    transferFunction = tfChoice.getValue();
                    volumeRender();
                }
        );


        midSlideButton.setOnAction(event -> {
            reset();
            isMIP = false;
            //firstViewSlider.valueProperty().setValue(76);
            secondViewSlider.valueProperty().setValue(1000);
            thirdViewSlider.valueProperty().setValue(0);
        });

        popUpButton.setOnAction(event -> {
            try {
                example.laplacianSide(side_image, 2300.0f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            example.resizePopUp(side_image, 600,400);
        });


        volumeRenderButton.setOnAction(event -> {
            if (!isVolumeRendered) {
                isMIP = false;
                volumeRender();
                volRendMenu.setVisible(true);
                volRendMenu.setManaged(true);
                isVolumeRendered = true;
            } else {
                midSlideButton.fire();
            }
        });

        secondViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            //ctViewer.drawSlice(front_image,"front", newValue.intValue());
            try {
                example.laplacianSide(side_image, newValue.doubleValue());
                example.opacityComputeSideNoReturn(front_image, newValue.doubleValue());
                example.opacityComputeTop(top_image, newValue.doubleValue());
                reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sliderValueStyle(secondViewSlider);
            reset();
            isMIP = false;
        });

        thirdViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                example.levoyTentFront(front_image, newValue.doubleValue());
                example.levoyTentSide(side_image, newValue.doubleValue());
                example.levoyTentTop(top_image, newValue.doubleValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            sliderValueStyle(thirdViewSlider);
            reset();
            isMIP = false;
        });

        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.setOpacity((double) (newValue) / 100.0);
            volumeRender();
            sliderValueStyle(opacitySlider);
        });

        tresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.setTreshold((double) (newValue));
            volumeRender();
            sliderValueStyle(tresholdSlider);
        });

        lightSource.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.setLightSourceX(newValue.intValue());
            volumeRender();
            sliderValueStyle(lightSource);
        });

        gradientButton.setOnAction(e -> {
            ctViewer.setGradientShading(!ctViewer.getGradientShading());
            lightMenu.setVisible(ctViewer.getGradientShading());
            lightMenu.setManaged(true);
            volumeRender();
        });

        gradientInterpolationButton.setOnAction(e -> {
            ctViewer.setGradientInterpolation(!ctViewer.getGradientInterpolation());
            String value = ctViewer.getGradientInterpolation() ? "On" : "Off";
            gradientInterpolationButton.setText("Interpolation: " + value);
            volumeRender();
        });

        mipButton.setOnAction(e -> {
            try {
                example.opacityComputeSideNoReturn(side_image, 1000);
                example.opacityComputeFront(front_image, 1000);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        colorButton.setOnAction(e -> {
            String value = example.getColor() ? "Off" : "On";
            colorButton.setText("Color: "+ value);
            example.changeColor();
            try {
                example.opacityComputeSideNoReturn(side_image, 1000);
                example.opacityComputeFront(front_image, 1000);
                example.opacityComputeTop(top_image, 1000);
            } catch (IOException ex) {
                ex.printStackTrace();
            }


        });
        openFileButton.setOnAction(e -> {
            menu.getRoot().setVisible(!menu.getRoot().isVisible());
            volRendMenu.setManaged(false);
        });
    }

    /**
     * Resets the volume rendering menu.
     */
    public void reset() {
        volRendMenu.setVisible(false);
        volRendMenu.setManaged(false);
        isVolumeRendered = false;
        ctViewer.setGradientShading(false);
        ctViewer.setGradientInterpolation(false);
        gradientInterpolationButton.setText("Interpolation: Off");
        lightMenu.setVisible(false);
        lightMenu.setManaged(false);
    }

    /**
     * Carries out volume rendering on all views.
     */
    public void volumeRender() {
        volumeRenderSingle(side_image, "side");
        volumeRenderSingle(top_image, "top");
        volumeRenderSingle(front_image, "front");
    }

    /**
     * Changes the style of a slider to be a different colour up to the value selected/thumb location.
     *
     * @param slider The slider to update.
     */
    public void sliderValueStyle(Slider slider) {
        double value = (slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin()) * 100.0;
        slider.lookup(".slider .track").setStyle(String.format("-fx-background-color: " +
                        "linear-gradient(to right, #0278D7 0%%, #0278D7 %f%%, #383838 %f%%, #383838 100%%);",
                value, value));
    }

    /**
     * Sets the viewer to use.
     *
     * @param CTViewer The viewer to use.
     */
    public void setCTViewer(CTViewer CTViewer) {
        this.ctViewer = CTViewer;
    }

    /**
     * Gets the slider that sets opacity.
     *
     * @return The opacity slider.
     */
    public Slider getOpacitySlider() {
        return opacitySlider;
    }

    public Slider getTresholdSlider() {
        return tresholdSlider;
    }


    /**
     * Gets the button that displays middle slide.
     *
     * @return The mid slide button.
     */
    public Button getMidSlideButton() {
        return midSlideButton;
    }

    /**
     * Gets the pane in which the menu is to be displayed..
     *
     * @return The menu pane.
     */
    public StackPane getMenuPane() {
        return menuPane;
    }

    /**
     * Gets the pane in which the volume rendering options are to be displayed in.
     *
     * @return The volume rendering menu pane.
     */
    public VBox getVolRendMenu() {
        return volRendMenu;
    }

    /**
     * Sets the stage of the application.
     *
     * @param stage The stage to set.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Carries out volume rendering on a single view.
     *
     * @param image The image to display the rendered image in.
     * @param view  The direction to view the scan/dataset from. i.e front, side or top.
     */
    private void volumeRenderSingle(WritableImage image, String view) {
        PixelReader reader = ctViewer.volumeRender(image, view, transferFunction).getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        image.getPixelWriter().setPixels(0, 0, width, height, reader, 0, 0);
    }
}
