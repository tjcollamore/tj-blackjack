import javafx.animation.FadeTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class AceCountHandView
    extends Pane
{
  private static final double HAND_WIDTH =
      52;

  private final Image[] positiveImages =
      new Image[5];

  private final Image[] negativeImages =
      new Image[5];

  private final ImageView handView =
      new ImageView();

  private int lastDisplayedBalance =
      Integer.MIN_VALUE;


  public AceCountHandView()
  {
    positiveImages[0] =
        loadImage(
            "AceCount-0.png"
        );

    negativeImages[0] =
        positiveImages[0];

    for (int i = 1; i <= 4; i++)
    {
      positiveImages[i] =
          loadImage(
              "AceCount-Positive"
              + i
              + ".png"
          );

      negativeImages[i] =
          loadImage(
              "AceCount-Negative"
              + i
              + ".png"
          );
    }

    handView.setPreserveRatio(
        true
    );
    handView.setOpacity(0.65);
    
    handView.setFitWidth(
        HAND_WIDTH
    );
      
    // Flip horizontally so it's left hand
    handView.setScaleX(-1);
          
    handView.setMouseTransparent(
        true
    );

    getChildren().add(
        handView
    );

    setMouseTransparent(
        true
    );

    setManaged(
        false
    );

    setBalance(
        0
    );
  }


  public void setBalance(
      int mentalBalance)
  {
      int displayedBalance =
          Math.max(
              -4,
              Math.min(
                  4,
                  mentalBalance
              )
          );

    if (displayedBalance
        == lastDisplayedBalance)
    {
      return;
    }

    lastDisplayedBalance =
        displayedBalance;

    int magnitude =
        Math.abs(
            displayedBalance
        );

    Image image;

    if (displayedBalance > 0)
    {
      image =
          positiveImages[
              magnitude
          ];
    }
    else if (displayedBalance < 0)
    {
      image =
          negativeImages[
              magnitude
          ];
    }
    else
    {
      image =
          positiveImages[0];
    }

    handView.setImage(
        image
    );

    FadeTransition fade =
        new FadeTransition(
            Duration.millis(180),
            handView
        );

    fade.setFromValue(
        0.35
    );

    fade.setToValue(
        0.65
    );

    fade.play();
  }


  private Image loadImage(
      String filename)
  {
    return new Image(
        AceCountHandView.class
            .getResourceAsStream(
                "/" + filename
            )
    );
  }
}
