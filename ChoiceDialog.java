import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ChoiceDialog
{
    public static String show(Stage ownerStage, String message, String... options)
    {
        final String[] result = {null};

        //Background overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        overlay.setAlignment(Pos.CENTER);

        //Dialog content
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY)));

        Text text = new Text(message);
        text.setStyle("-fx-font-size: 16px; -fx-fill: #333;");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        for (int i = 0; i < options.length; i++) {
            String label = options[i];
            Button btn = new Button(label);
            String baseColor = (i == 0) ? "#007bff" : "#6c757d";  // First button = primary
            String focusColor = (i == 0) ? "#339af0" : "#868e96";
            applyButtonStyles(btn, baseColor, focusColor);

            btn.setOnAction(e -> {
                result[0] = label;
                ((Stage) content.getScene().getWindow()).close();
            });

            buttons.getChildren().add(btn);
        }

        content.getChildren().addAll(text, buttons);

        //Outer container
        StackPane dialogBox = new StackPane();
        dialogBox.setPadding(new Insets(2));
        dialogBox.getChildren().add(content);
        dialogBox.setBackground(new Background(new BackgroundFill(
                Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY
        )));
        dialogBox.setEffect(new DropShadow(4, Color.rgb(0, 0, 0, 0.1)));

        overlay.getChildren().add(dialogBox);

        //Stage setup
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(ownerStage);
        dialogStage.setScene(new Scene(overlay, Color.TRANSPARENT));

        //Position offset from center
        dialogStage.setOnShown(e -> {
            javafx.application.Platform.runLater(() -> {
                double offsetX = 0;
                dialogStage.setX(ownerStage.getX() + ownerStage.getWidth() / 2 - dialogStage.getWidth() / 2 + offsetX);
                double offsetY = -90;
                dialogStage.setY(ownerStage.getY() + ownerStage.getHeight() / 2 - dialogStage.getHeight() / 2 + offsetY);
            });
        });

        //Draggable dialog
        final Delta dragDelta = new Delta();
        dialogBox.setOnMousePressed(e -> {
            dragDelta.x = dialogStage.getX() - e.getScreenX();
            dragDelta.y = dialogStage.getY() - e.getScreenY();
        });
        dialogBox.setOnMouseDragged(e -> {
            dialogStage.setX(e.getScreenX() + dragDelta.x);
            dialogStage.setY(e.getScreenY() + dragDelta.y);
        });

        dialogStage.showAndWait();
        return result[0];
    }

    private static void applyButtonStyles(Button button, String baseColor, String focusColor) {
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 4px;
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

    private static class Delta {
        double x, y;
    }
}
