package views;
import controllers.ViewerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import models.VolumeRender;
import java.util.Objects;

public class Viewer {

    /**
     * Creates and displays a viewer using the specified ct viewer.
     * @param stage The stage to display this scene on.
     * @param volumeRender The viewer to display.
     */
    public Viewer(Stage stage, VolumeRender volumeRender){
        ViewerController controller = new ViewerController();
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());

            Scene scene = new Scene(root, volumeRender.getTop_width() + volumeRender.getSide_width() + 350,
                    Math.max(620, volumeRender.getSide_height() + volumeRender.getFront_height() + 50));
            controller = loader.getController();
            controller.setVolumeRenderViewer(volumeRender);
            scene.getStylesheets().add("styles.css");
            controller.setStage(stage);
            controller.init();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            stage.close();
        }
        controller.setSliderStyle(controller.getOpacitySlider());
        controller.setSliderStyle(controller.getThresholdSlider());
        controller.getBaseMenuButton().fire();
    }

    /**
     * Creates and displays a viewer showing the menu specified.
     * @param stage The stage to display this scene on.
     * @param menu The menu to display.
     */
    public Viewer(Stage stage, Parent menu){
        ViewerController controller = new ViewerController();
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());
            Scene scene = new Scene(root, 890, 550);
            controller = loader.getController();
            controller.getVolRendMenu().setManaged(false);
            scene.getStylesheets().add("styles.css");
            controller.getMenuPane().getChildren().add(menu);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            stage.close();
        }

        controller.setSliderStyle(controller.getOpacitySlider());
        controller.setSliderStyle(controller.getThresholdSlider());
        controller.getBaseMenuButton().fire();
    }
}
