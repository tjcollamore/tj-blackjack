import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Map;

public class BetDialog
{   
    public static int show(Stage ownerStage, int initialBet, int minBet, int maxBet)
    {
        // Round down to nearest 5
        maxBet -= maxBet % 5;

        // Cap the default selection to the max bet
        if (initialBet > maxBet)
            initialBet = maxBet;

        //should not happen,
        //unless min bet allowed to change
        if (initialBet < minBet)
            initialBet = minBet;
        
        final Integer[] result = {null};

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        overlay.setAlignment(Pos.CENTER);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(12));
        content.setBackground(new Background(new BackgroundFill(Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY)));
        content.setSpacing(10);

        Text prompt = new Text("What's your bet?");
        prompt.setStyle("-fx-font-size: 16px; -fx-fill: #333;");

        Spinner<Integer> spinner = new Spinner<>(minBet, maxBet, initialBet, 5);
        spinner.setEditable(false);
        spinner.setPrefWidth(75);

        VBox chipPreviewContainer = new VBox();
        chipPreviewContainer.setAlignment(Pos.CENTER);
        chipPreviewContainer.setPrefHeight(100);
        
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button submit = new Button("Let's go!");

        applyButtonStyles(submit, "#007bff", "#339af0");

        submit.setOnAction(e -> {
            result[0] = spinner.getValue();
            ((Stage) content.getScene().getWindow()).close();
        });
        buttons.getChildren().addAll(submit);
        content.getChildren().addAll(prompt, spinner, chipPreviewContainer, buttons);

        StackPane dialogBox = new StackPane();
        dialogBox.setPadding(new Insets(2));
        dialogBox.getChildren().add(content);
        dialogBox.setBackground(new Background(new BackgroundFill(Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY)));
        dialogBox.setEffect(new DropShadow(4, Color.rgb(0, 0, 0, 0.1)));

        overlay.getChildren().add(dialogBox);

        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(ownerStage);
        dialogStage.setScene(new Scene(overlay, Color.TRANSPARENT));

        dialogStage.setOnShown(e -> {
            javafx.application.Platform.runLater(() -> {
                double offsetX = 0;
                dialogStage.setX(ownerStage.getX() + ownerStage.getWidth() / 2 - dialogStage.getWidth() / 2 + offsetX);
                double offsetY = -90;
                dialogStage.setY(ownerStage.getY() + ownerStage.getHeight() / 2 - dialogStage.getHeight() / 2 + offsetY);
                submit.requestFocus();
            });
        });

        final Delta dragDelta = new Delta();
        dialogBox.setOnMousePressed(e -> {
            dragDelta.x = dialogStage.getX() - e.getScreenX();
            dragDelta.y = dialogStage.getY() - e.getScreenY();
        });
        dialogBox.setOnMouseDragged(e -> {
            dialogStage.setX(e.getScreenX() + dragDelta.x);
            dialogStage.setY(e.getScreenY() + dragDelta.y);
        });
/*
        // Initial chip preview
        Map<WagerChip, Integer> chips = WagerChip.getChipsByValue(spinner.getValue());
        ChipStackView previewView = new ChipStackView(chips, false);
        chipPreviewContainer.getChildren().setAll(previewView);

        // Update chip preview when spinner changes
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Map<WagerChip, Integer> updatedChips = WagerChip.getChipsByValue(newVal);
            ChipStackView updatedView = new ChipStackView(updatedChips, false);
            chipPreviewContainer.getChildren().setAll(updatedView);
        });
*/        
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

    private static class Delta
    {
        double x, y;
    }
}
