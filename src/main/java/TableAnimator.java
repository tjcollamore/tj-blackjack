import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.animation.ParallelTransition;
import java.util.List;
import java.util.ArrayList;

public class TableAnimator
{
  private static final Duration DEAL_DURATION =
      Duration.millis(260);

  private static final Duration FLIP_HALF_DURATION =
      Duration.millis(90);

  private static final Duration BETWEEN_CARDS =
      Duration.millis(80);

  private BlackjackSceneCreator sceneCreator;

  public TableAnimator(BlackjackSceneCreator sceneCreator)
  {
    this.sceneCreator = sceneCreator;
  }

  public void dealCard(Card card, CardView cardView, boolean leaveFaceDown)
  {
    Node shoeAnchor = sceneCreator.getShoeAnchor();

    // The CardView must already be in the scene graph so that
    // JavaFX knows its final position.
    cardView.applyCss();
    cardView.layout();

    Bounds shoeBounds =
        shoeAnchor.localToScene(
            shoeAnchor.getBoundsInLocal()
        );

    Bounds cardBounds =
        cardView.localToScene(
            cardView.getBoundsInLocal()
        );

    double shoeX = shoeBounds.getCenterX();
    double shoeY = shoeBounds.getCenterY();

    double cardX = cardBounds.getCenterX();
    double cardY = cardBounds.getCenterY();

    // Start visually at the shoe.
    cardView.setTranslateX(shoeX - cardX);
    cardView.setTranslateY(shoeY - cardY);

    cardView.showBack();

    TranslateTransition deal =
        new TranslateTransition(
            DEAL_DURATION,
            cardView
        );

    deal.setToX(0);
    deal.setToY(0);
    deal.setInterpolator(Interpolator.EASE_OUT);

    if (leaveFaceDown)
    {
      SequentialTransition sequence =
          new SequentialTransition(
              deal,
              new PauseTransition(BETWEEN_CARDS)
          );

      playAndWait(sequence);
    }
    else
    {
      ScaleTransition flipClose =
          new ScaleTransition(
              FLIP_HALF_DURATION,
              cardView
          );

      flipClose.setFromX(1.0);
      flipClose.setToX(0.0);

      flipClose.setOnFinished(e ->
          cardView.showFront(card)
      );

      ScaleTransition flipOpen =
          new ScaleTransition(
              FLIP_HALF_DURATION,
              cardView
          );

      flipOpen.setFromX(0.0);
      flipOpen.setToX(1.0);

      SequentialTransition sequence =
          new SequentialTransition(
              deal,
              flipClose,
              flipOpen,
              new PauseTransition(BETWEEN_CARDS)
          );

      playAndWait(sequence);
    }
  }

  private void playAndWait(SequentialTransition animation)
  {
    Object key = new Object();

    animation.setOnFinished(e ->
        javafx.application.Platform.exitNestedEventLoop(
            key,
            null
        )
    );

    animation.play();

    javafx.application.Platform.enterNestedEventLoop(key);
  }

    public void flipCard(Card card, CardView cardView)
    {
      ScaleTransition flipClose =
          new ScaleTransition(FLIP_HALF_DURATION, cardView);
    
      flipClose.setFromX(1.0);
      flipClose.setToX(0.0);
      flipClose.setOnFinished(e -> cardView.showFront(card));
    
      ScaleTransition flipOpen =
          new ScaleTransition(FLIP_HALF_DURATION, cardView);
    
      flipOpen.setFromX(0.0);
      flipOpen.setToX(1.0);
    
      SequentialTransition sequence =
          new SequentialTransition(flipClose, flipOpen);
    
      playAndWait(sequence);
    }

    public void discardCards(List<Node> cards)
    {
      if (cards.isEmpty())
        return;

      Node discardAnchor = sceneCreator.getDiscardAnchor();

      Bounds discardBounds =
          discardAnchor.localToScene(
              discardAnchor.getBoundsInLocal()
          );

      double discardX = discardBounds.getCenterX();
      double discardY = discardBounds.getCenterY();

      SequentialTransition wholeSequence =
          new SequentialTransition();

      for (Node node : cards)
      {
        if (!(node instanceof CardView))
          continue;

        CardView cardView = (CardView) node;

        ScaleTransition flipClose =
            new ScaleTransition(
                FLIP_HALF_DURATION,
                cardView
            );

        flipClose.setFromX(1.0);
        flipClose.setToX(0.0);

        flipClose.setOnFinished(e ->
            cardView.showBack()
        );

        ScaleTransition flipOpen =
            new ScaleTransition(
                FLIP_HALF_DURATION,
                cardView
            );

        flipOpen.setFromX(0.0);
        flipOpen.setToX(1.0);

        SequentialTransition flip =
            new SequentialTransition(
                flipClose,
                flipOpen
            );

        wholeSequence.getChildren().add(flip);
      }

      ParallelTransition sweep =
          new ParallelTransition();

      for (Node node : cards)
      {
        if (!(node instanceof CardView))
          continue;

        CardView cardView = (CardView) node;

        Bounds cardBounds =
            cardView.localToScene(
                cardView.getBoundsInLocal()
            );

        TranslateTransition move =
            new TranslateTransition(
                Duration.millis(320),
                cardView
            );

        move.setByX(
            discardX - cardBounds.getCenterX()
        );

        move.setByY(
            discardY - cardBounds.getCenterY()
        );

        move.setInterpolator(
            Interpolator.EASE_IN
        );

        sweep.getChildren().add(move);
      }

      wholeSequence.getChildren().add(sweep);

      playAndWait(wholeSequence);
    }

    public void clearTable()
    {
      List<Node> allCards = new ArrayList<>();

      allCards.addAll(
          sceneCreator.getDealerCards()
      );

      allCards.addAll(
          sceneCreator.getPlayerCards()
      );
        
      discardCards(allCards);

      sceneCreator.getDealerScoreBadge().setVisible(false);          sceneCreator.getPlayerScoreBadge().setVisible(false);

      sceneCreator.getDealerCards().clear();
      sceneCreator.getPlayerCards().clear();
    }

    public void pause(Duration duration)
    {
      PauseTransition pause =
          new PauseTransition(duration);

      playAndWait(
          new SequentialTransition(pause)
      );
    }
}
