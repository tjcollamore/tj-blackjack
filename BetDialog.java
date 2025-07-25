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

public class BetDialog
{
    private static int previousBet = GameOptions.MIN_BET;
    
    public static int show(Stage ownerStage, int minBet, int maxBet)
    {
        // Round down to nearest 5
        maxBet -= maxBet % 5;

        // Cap the default selection to the max bet
        if (previousBet > maxBet)
            previousBet = maxBet;

        
        //should not happen,
        //unless min bet allowed to change
        if (previousBet < minBet)
            previousBet = minBet;
        
        final Integer[] result = {null};

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        overlay.setAlignment(Pos.CENTER);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(Color.web("#f9f9f9"), CornerRadii.EMPTY, Insets.EMPTY)));

        Text prompt = new Text("Select your wager:");
        prompt.setStyle("-fx-font-size: 16px; -fx-fill: #333;");

        Spinner<Integer> spinner = new Spinner<>(minBet, maxBet, previousBet, 5);
        spinner.setEditable(false);
        spinner.setPrefWidth(150);

        HBox chipPreview = new HBox(); // You can draw chips here later
        chipPreview.setSpacing(5);
        chipPreview.setAlignment(Pos.CENTER);
        chipPreview.setPrefHeight(60);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button submit = new Button("Submit");
//        Button cancel = new Button("Cancel");

        applyButtonStyles(submit, "#007bff", "#339af0");
//        applyButtonStyles(cancel, "#6c757d", "#868e96");

        submit.setOnAction(e -> {
            result[0] = spinner.getValue();
            previousBet = spinner.getValue();
            ((Stage) content.getScene().getWindow()).close();
        });
/*
        cancel.setOnAction(e -> {
            result[0] = null;
            ((Stage) content.getScene().getWindow()).close();
        });
*/
        buttons.getChildren().addAll(submit/*, cancel*/);
        content.getChildren().addAll(prompt, spinner, chipPreview, buttons);

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
