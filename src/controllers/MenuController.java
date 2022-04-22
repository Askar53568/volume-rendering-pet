package controllers;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.*;
import javafx.stage.Stage;
import views.Viewer;

/**
 * Controller class for the menu interface.
 * Handles all user input.
 */
public class MenuController {
    private Stage stage;

    public Button defaultButton;
    public VBox menuPane;

    /**
     * Initializes the user interface elements.
     */
    @FXML
    public void initialize(){

    }


    /**
     * Opens a new viewer with the CT scan being displayed.
     */
    public void handleCTViewer() {
        Volume v = new Volume(256,256, 113);
        try {
            v.ReadData("src/data/CThead_256_256_113", false);

            new Viewer(stage, new VolumeRender(v));
        } catch (Exception e){
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
        }
    }

    /**
     * Sets the stage of the application.
     * @param stage The stage to set.
     */
    public void setStage (Stage stage) {
        this.stage = stage;
    }

}
