import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;


public class DiscardTrayView extends Pane
{
  private static final double PIXELS_PER_DECK =
      6.8;

  private static final double CARDS_PER_DECK =
      52.0;


  private static final double STACK_FRONT_LEFT_X =
      30.0;

  private static final double STACK_BACK_BOTTOM_Y =
      60.0;

  private static final double PERSPECTIVE_HEIGHT =
      17.0;

  private static final double PERSPECTIVE_WIDTH =
      4.0;

  private static final double CARD_WIDTH =
      32.0;


  private final GameSettings settings;


  private final ImageView trayView =
      new ImageView(
          new Image(
              DiscardTrayView.class
                  .getResourceAsStream(
                      "/discard-tray.png"
                  )
          )
      );


  private final ImageView topCardView =
      new ImageView(
          new Image(
              DiscardTrayView.class
                  .getResourceAsStream(
                      "/card_back4_angled.png"
                  )
          )
      );


  private final Polygon stackBody =
      new Polygon();


  private final Pane stripeLayer =
      new Pane();


  private int discardedCardCount;


  public DiscardTrayView(
      GameSettings settings)
  {
    setManaged(false);

    this.settings =
        settings;


    setPrefSize(
        120,
        150
    );

    setMinSize(
        USE_PREF_SIZE,
        USE_PREF_SIZE
    );


    trayView.setPreserveRatio(
        true
    );

    trayView.setFitWidth(
        115
    );

    trayView.setMouseTransparent(
        true
    );


    stackBody.setFill(
        Color.rgb(
            158,
            158,
            158
        )
    );

    stackBody.setStroke(
        Color.rgb(
            45,
            45,
            42
        )
    );

    stackBody.setStrokeWidth(
        0.5
    );

    stackBody.setMouseTransparent(
        true
    );


    stripeLayer.setMouseTransparent(
        true
    );


    topCardView.setPreserveRatio(
        true
    );

    topCardView.setMouseTransparent(
        true
    );

    topCardView.setViewport(
        new Rectangle2D(
            64,
            250,
            1356,
            670
        )
    );

    topCardView.setFitWidth(
        36
    );


    getChildren().addAll(
        trayView,
        stackBody,
        stripeLayer,
        topCardView
    );


    updateDisplay();
  }


  public void addDiscardedCards(
      int count)
  {
    if (count < 0)
    {
      throw new IllegalArgumentException(
          "Discarded card count cannot be negative"
      );
    }


    setDiscardedCardCount(
        discardedCardCount + count
    );
  }


  public void setDiscardedCardCount(
      int count)
  {
    if (count < 0)
    {
      throw new IllegalArgumentException(
          "Discarded card count cannot be negative"
      );
    }


    int maximumCards =
        settings.getDecksInShoe()
        * (int) CARDS_PER_DECK;


    discardedCardCount =
        Math.min(
            count,
            maximumCards
        );


    updateDisplay();
  }


  public int getDiscardedCardCount()
  {
    return discardedCardCount;
  }


  public void reset()
  {
    setDiscardedCardCount(
        0
    );
  }


  public Node getAnimationAnchor()
  {
    return trayView;
  }


  private void updateDisplay()
  {
    if (discardedCardCount == 0)
    {
      stackBody.setVisible(
          false
      );

      stripeLayer.setVisible(
          false
      );

      topCardView.setVisible(
          false
      );

      return;
    }


    stackBody.setVisible(
        true
    );

    stripeLayer.setVisible(
        true
    );

    topCardView.setVisible(
        true
    );


    double decksDiscarded =
        discardedCardCount
        / CARDS_PER_DECK;


    double stackHeight =
        PIXELS_PER_DECK
        * decksDiscarded;


    double stackBackTopY =
        STACK_BACK_BOTTOM_Y
        - stackHeight;


    double stackFrontTopY =
        stackBackTopY
        + PERSPECTIVE_HEIGHT;


    double stackFrontBottomY =
        STACK_BACK_BOTTOM_Y
        + PERSPECTIVE_HEIGHT;


    double stackFrontRightX =
        STACK_FRONT_LEFT_X
        + CARD_WIDTH;


    double stackBackRightX =
        stackFrontRightX
        + PERSPECTIVE_WIDTH;


    //          /|
    //         / |
    //   -----/  |
    //   |      /
    //   |     /
    //   -----/
    stackBody
        .getPoints()
        .setAll(
            stackBackRightX,
            stackBackTopY,

            stackFrontRightX,
            stackFrontTopY,

            STACK_FRONT_LEFT_X,
            stackFrontTopY,

            STACK_FRONT_LEFT_X,
            stackFrontBottomY,

            stackFrontRightX,
            stackFrontBottomY,

            stackBackRightX,
            STACK_BACK_BOTTOM_Y
        );


    redrawStripes(
        stackBackTopY,
        stackFrontTopY,
        stackFrontBottomY,
        stackFrontRightX,
        stackBackRightX
    );


    topCardView.setLayoutX(
        STACK_FRONT_LEFT_X
    );

    topCardView.setLayoutY(
        stackBackTopY
    );
  }

    public double getDecksSeen()
    {
      return discardedCardCount / 52.0;
    }
    

  private void redrawStripes(
      double stackBackTopY,
      double stackFrontTopY,
      double stackFrontBottomY,
      double stackFrontRightX,
      double stackBackRightX)
  {
    stripeLayer
        .getChildren()
        .clear();


    // Front edges of the discarded cards.
    for (double y =
             stackFrontBottomY - 1;
         y >= stackFrontTopY + 1;
         y -= 2.0)
    {
      Line stripe =
          new Line(
              STACK_FRONT_LEFT_X,
              y,
              stackFrontRightX,
              y
          );


      stripe.setStroke(
          Color.rgb(
              60,
              60,
              58
          )
      );

      stripe.setStrokeWidth(
          0.5
      );


      stripeLayer
          .getChildren()
          .add(
              stripe
          );
    }


    // Right/depth edges of the discarded cards.
    for (double y =
             stackFrontBottomY - 1;
         y >= stackFrontTopY + 1;
         y -= 2.0)
    {
      Line stripe =
          new Line(
              stackFrontRightX,
              y,
              stackBackRightX,
              y - PERSPECTIVE_HEIGHT
          );


      stripe.setStroke(
          Color.rgb(
              60,
              60,
              58
          )
      );

      stripe.setStrokeWidth(
          0.2
      );


      stripeLayer
          .getChildren()
          .add(
              stripe
          );
    }
  }
}
