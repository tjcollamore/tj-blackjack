
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class VNCTest extends Application {
    
    public static void main(String[] args) {
        // Set VNC compatibility properties
        System.setProperty("java.awt.headless", "false");
        System.setProperty("prism.order", "sw");
        System.setProperty("glass.platform", "gtk");
        
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("VNC Test - If you see this, JavaFX is working!");
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: blue;");
        
        StackPane root = new StackPane();
        root.getChildren().add(label);
        
        Scene scene = new Scene(root, 400, 200);
        
        primaryStage.setTitle("VNC Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("JavaFX window created successfully!");
    }
}
