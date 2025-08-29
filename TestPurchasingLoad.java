import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestPurchasingLoad extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Attempting to load purchasing.fxml...");
            
            // Try to load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/purchasing.fxml"));
            VBox root = loader.load();
            
            System.out.println("Successfully loaded purchasing.fxml!");
            System.out.println("Controller: " + loader.getController().getClass().getSimpleName());
            
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Test Purchasing FXML Load");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading purchasing.fxml:");
            e.printStackTrace();
            
            // Show a simple placeholder
            VBox placeholder = new VBox();
            placeholder.getChildren().add(new javafx.scene.control.Label("Error: " + e.getMessage()));
            
            Scene scene = new Scene(placeholder, 400, 300);
            primaryStage.setTitle("Test Failed");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}