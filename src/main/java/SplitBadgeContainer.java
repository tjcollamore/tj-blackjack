import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class SplitBadgeContainer extends StackPane {
    private final HBox badgeRow;
    private final List<ScoreBadge> scoreBadges = new ArrayList<>();
    private ScoreBadge okBadge;
    private boolean visibleState = false;

    public SplitBadgeContainer() {
        setPickOnBounds(false);
        setPadding(new Insets(5, 10, 5, 10));
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 60, 0.85), new CornerRadii(12), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(Color.LIGHTBLUE, BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
        setEffect(new DropShadow(5, Color.BLACK));
        setOpacity(0);
        setTranslateY(-10);

        badgeRow = new HBox(10);
        badgeRow.setAlignment(Pos.CENTER);
        getChildren().add(badgeRow);
        setVisible(false);
    }

    public void initialize(ScoreBadge okBadge) {
        this.okBadge = okBadge;
        scoreBadges.clear();
        badgeRow.getChildren().clear();
        badgeRow.getChildren().add(okBadge);
    }

    public void addScoreBadge(ScoreBadge badge) {
        int insertIndex = badgeRow.getChildren().size() - 1; // before OK
        badgeRow.getChildren().add(insertIndex, badge);
        scoreBadges.add(badge);

        badge.setScaleX(0.0);
        badge.setScaleY(0.0);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(badge.scaleXProperty(), 0),
                new KeyValue(badge.scaleYProperty(), 0)
            ),
            new KeyFrame(Duration.millis(250),
                new KeyValue(badge.scaleXProperty(), 1),
                new KeyValue(badge.scaleYProperty(), 1)
            )
        );
        timeline.play();
    }

    public void highlight(int index) {
        for (int i = 0; i < scoreBadges.size(); i++) {
            ScoreBadge badge = scoreBadges.get(i);
            badge.setScaleX(i == index ? 1.3 : 1.0);
            badge.setScaleY(i == index ? 1.3 : 1.0);
        }
    }

    public int getHandCount() {
        return scoreBadges.size();
    }

    public void showIfNeeded() {
        if (!visibleState) {
            visibleState = true;
            fadeAndSlideIn();
        }
    }

    public void hide() {
        if (visibleState) {
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
