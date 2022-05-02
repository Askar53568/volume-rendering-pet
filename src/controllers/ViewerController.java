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
import models.VolumeRender;
import models.tfViewer;
import views.Menu;

import java.io.IOException;

/**
 * This class provides a UI for the program
 */
public class ViewerController {
    @FXML
    //first image view
    public ImageView firstView;
    //second image view
    public ImageView secondView;
    //third image view
    public ImageView thirdView;
    public ImageView resizedView;
    // 1D transfer function slider
    public Slider firstSlider;
    // 2D transfer function slider
    public Slider secondSlider;
    //opacity slider for 1D transfer function
    public Slider opacitySlider;
    //threshold slider for all 3 transfer functions
    public Slider thresholdSlider;
    //access volume rendering menu
    public Button volumeRenderButton;
    //access basic menu
    public Button baseMenuButton;
    //background for the first view
    public StackPane firstViewBackground;
    //background for the second view
    public StackPane secondViewBackground;
    //background for the third view
    public StackPane thirdViewBackground;
    //menu pane
    public StackPane menuPane;
    //slider for positioning the light source
    public Slider lightSource;
    //set the gradient shading
    public Button gradientButton;
    //apply interpolation
    public Button gradientInterpolationButton;
    //apply color transfer function
    public Button colorButton;
    //container for the volume rendering menu
    public VBox volRendMenu;
    //container for the light manipulation menu
    public VBox lightMenu;
    //access the transfer function name
    public ChoiceBox<String> tfChoice;
    //scroll pane
    public ScrollPane scrollPane;
    public Slider widthSlider;

    //stage
    private Stage stage;
    //reference to the volume rendering class
    private VolumeRender volumeRender;
    //reference to the transfer
    private final tfViewer tfViewer = new tfViewer();
    //volume render toggle
    private boolean isVolumeRendered = false;
    //transfer function specification
    private String transferFunction = "TF1";

    //volume view from the top
    WritableImage top_image;
    //volume view from the front
    WritableImage front_image;
    //volume view from the side
    WritableImage side_image;
    //resized image
    WritableImage resized_image;

    /**
     * Initialises UI elements to be ready for display.
     */
    public void init() {
        top_image = new WritableImage(volumeRender.getTop_width(), volumeRender.getTop_height());
        front_image = new WritableImage(volumeRender.getFront_width(), volumeRender.getFront_height());
        side_image = new WritableImage(volumeRender.getSide_width(), volumeRender.getSide_height());
        resized_image = new WritableImage(511, 133);

        Menu menu = new Menu(stage);
        menuPane.getChildren().add(menu.getRoot());
        menu.getController().setStage(stage);
        menu.getRoot().setVisible(false);

        firstView.setImage(top_image);
        secondView.setImage(front_image);
        thirdView.setImage(side_image);
        //resizedView.setImage(resized_image);


        firstSlider.setMax(113);
        thresholdSlider.setMax(4500);
        thresholdSlider.setMin(-1000);
        secondSlider.setMax(256);
        widthSlider.setValue(2);


        tfChoice.getItems().add("TF1");
        tfChoice.getItems().add("TF2D");
        tfChoice.getItems().add("TF3D");
        tfChoice.setValue("TF1");

        tfChoice.setOnAction(event -> {
                    transferFunction = tfChoice.getValue();
                    volumeRender();
                }
        );


        baseMenuButton.setOnAction(event -> {
            reset();
            firstSlider.valueProperty().setValue(76);
            secondSlider.valueProperty().setValue(76);
            widthSlider.valueProperty().setValue(2);

        });

        /*
          Sets an On-click event for the top image view
          Displays the top image resized x2
         */
        firstView.setOnMouseClicked(event -> tfViewer.resizePopUp(top_image, 2));
        /*
          Displays the front image resized x2
         */
        secondView.setOnMouseClicked(event -> tfViewer.resizePopUp(front_image, 2));
        /*
          Displays the side image resized x2
         */
        thirdView.setOnMouseClicked(event -> tfViewer.resizePopUp(side_image, 2));

        /*
          Sets the menu for volume rendering visible
         */
        volumeRenderButton.setOnAction(event -> {
            if (!isVolumeRendered) { //if "volume render" button is pressed
                volumeRender();
                volRendMenu.setVisible(true);
                volRendMenu.setManaged(true);
                isVolumeRendered = true;
            } else {
                baseMenuButton.fire(); //activate base menu
            }
        });
        /*
          Set the second view slider to input threshold values for the gradient magnitude - intensity transfer function
         */
        firstSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volumeRender.drawSlice(top_image, "top", newValue.intValue());
            //tfViewer.FrontRotate(resized_image,secondSlider.getValue(), newValue.doubleValue());
            reset();
            setSliderStyle(firstSlider);
            reset();
        });
        /*
          Set the third slider to input threshold values for the gradient magnitude - intensity - second derivative transfer function
         */
        secondSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                volumeRender.drawSlice(front_image, "front", newValue.intValue());
                volumeRender.drawSlice(side_image, "side", newValue.intValue());
            }catch(ArrayIndexOutOfBoundsException e){

            }
            //tfViewer.setAngle(newValue.doubleValue());
            //tfViewer.FrontRotate(resized_image, newValue.doubleValue(),firstSlider.getValue());
            setSliderStyle(secondSlider);
            reset();
        });
        /*
          Set the opacity for the 1D Tent function
         */
        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volumeRender.setOpacity((double) (newValue) / 100.0);
            volumeRender();
            setSliderStyle(opacitySlider);
        });
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volumeRender.setWidth((double) (newValue));
            volumeRender();
            setSliderStyle(widthSlider);
        });
        /*
          Set the threshold for all 3 transfer function
         */
        thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volumeRender.setThreshold((double) (newValue));
            volumeRender();
            setSliderStyle(thresholdSlider);
        });
        /*
          Set the position of the light source
         */
        lightSource.valueProperty().addListener((observable, oldValue, newValue) -> {
            volumeRender.setLightX(newValue.intValue());
            volumeRender();
            setSliderStyle(lightSource);
        });
        /*
          Set the diffuse shading
         */
        gradientButton.setOnAction(e -> {
            volumeRender.setGradientShading(!volumeRender.getGradientShading());
            lightMenu.setVisible(volumeRender.getGradientShading());
            lightMenu.setManaged(true);
            volumeRender();
        });
        /*
          Apply gradient interpolation
         */
        gradientInterpolationButton.setOnAction(e -> {
            volumeRender.setGradientInterpolation(!volumeRender.getGradientInterpolation());
            String value = volumeRender.getGradientInterpolation() ? "On" : "Off";
            gradientInterpolationButton.setText("Interpolation: " + value);
            volumeRender();
        });
        /*
          Apply or not apply the color transfer function
         */
        colorButton.setOnAction(e -> {
            String value = tfViewer.getColor() ? "Off" : "On";
            colorButton.setText("Color: "+ value);
            tfViewer.changeColor();
            volumeRender.changeColor();
        });
    }

    /**
     * Resets the volume rendering menu.
     */
    public void reset() {
        volRendMenu.setVisible(false);
        volRendMenu.setManaged(false);
        isVolumeRendered = false;
        volumeRender.setGradientShading(false);
        volumeRender.setGradientInterpolation(false);
        gradientInterpolationButton.setText("Interpolation: Off");
        lightMenu.setVisible(false);
        lightMenu.setManaged(false);
    }

    /**
     * Carries out volume rendering on all views.
     */
    public void volumeRender() {
        renderView(side_image, "side");
        renderView(top_image, "top");
        renderView(front_image, "front");
    }

    /**
     * Changes the style of a slider to be a different colour up to the value selected/thumb location.
     *
     * @param slider The slider to update.
     */
    public void setSliderStyle(Slider slider) {
        double value = (slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin()) * 100.0;
        slider.lookup(".slider .track").setStyle(String.format("-fx-background-color: " +
                        "linear-gradient(to right, #0278D7 0%%, #0278D7 %f%%, #383838 %f%%, #383838 100%%);",
                value, value));
    }

    /**
     * Sets the viewer to use.
     *
     * @param VolumeRender The viewer to use.
     */
    public void setVolumeRenderViewer(VolumeRender VolumeRender) {
        this.volumeRender = VolumeRender;
    }

    /**
     * Gets the slider that sets opacity.
     *
     * @return The opacity slider.
     */
    public Slider getOpacitySlider() {
        return opacitySlider;
    }

    /**
     * Gets the slider that sets the threshold.
     * @return The threshold slider.
     */
    public Slider getThresholdSlider() {
        return thresholdSlider;
    }


    /**
     * Gets the button that displays middle slide.
     *
     * @return The base menu button.
     */
    public Button getBaseMenuButton() {
        return baseMenuButton;
    }

    /**
     * Gets the pane in which the menu is to be displayed.
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
     * Individual view volume render
     *
     * @param image The image to display the rendered image.
     * @param view  The direction of the volume.
     */
    private void renderView(WritableImage image, String view) {
        PixelReader reader = volumeRender.volumeRender(image, view, transferFunction).getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        image.getPixelWriter().setPixels(0, 0, width, height, reader, 0, 0);
    }
}
