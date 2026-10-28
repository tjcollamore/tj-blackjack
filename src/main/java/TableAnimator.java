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
import javafx.animation.Interpolator;

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
    Node shoeAnchor = sceneCreator.getDealerArea();

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
          if (sceneCreator.isDiscardTrayVisible())
          {
            ScaleTransition shrink =
                new ScaleTransition(
                    Duration.millis(320),
                    cardView
                );

            shrink.setToX(
                0.35
            );

            shrink.setToY(
                0.35
            );

            shrink.setInterpolator(
                Interpolator.EASE_IN
            );

            sweep.getChildren().add(
                shrink
            );
          }
      }

      wholeSequence.getChildren().add(sweep);

      playAndWait(wholeSequence);
    }

    public void clearTable()
    {
      List<Node> allCards =
          new ArrayList<>();


      allCards.addAll(
          sceneCreator.getDealerCards()
      );

      allCards.addAll(
          sceneCreator.getPlayerCards()
      );


      int discardedCards =
          0;


      for (Node node :
           allCards)
      {
        if (node instanceof CardView)
        {
          discardedCards++;
        }
      }


      discardCards(
          allCards
      );


      sceneCreator.addDiscardedCards(
          discardedCards
      );


      sceneCreator
          .getDealerScoreBadge()
          .setVisible(
              false
          );

      sceneCreator
          .getPlayerScoreBadge()
          .setVisible(
              false
          );


      sceneCreator
          .getDealerCards()
          .clear();

      sceneCreator
          .getPlayerCards()
          .clear();


      sceneCreator
          .getSplitWagerChipView()
          .setAmount(
              0,
              ChipDisplayMode.WAGER
          );


      sceneCreator
          .getSplitWagerChipView()
          .setTranslateX(
              0
          );


      sceneCreator
          .getWagerChipView()
          .setTranslateX(
              0
          );
    }
    
    public void pause(Duration duration)
    {
      PauseTransition pause =
          new PauseTransition(duration);

      playAndWait(
          new SequentialTransition(pause)
      );
    }

public void commitWager(
    double amount)
{
  animateWagerAddition(
      amount,
      amount
  );
}


public void animateWagerAddition(
    double addedAmount,
    double finalAmount)
{
  Node previewArea =
      sceneCreator
          .getWagerPreviewArea();

  Node wagerArea =
      sceneCreator
          .getWagerArea();

  double previewStartX =
      previewArea.getTranslateX();

  double previewStartY =
      previewArea.getTranslateY();

  double destinationX =
      wagerArea.getTranslateX();

  double destinationY =
      wagerArea.getTranslateY();

  /*
   * Show only the NEW chips being added.
   *
   * On the initial wager:
   *   addedAmount = original wager.
   *
   * On DOUBLE:
   *   addedAmount = the additional wager.
   */
  sceneCreator
      .getWagerPreviewChipView()
      .setAmount(
          addedAmount,
          ChipDisplayMode.WAGER
      );

  TranslateTransition slide =
      new TranslateTransition(
          Duration.millis(340),
          previewArea
      );

  slide.setFromX(
      previewStartX
  );

  slide.setFromY(
      previewStartY
  );

  slide.setToX(
      destinationX
  );

  slide.setToY(
      destinationY
  );

  slide.setInterpolator(
      Interpolator.EASE_BOTH
  );

  slide.play();

  pause(
      Duration.millis(340)
  );

  /*
   * The extra chips have reached the betting area.
   * Now redraw the committed wager as the new total.
   */
  sceneCreator
      .getWagerChipView()
      .setAmount(
          finalAmount,
          ChipDisplayMode.WAGER
      );

  sceneCreator
      .getWagerPreviewChipView()
      .setAmount(
          0,
          ChipDisplayMode.WAGER
      );

  /*
   * Reset the moving container for the next
   * wager-related animation.
   */
  previewArea.setTranslateX(
      previewStartX
  );

  previewArea.setTranslateY(
      previewStartY
  );
}

        public void animateSplitWager(
            double wagerAmount)
        {
          Node previewArea =
              sceneCreator
                  .getWagerPreviewArea();

          ChipStackView originalWager =
              sceneCreator
                  .getWagerChipView();

          ChipStackView newWager =
              sceneCreator
                  .getSplitWagerChipView();

          Node wagerArea =
              sceneCreator
                  .getWagerArea();

          double previewStartX =
              previewArea.getTranslateX();

          double previewStartY =
              previewArea.getTranslateY();

          double splitOffset = 45;

          sceneCreator
              .getWagerPreviewChipView()
              .setAmount(
                  wagerAmount,
                  ChipDisplayMode.WAGER
              );

          newWager.setTranslateX(
              splitOffset
          );

          newWager.setAmount(
              0,
              ChipDisplayMode.WAGER
          );

          TranslateTransition moveOriginal =
              new TranslateTransition(
                  Duration.millis(260),
                  originalWager
              );

          moveOriginal.setToX(
              -splitOffset
          );

          moveOriginal.setInterpolator(
              Interpolator.EASE_BOTH
          );

          TranslateTransition moveNew =
              new TranslateTransition(
                  Duration.millis(340),
                  previewArea
              );

          moveNew.setFromX(
              previewStartX
          );

          moveNew.setFromY(
              previewStartY
          );

          moveNew.setToX(
              wagerArea.getTranslateX()
              + splitOffset
          );

          moveNew.setToY(
              wagerArea.getTranslateY()
          );

          moveNew.setInterpolator(
              Interpolator.EASE_BOTH
          );

          ParallelTransition splitAnimation =
              new ParallelTransition(
                  moveOriginal,
                  moveNew
              );

          splitAnimation.play();

          pause(
              Duration.millis(340)
          );

          newWager.setAmount(
              wagerAmount,
              ChipDisplayMode.WAGER
          );

          sceneCreator
              .getWagerPreviewChipView()
              .setAmount(
                  0,
                  ChipDisplayMode.WAGER
              );

          previewArea.setTranslateX(
              previewStartX
          );

          previewArea.setTranslateY(
              previewStartY
          );

          /*
           * Leave both piles visible briefly so the
           * player sees the physical split wager.
           */
          pause(
              Duration.millis(300)
          );

        }    

    public void moveSplitCardRight(
        CardView cardView)
    {
      TranslateTransition move =
          new TranslateTransition(
              Duration.millis(280),
              cardView
          );

      move.setByX(
          120
      );

      move.setInterpolator(
          Interpolator.EASE_BOTH
      );

      move.play();

      pause(
          Duration.millis(280)
      );
    }

    public void finishSplitWagerAnimation()
    {
      ChipStackView originalWager =
          sceneCreator
              .getWagerChipView();

      ChipStackView splitWager =
          sceneCreator
              .getSplitWagerChipView();

      originalWager.setTranslateX(
          0
      );

      splitWager.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      splitWager.setTranslateX(
          0
      );
    }

    public void animateLostWager()
    {
      ChipStackView wagerView =
          sceneCreator
              .getWagerChipView();

      Node wagerArea =
          sceneCreator
              .getWagerArea();

      Node dealerArea =
          sceneCreator
              .getDealerArea();

      Bounds wagerBounds =
          wagerArea.localToScene(
              wagerArea.getBoundsInLocal()
          );

      Bounds dealerBounds =
          dealerArea.localToScene(
              dealerArea.getBoundsInLocal()
          );

      double dx =
          dealerBounds.getCenterX()
          - wagerBounds.getCenterX();

      double dy =
          dealerBounds.getCenterY()
          - wagerBounds.getCenterY();

      TranslateTransition move =
          new TranslateTransition(
              Duration.millis(360),
              wagerView
          );

      move.setByX(dx);
      move.setByY(dy);
      move.setInterpolator(
          Interpolator.EASE_IN
      );

      move.play();

      pause(
          Duration.millis(360)
      );

      wagerView.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      wagerView.setTranslateX(0);
      wagerView.setTranslateY(0);
    }

    public void animateWinningPayout(double wagerAmount, double payoutAmount)
    {
      ChipStackView originalWager =
          sceneCreator
              .getWagerChipView();

      ChipStackView payoutWager =
          sceneCreator
              .getSplitWagerChipView();

      Node wagerArea =
          sceneCreator
              .getWagerArea();

      ChipStackView bankrollView =
          sceneCreator
              .getBankrollChipView();

      double winnings = payoutAmount - wagerAmount;

      /*
       * Keep the player's original wager,
       * and place only the dealer's winnings
       * beside it.
       */
      originalWager.setTranslateX(
          -32
      );

      payoutWager.setAmount(
          winnings,
          ChipDisplayMode.WAGER
      );
    
      payoutWager.setTranslateX(
          32
      );

        Node dealerArea =
            sceneCreator
                .getDealerArea();

        dealerArea.applyCss();
        payoutWager.applyCss();

        Bounds dealerBounds =
            dealerArea.localToScene(
                dealerArea.getBoundsInLocal()
            );

        Bounds payoutBounds =
            payoutWager.localToScene(
                payoutWager.getBoundsInLocal()
            );

        double dealerDx =
            dealerBounds.getCenterX()
            - payoutBounds.getCenterX();

        double dealerDy =
            dealerBounds.getCenterY()
            - payoutBounds.getCenterY();

        payoutWager.setTranslateX(
            32 + dealerDx
        );

        payoutWager.setTranslateY(
            dealerDy
        );

        TranslateTransition payPlayer =
            new TranslateTransition(
                Duration.millis(500),
                payoutWager
            );

        payPlayer.setToX(
            32
        );

        payPlayer.setToY(
            0
        );

        payPlayer.setInterpolator(
            Interpolator.EASE_BOTH
        );

        payPlayer.play();

        pause(
            Duration.millis(500)
        );
        
      /*
       * Let the player register the payout.
       */
      pause(
          Duration.millis(350)
      );

        moveWagerViewsToBankroll(
            originalWager,
            payoutWager
        );

    }

    public void animatePush()
    {
      ChipStackView wagerView =
          sceneCreator
              .getWagerChipView();

      pause(
          Duration.millis(300)
      );

      moveWagerViewsToBankroll(
          wagerView
      );
    }

    public void animateSurrender(
        double wagerAmount)
    {
      ChipStackView dealerHalf =
          sceneCreator
              .getWagerChipView();

      ChipStackView playerHalf =
          sceneCreator
              .getSplitWagerChipView();

      Node wagerArea =
          sceneCreator
              .getWagerArea();

      Node dealerArea =
          sceneCreator
              .getDealerArea();

      ChipStackView bankrollView =
          sceneCreator
              .getBankrollChipView();

      double halfAmount =
          wagerAmount / 2.0;


      /*
       * Visually divide the original wager
       * into two equal piles.
       */
      dealerHalf.setAmount(
          halfAmount,
          ChipDisplayMode.WAGER
      );

      playerHalf.setAmount(
          halfAmount,
          ChipDisplayMode.WAGER
      );

      /*
       * Start both halves essentially together,
       * then pull them apart so the split is visible.
       */
      dealerHalf.setTranslateX(0);
      playerHalf.setTranslateX(0);

      TranslateTransition separateDealer =
          new TranslateTransition(
              Duration.millis(260),
              dealerHalf
          );

      separateDealer.setToX(
          32
      );

      separateDealer.setInterpolator(
          Interpolator.EASE_BOTH
      );

      TranslateTransition separatePlayer =
          new TranslateTransition(
              Duration.millis(260),
              playerHalf
          );

      separatePlayer.setToX(
          -32
      );

      separatePlayer.setInterpolator(
          Interpolator.EASE_BOTH
      );

      ParallelTransition separate =
          new ParallelTransition(
              separateDealer,
              separatePlayer
          );

      separate.play();

      pause(
          Duration.millis(260)
      );

      /*
       * Give the player a moment to see:
       *
       * half goes to house,
       * half comes back.
       */
      pause(
          Duration.millis(250)
      );


      Bounds wagerBounds =
          wagerArea.localToScene(
              wagerArea.getBoundsInLocal()
          );

      Bounds dealerBounds =
          dealerArea.localToScene(
              dealerArea.getBoundsInLocal()
          );

      Bounds bankrollBounds =
          bankrollView.localToScene(
              bankrollView.getBoundsInLocal()
          );


      double dealerDx =
          dealerBounds.getCenterX()
          - wagerBounds.getCenterX();

      double dealerDy =
          dealerBounds.getCenterY()
          - wagerBounds.getCenterY();

      double bankrollDx =
          bankrollBounds.getCenterX()
          - wagerBounds.getCenterX();

      double bankrollDy =
          bankrollBounds.getCenterY()
          - wagerBounds.getCenterY();


      /*
       * House takes one half.
       */
      TranslateTransition takeHalf =
          new TranslateTransition(
              Duration.millis(420),
              dealerHalf
          );

      takeHalf.setByX(
          dealerDx
      );

      takeHalf.setByY(
          dealerDy
      );

      takeHalf.setInterpolator(
          Interpolator.EASE_IN
      );


      /*
       * Player receives the other half back.
       */
      TranslateTransition returnHalf =
          new TranslateTransition(
              Duration.millis(420),
              playerHalf
          );

      returnHalf.setByX(
          bankrollDx
      );

      returnHalf.setByY(
          bankrollDy
      );

      returnHalf.setInterpolator(
          Interpolator.EASE_IN
      );


      ParallelTransition settlement =
          new ParallelTransition(
              takeHalf,
              returnHalf
          );

      settlement.play();

      pause(
          Duration.millis(420)
      );


      /*
       * Clean up.
       */
      dealerHalf.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      playerHalf.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      dealerHalf.setTranslateX(0);
      dealerHalf.setTranslateY(0);

      playerHalf.setTranslateX(0);
      playerHalf.setTranslateY(0);
    }

    public void animateAggregateSettlement(
        double wagered,
        double payout)
    {
      ChipStackView wagerView =
          sceneCreator
              .getWagerChipView();

      ChipStackView secondView =
          sceneCreator
              .getSplitWagerChipView();

      /*
       * During split play the visible wager represents
       * only the active hand. For settlement, temporarily
       * represent the entire round as one aggregate wager.
       */
      wagerView.setAmount(
          wagered,
          ChipDisplayMode.WAGER
      );

      secondView.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      wagerView.setTranslateX(0);
      wagerView.setTranslateY(0);

      secondView.setTranslateX(0);
      secondView.setTranslateY(0);


      if (payout == 0)
      {
        /*
         * Everything was lost.
         */
        animateLostWager();
      }
      else if (payout == wagered)
      {
        /*
         * Aggregate push / break-even.
         */
        animatePush();
      }
      else if (payout > wagered)
      {
        /*
         * Overall winning round.
         * Dealer adds the net winnings, then
         * everything returns to the player.
         */
        animateWinningPayout(
            wagered,
            payout
        );
      }
      else
      {
        /*
         * Partial recovery:
         * some of the aggregate wager is lost,
         * while the rest comes back.
         */
        animatePartialSettlement(
            wagered - payout,
            payout
        );
      }
    }

    private void animatePartialSettlement(
        double lostAmount,
        double returnedAmount)
    {
      ChipStackView dealerPortion =
          sceneCreator
              .getWagerChipView();

      ChipStackView playerPortion =
          sceneCreator
              .getSplitWagerChipView();

      Node wagerArea =
          sceneCreator
              .getWagerArea();

      Node dealerArea =
          sceneCreator
              .getDealerArea();

      ChipStackView bankrollView =
          sceneCreator
              .getBankrollChipView();


      /*
       * Represent the two pieces of the
       * aggregate result.
       */
      dealerPortion.setAmount(
          lostAmount,
          ChipDisplayMode.WAGER
      );

      playerPortion.setAmount(
          returnedAmount,
          ChipDisplayMode.WAGER
      );

      dealerPortion.setTranslateX(0);
      playerPortion.setTranslateX(0);


      /*
       * Pull the two portions apart briefly.
       */
      TranslateTransition separateDealer =
          new TranslateTransition(
              Duration.millis(260),
              dealerPortion
          );

      separateDealer.setToX(
          32
      );

      separateDealer.setInterpolator(
          Interpolator.EASE_BOTH
      );

      TranslateTransition separatePlayer =
          new TranslateTransition(
              Duration.millis(260),
              playerPortion
          );

      separatePlayer.setToX(
          -32
      );

      separatePlayer.setInterpolator(
          Interpolator.EASE_BOTH
      );

      ParallelTransition separate =
          new ParallelTransition(
              separateDealer,
              separatePlayer
          );

      separate.play();

      pause(
          Duration.millis(260)
      );

      pause(
          Duration.millis(250)
      );


      Bounds wagerBounds =
          wagerArea.localToScene(
              wagerArea.getBoundsInLocal()
          );

      Bounds dealerBounds =
          dealerArea.localToScene(
              dealerArea.getBoundsInLocal()
          );

      Bounds bankrollBounds =
          bankrollView.localToScene(
              bankrollView.getBoundsInLocal()
          );


      double dealerDx =
          dealerBounds.getCenterX()
          - wagerBounds.getCenterX();

      double dealerDy =
          dealerBounds.getCenterY()
          - wagerBounds.getCenterY();

      double bankrollDx =
          bankrollBounds.getCenterX()
          - wagerBounds.getCenterX();

      double bankrollDy =
          bankrollBounds.getCenterY()
          - wagerBounds.getCenterY();


      /*
       * Lost portion goes to the house.
       */
      TranslateTransition takeLost =
          new TranslateTransition(
              Duration.millis(420),
              dealerPortion
          );

      takeLost.setByX(
          dealerDx
      );

      takeLost.setByY(
          dealerDy
      );

      takeLost.setInterpolator(
          Interpolator.EASE_IN
      );


      /*
       * Recovered portion returns to player.
       */
      TranslateTransition returnRecovered =
          new TranslateTransition(
              Duration.millis(420),
              playerPortion
          );

      returnRecovered.setByX(
          bankrollDx
      );

      returnRecovered.setByY(
          bankrollDy
      );

      returnRecovered.setInterpolator(
          Interpolator.EASE_IN
      );


      ParallelTransition settlement =
          new ParallelTransition(
              takeLost,
              returnRecovered
          );

      settlement.play();

      pause(
          Duration.millis(420)
      );


      dealerPortion.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      playerPortion.setAmount(
          0,
          ChipDisplayMode.WAGER
      );

      dealerPortion.setTranslateX(0);
      dealerPortion.setTranslateY(0);

      playerPortion.setTranslateX(0);
      playerPortion.setTranslateY(0);
    }
    
    private void moveWagerViewsToBankroll(
        ChipStackView... wagerViews)
    {
      Node wagerArea =
          sceneCreator
              .getWagerArea();

      ChipStackView bankrollView =
          sceneCreator
              .getBankrollChipView();

      Bounds wagerBounds =
          wagerArea.localToScene(
              wagerArea.getBoundsInLocal()
          );

      Bounds bankrollBounds =
          bankrollView.localToScene(
              bankrollView.getBoundsInLocal()
          );

      double dx =
          bankrollBounds.getCenterX()
          - wagerBounds.getCenterX();

      double dy =
          bankrollBounds.getCenterY()
          - wagerBounds.getCenterY();

      ParallelTransition moveAll =
          new ParallelTransition();

      for (ChipStackView wagerView :
           wagerViews)
      {
        TranslateTransition move =
            new TranslateTransition(
                Duration.millis(420),
                wagerView
            );

        move.setByX(dx);
        move.setByY(dy);
        move.setInterpolator(
            Interpolator.EASE_IN
        );

        moveAll.getChildren().add(
            move
        );
      }

      moveAll.play();

      pause(
          Duration.millis(420)
      );

      for (ChipStackView wagerView :
           wagerViews)
      {
        wagerView.setAmount(
            0,
            ChipDisplayMode.WAGER
        );

        wagerView.setTranslateX(0);
        wagerView.setTranslateY(0);
      }
    }
    
}
