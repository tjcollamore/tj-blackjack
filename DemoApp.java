import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class DemoApp extends Application {
    private final Label mainDisplay = new Label("Current Hand: 1");

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        mainDisplay.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        SplitBadgeContainer container = new SplitBadgeContainer();

        // Simulate 4 split hands
        List<ScoreBadge> badges = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ScoreBadge badge = new ScoreBadge();
            badge.setText(String.valueOf(13 + i * 2), Color.DARKSLATEBLUE);
            int index = i;
            badge.setOnMouseClicked(e -> {
                mainDisplay.setText("Current Hand: " + (index + 1));
                container.highlight(index);
            });
            badges.add(badge);
        }

        // OK badge
        ScoreBadge okBadge = new ScoreBadge();
        okBadge.setText("OK", Color.DARKGREEN);
        okBadge.setOnMouseClicked(e -> {
            mainDisplay.setText("Exiting split mode...");
            container.hide();
        });

        container.show(badges, okBadge);
        container.highlight(0);

        root.getChildren().addAll(mainDisplay, container);
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("SplitBadgeContainer Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
