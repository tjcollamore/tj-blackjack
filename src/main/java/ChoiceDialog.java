import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ChoiceDialog {
    public static String show(Stage ownerStage, String message, String... options) {
        final String[] result = {null};

        // Dialog content
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40, 0.95), CornerRadii.EMPTY, Insets.EMPTY)));
//        buttonBar.setMaxWidth(Region.USE_PREF_SIZE);
        buttonBar.setMaxWidth(Double.MAX_VALUE); // allow it to stretch
        HBox.setHgrow(buttonBar, Priority.ALWAYS); // fill horizontal space

        for (int i = 0; i < options.length; i++) {
            String originalLabel = options[i];
            if (originalLabel.equals(message)) continue; // Skip message-as-button

            String label = originalLabel;

            Button btn = new Button(label);
            btn.setPrefWidth(100);
            String baseColor = (i == 1) ? "#007bff" : "#6c757d";  // Make second button primary
            String focusColor = (i == 1) ? "#339af0" : "#868e96";
            applyButtonStyles(btn, baseColor, focusColor);

            final String finalLabel = originalLabel; // Ensure final variable for lambda
            btn.setOnAction(e -> {
                result[0] = finalLabel;
                ((Stage) buttonBar.getScene().getWindow()).close();
            });

            buttonBar.getChildren().add(btn);
        }

        // Outer wrapper to dock to bottom only
        VBox root = new VBox();
        root.setAlignment(Pos.BOTTOM_CENTER);
        root.setPadding(new Insets(0, 0, 0, 0)); // Fine-tuned vertical padding
//        root.setBackground(Background.EMPTY);
        root.setBackground(new Background(
            new BackgroundFill(Color.rgb(40, 40, 40, 0.95), CornerRadii.EMPTY, Insets.EMPTY)
        ));
        root.getChildren().add(buttonBar);

        // Only use root in the Scene (no StackPane wrapper)
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Stage setup
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.NONE);
        dialogStage.initOwner(ownerStage);
        dialogStage.setScene(scene);

        dialogStage.setOnShown(e -> {
            javafx.application.Platform.runLater(() -> {
                dialogStage.setWidth(ownerStage.getWidth());
                dialogStage.setHeight(root.prefHeight(-1));
                dialogStage.setX(ownerStage.getX());
                dialogStage.setY(ownerStage.getY() + ownerStage.getHeight() - dialogStage.getHeight());
            });
        });

        dialogStage.showAndWait();
        return result[0];
    }

    private static void applyButtonStyles(Button button, String baseColor, String focusColor) {
        // Set default to rounded style (even before focused)
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 4px;
            -fx-border-color: transparent;
            -fx-border-width: 2px;
            -fx-border-radius: 4px;
        """, baseColor));

        button.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            String color = isNowFocused ? focusColor : baseColor;
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 4px;
                -fx-border-color: %s;
                -fx-border-width: 2px;
                -fx-border-radius: 4px;
            """, color, isNowFocused ? "#66b2ff" : "transparent"));
        });
    }
}
