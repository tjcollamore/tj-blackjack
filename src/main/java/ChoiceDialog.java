import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;

import javafx.util.Duration;

public class ChoiceDialog {

    private static final double ACTION_BAR_HEIGHT = 57;
    
    public static String show(
        Stage ownerStage,
        String message,
        String... options)
    {
        return show(ownerStage, message, null, null, options);
    }

    public static String show(
        Stage ownerStage,
        String message,
        Node leftAccessory,  // settings icon
        Node leadingContent, // generic content for wager selection
        String... options) 
    {
        final String[] result = {null};

        // Dialog content
        StackPane buttonBar = new StackPane();
        buttonBar.setPadding(new Insets(10));
        buttonBar.setMinHeight(ACTION_BAR_HEIGHT);
        buttonBar.setPrefHeight(ACTION_BAR_HEIGHT);
        buttonBar.setMaxHeight(ACTION_BAR_HEIGHT);
        
        buttonBar.setBackground(new Background(
            new BackgroundFill(
                Color.rgb(40, 40, 40, 0.95),
                CornerRadii.EMPTY,
                Insets.EMPTY
            )
        ));

        buttonBar.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonBar, Priority.NEVER);


        // The actual game controls stay centered as one unit
        HBox actionControls = new HBox(10);
        actionControls.setAlignment(Pos.CENTER);

        buttonBar.getChildren().add(actionControls);
        StackPane.setAlignment(actionControls, Pos.CENTER);


        // Optional far-left accessory, such as the settings gear
        if (leftAccessory != null) {
            buttonBar.getChildren().add(leftAccessory);

            StackPane.setAlignment(leftAccessory, Pos.CENTER_LEFT);
            StackPane.setMargin(
                leftAccessory,
                new Insets(0, 0, 0, 16)
            );
        }


        // Optional content immediately before the choice buttons,
        // such as the wager selector
        if (leadingContent != null) {
            actionControls.getChildren().add(leadingContent);
            HBox.setMargin(leadingContent, new Insets(0, 14, 0, 0));
        }
        
        StackPane persistentMessageBox = null;
        if (message != null && !message.isBlank())
        {
            persistentMessageBox =
                showPersistentGameMessage(ownerStage, message);
        }

        
        for (int i = 0; i < options.length; i++) 
        {
            String originalLabel = options[i];
            String label = originalLabel;

            Button btn = new Button(label);
            btn.setPrefWidth(100);
            String baseColor = (i == 0) ? "#007bff" : "#6c757d";  // Make first button primary
            String focusColor = (i == 0) ? "#339af0" : "#868e96";
            applyButtonStyles(btn, baseColor, focusColor);

            final String finalLabel = originalLabel; // Ensure final variable for lambda
            btn.setOnAction(e -> {
                result[0] = finalLabel;
                ((Stage) buttonBar.getScene().getWindow()).close();
            });

            actionControls.getChildren().add(btn);
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

        // Set an initial position before the stage becomes visible.
        positionActionBar(dialogStage, ownerStage);

        // Correct the position once JavaFX/OS has actually created the window.
        // This is especially important for the very first action bar.
        dialogStage.setOnShown(e ->
            positionActionBar(dialogStage, ownerStage)
        );

        dialogStage.showAndWait();

        if (persistentMessageBox != null)
        {
           StackPane gameRoot =
                (StackPane) ownerStage.getScene().getRoot();
           gameRoot.getChildren().remove(persistentMessageBox);
        }
        
        return result[0];
    }

    private static StackPane showPersistentGameMessage(
        Stage ownerStage,
        String message)
    {
        Color accent = getMessageAccent(message);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);
        messageLabel.setAlignment(Pos.CENTER);

        messageLabel.setStyle("""
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-text-alignment: center;
        """);

        StackPane messageBox = new StackPane(messageLabel);
        messageBox.setPadding(
            new Insets(14, 24, 14, 24)
        );

        messageBox.setMaxWidth(Region.USE_PREF_SIZE);
        messageBox.setMaxHeight(Region.USE_PREF_SIZE);

        messageBox.setBackground(
            new Background(
                new BackgroundFill(
                    Color.rgb(18, 18, 18, 0.88),
                    new CornerRadii(12),
                    Insets.EMPTY
                )
            )
        );

        messageBox.setBorder(
            new Border(
                new BorderStroke(
                    accent,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(2)
                )
            )
        );

        DropShadow glow = new DropShadow();
        glow.setRadius(18);
        glow.setSpread(0.30);
        glow.setColor(accent);
        messageBox.setEffect(glow);

        StackPane gameRoot =
            (StackPane) ownerStage.getScene().getRoot();

        StackPane.setAlignment(
            messageBox,
            Pos.CENTER
        );

        messageBox.setTranslateY(-20);

        gameRoot.getChildren().add(messageBox);

        return messageBox;
    }
    
    
    public static void showGameMessage(Stage ownerStage, String message) {
        Color accent = getMessageAccent(message);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setStyle("""
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-text-alignment: center;
        """);

        StackPane messageBox = new StackPane(messageLabel);
        messageBox.setPadding(new Insets(14, 24, 14, 24));

        messageBox.setMaxWidth(Region.USE_PREF_SIZE);
        messageBox.setMaxHeight(Region.USE_PREF_SIZE);
        
        messageBox.setBackground(new Background(
            new BackgroundFill(
                Color.rgb(18, 18, 18, 0.88),
                new CornerRadii(12),
                Insets.EMPTY
            )
        ));

        messageBox.setBorder(new Border(
            new BorderStroke(
                accent,
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(2)
            )
        ));

        DropShadow glow = new DropShadow();
        glow.setRadius(18);
        glow.setSpread(0.30);
        glow.setColor(accent);
        messageBox.setEffect(glow);

        // Add directly to the existing blackjack scene
        StackPane gameRoot = (StackPane) ownerStage.getScene().getRoot();

        StackPane.setAlignment(messageBox, Pos.CENTER);

        // Optional: move it a little above/below exact center
        messageBox.setTranslateY(-20);

        messageBox.setOpacity(0.0);
        gameRoot.getChildren().add(messageBox);

        FadeTransition fadeIn =
            new FadeTransition(Duration.millis(180), messageBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition hold =
            new PauseTransition(Duration.seconds(1.65));

        FadeTransition fadeOut =
            new FadeTransition(Duration.millis(270), messageBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition animation =
            new SequentialTransition(fadeIn, hold, fadeOut);

        animation.setOnFinished(e ->
            gameRoot.getChildren().remove(messageBox)
        );

        animation.play();
    }
    
    private static Color getMessageAccent(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("blackjack")
                || lower.contains("win")
                || lower.contains("won")) {
            return Color.web("#FFD700"); // gold
        }

        if (lower.contains("bust")
                || lower.contains("lost")
                || lower.contains("lose")) {
            return Color.web("#A52A2A"); // dark casino red
        }

        if (lower.contains("push")
                || lower.contains("tie")) {
            return Color.web("#C0C0C0"); // silver
        }

        return Color.web("#D4AF37"); // antique gold
    }

    private static void positionActionBar(
        Stage dialogStage,
        Stage ownerStage)
    {
        dialogStage.setWidth(ownerStage.getWidth());
        dialogStage.setHeight(ACTION_BAR_HEIGHT);

        dialogStage.setX(ownerStage.getX());
        dialogStage.setY(
            ownerStage.getY()
            + ownerStage.getHeight()
            - ACTION_BAR_HEIGHT
        );
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
