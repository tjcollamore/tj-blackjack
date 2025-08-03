import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class SubroundBadge extends StackPane {
    private final Label label;
    private boolean visibleState = false;

    public SubroundBadge() {
        setPickOnBounds(false);

        label = new Label("1 of 2");
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        setPadding(new Insets(3, 8, 3, 8));
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 60, 0.85), new CornerRadii(12), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(Color.LIGHTBLUE, BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
        setEffect(new DropShadow(5, Color.BLACK));
        setOpacity(0);      // start hidden
        setTranslateY(-10); // start slightly above

        getChildren().add(label);
        setVisible(false);
    }

    public void update(int current, int total) {
        label.setText(current + " of " + total);

        if (total > 1 && !visibleState) {
            visibleState = true;
            fadeAndSlideIn();
        } else if (total <= 1 && visibleState) {
            visibleState = false;
            fadeAndSlideOut();
        }
    }

    private void fadeAndSlideIn() {
        setVisible(true);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(opacityProperty(), 0),
                new KeyValue(translateYProperty(), -10)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(opacityProperty(), 1),
                new KeyValue(translateYProperty(), 0)
            )
        );
        timeline.play();
    }

    private void fadeAndSlideOut() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(opacityProperty(), 1),
                new KeyValue(translateYProperty(), 0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(opacityProperty(), 0),
                new KeyValue(translateYProperty(), -10)
            )
        );
        timeline.setOnFinished(e -> setVisible(false));
        timeline.play();
    }
}
