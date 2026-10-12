import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;


public class HandSignalAnimator
{
  private final Pane overlay;
  private final Node wagerArea;
  private static final Duration GESTURE_DURATION = Duration.millis(750);
    
  private final Image hitGestureImage;
  private final Image standGestureImage;
  private final Image doubleGestureImage;
  private final Image splitGestureImage;
  private final Image surrenderGestureImage;


  public HandSignalAnimator(
      Pane overlay,
      Node wagerArea)
  {
    this.overlay = overlay;
    this.wagerArea = wagerArea;

    hitGestureImage =
        loadGesture(
            "Gesture-Hit.png"
        );

    standGestureImage =
        loadGesture(
            "Gesture-Stand.png"
        );

    doubleGestureImage =
        loadGesture(
            "Gesture-Double.png"
        );

    splitGestureImage =
        loadGesture(
            "Gesture-Split.png"
        );

    surrenderGestureImage =
        loadGesture(
            "Gesture-Surrender.png"
        );
  }


    public Duration show(
        HandAction action)
    {
      switch (action)
      {
        case HIT:
          showGesture(
              hitGestureImage,
              110,
              100,
              0.84,
              0,
              -8
          );
          break;

        case STAND:
          showGesture(
              standGestureImage,
              125,
              100,
              0.82,
              -12,
              0
          );
          break;

        case DOUBLE:
          showGesture(
              doubleGestureImage,
              105,
              100,
              0.88,
              0,
              6
          );
          break;

        case SPLIT:
          showGesture(
              splitGestureImage,
              105,
              100,
              0.88,
              0,
              6
          );
          break;

        case SURRENDER:
          showGesture(
              surrenderGestureImage,
              125,
              100,
              0.82,
              -12,
              0
          );
          break;

        default:
          return Duration.ZERO;
      }

      return GESTURE_DURATION;
    }
    

  private Image loadGesture(
      String filename)
  {
    return new Image(
        "file:src/main/resources/"
        + filename
    );
  }


  private void showGesture(
      Image image,
      double width,
      double yOffset,
      double anchorFraction,
      double startX,
      double startY)
  {
    Bounds bounds =
        wagerArea.getBoundsInLocal();

    Point2D wagerCenterInScene =
        wagerArea.localToScene(
            bounds.getCenterX(),
            bounds.getCenterY()
        );

    Point2D wagerCenter =
        overlay.sceneToLocal(
            wagerCenterInScene
        );


    double targetX =
        wagerCenter.getX();

    double targetY =
        wagerCenter.getY()
        + yOffset;


    double height =
        width
        * image.getHeight()
        / image.getWidth();


    ImageView gesture =
        new ImageView(
            image
        );

    gesture.setPreserveRatio(
        true
    );

    gesture.setFitWidth(
        width
    );

    gesture.setMouseTransparent(
        true
    );


    /*
     * Horizontally center every gesture on
     * the betting circle.
     *
     * anchorFraction describes where the
     * visual "contact point" sits vertically
     * within each PNG.
     */
    gesture.setLayoutX(
        targetX
        - width / 2.0
    );

    gesture.setLayoutY(
        targetY
        - height * anchorFraction
    );


    overlay.getChildren().add(
        gesture
    );


    TranslateTransition movement =
        new TranslateTransition(
            Duration.millis(260),
            gesture
        );

    movement.setFromX(
        startX
    );

    movement.setToX(
        0
    );

    movement.setFromY(
        startY
    );

    movement.setToY(
        0
    );


    FadeTransition fade =
        new FadeTransition(
            GESTURE_DURATION,
            gesture
        );

    fade.setFromValue(
        0.95
    );

    fade.setToValue(
        0.0
    );


    ParallelTransition animation =
        new ParallelTransition(
            movement,
            fade
        );

    animation.setOnFinished(e ->
        overlay.getChildren().remove(
            gesture
        )
    );

    animation.play();
  }
}
