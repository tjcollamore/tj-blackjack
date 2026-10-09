import javafx.util.Duration;
import javafx.stage.Stage;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;

public class Round
{
  private Stage stage;
  private BlackjackSceneCreator sceneCreator;
  private IOutputter outputter;
  private Shoe shoe;
  private HandStrategy playerHandStrategy;
  private int betAmount;
  private Bankroll bankroll;
  private TableAnimator tableAnimator;
  private GameSettings settings;
  private Map<Hand, HandView> handViews = new IdentityHashMap<>();
  private DealerPlayEngine dealerPlayEngine;
  private PlayerRoundEngine playerRoundEngine;
  
  
  public Round(Stage stage, BlackjackSceneCreator sceneCreator, IOutputter outputter, Shoe shoe, HandStrategy playerHandStrategy, int betAmount, Bankroll bankroll, GameSettings settings)
  {
    this.stage = stage;
    this.sceneCreator = sceneCreator;
    this.outputter = outputter;
    this.shoe = shoe;
    this.playerHandStrategy = playerHandStrategy;
    this.betAmount = betAmount;
    this.bankroll = bankroll;
    this.tableAnimator = new TableAnimator(sceneCreator);
    this.settings = settings;
    this.dealerPlayEngine = new DealerPlayEngine(settings);
    this.playerRoundEngine = new PlayerRoundEngine(settings);
  }
  
  public void play()
  {
    sceneCreator.getPlayerScoreBadge().setShowSoftIndicator(true);
    
    Wager wager = new Wager(bankroll, betAmount);
    wager.place();

    int maxNumSubrounds = 4;
    Subround[] subrounds = new Subround[maxNumSubrounds]; 

    Hand dealerHand = new Hand();
    HandView dealerHandView = new HandView(outputter, true, sceneCreator.getDealerCards(), true, sceneCreator.getDealerScoreBadge());
    registerHandView(dealerHand, dealerHandView);
    
    Hand firstPlayerHand = new Hand();
    HandView firstPlayerHandView = new HandView(outputter, false, sceneCreator.getPlayerCards(), true, sceneCreator.getPlayerScoreBadge());
    registerHandView(firstPlayerHand, firstPlayerHandView);
    
    subrounds[0] = new Subround(firstPlayerHand, wager);

    Map<PlayerHandState, Subround> subroundByState = new IdentityHashMap<>();
    PlayerHandState initialState = new PlayerHandState(firstPlayerHand);
    subroundByState.put(initialState, subrounds[0]);
    
    // Player first card
    Card playerCard1 = shoe.drawCard();
    firstPlayerHand.addCard(playerCard1);
    CardView playerCardView1 = firstPlayerHandView.cardAdded(firstPlayerHand, playerCard1);
    tableAnimator.dealCard(playerCard1, playerCardView1, false);

    // Dealer first card
    Card dealerCard1 = shoe.drawCard();
    dealerHand.addCard(dealerCard1);
    CardView dealerCardView1 = dealerHandView.cardAdded(dealerHand, dealerCard1);
    tableAnimator.dealCard(dealerCard1, dealerCardView1, false);

    // Player second card
    Card playerCard2 = shoe.drawCard();
    
    //Testing: always split
       while (playerCard1.getRank().getValue() != playerCard2.getRank().getValue()) playerCard2 = shoe.drawCard();
    
    firstPlayerHand.addCard(playerCard2);
    CardView playerCardView2 = firstPlayerHandView.cardAdded(firstPlayerHand, playerCard2);
    tableAnimator.dealCard(playerCard2, playerCardView2, false);

    // Dealer hole card
    Card dealerCard2 = shoe.drawCard();
    dealerHand.addCard(dealerCard2);
    CardView dealerCardView2 = dealerHandView.cardAdded(dealerHand, dealerCard2);
    tableAnimator.dealCard(dealerCard2, dealerCardView2, true);

    boolean dealerBlackjack = dealerHand.isBlackjack();
    if (dealerBlackjack)
    {
      HandView dealerView = getHandView(dealerHand);
      dealerView.showDealerHoleCard(dealerHand);
      tableAnimator.flipCard(dealerHand.getDealerHoleCard(),
                              dealerView.getDealerHoleCardView());
    }
    else
    {
      playerRoundEngine.play(
          initialState,
          dealerCard1,
          playerHandStrategy,
          shoe,
          new PlayerRoundListener()
          {
            @Override
            public boolean canDoubleOrSplit(PlayerHandState state)
            {
              Subround subround = subroundByState.get(state);
              return subround.getWager().hasFundsToDoubleOrSplit();
            }

            @Override
            public double getWagerAmount(PlayerHandState state)
            {
              return subroundByState.get(state).getWager().getBetAmount();
            }

            @Override
            public void handStarted(PlayerHandState state, int index, int handCount)
            {
              Hand hand = state.getHand();

              sceneCreator
                  .getPlayerScoreBadge()
                  .setShowSoftIndicator(true);

              getHandView(hand).start(hand);

              sceneCreator
                  .getSubroundBadge()
                  .update(
                      index + 1,
                      handCount
                  );
            }

            @Override
            public void cardDrawn(PlayerHandState state, Card card)
            {
              Hand hand = state.getHand();
              CardView cardView =getHandView(hand)
                      .cardAdded(
                          hand,
                          card
                      );

              tableAnimator.dealCard(card, cardView, false);
            }

            @Override
            public void handDoubled(PlayerHandState state)
            {
              subroundByState.get(state).getWager().doubleDown();
            }
  
            @Override
            public void handSurrendered(PlayerHandState state)
            {
              subroundByState.get(state).getWager().surrender();
            }

            @Override
            public void handFinished(
                PlayerHandState state,
                int index,
                int handCount)
            {
              if (state.didSplitAces()
                  || state.getWagerMultiplier() > 1.0)
              {
                tableAnimator.pause(
                    Duration.seconds(0.8)
                );
              }
            }
            
            @Override
            public void handSplit(
                PlayerHandState originalState,
                PlayerHandState newState,
                HandSplitResult splitResult)
            {
              Subround currentSubround = subroundByState.get(originalState);
              Hand currentHand = originalState.getHand();
              Hand newPlayerHand = newState.getHand();
              HandView newPlayerHandView = new HandView(outputter,
                      false,
                      sceneCreator.getPlayerCards(),
                      false,
                      sceneCreator.getPlayerScoreBadge()
                  );

              registerHandView(newPlayerHand, newPlayerHandView);

              Wager newWager = new Wager(
                                    bankroll,
                                    currentSubround
                                        .getWager()
                                        .getBetAmount());
              newWager.place();
              Subround newSubround = new Subround(newPlayerHand, newWager);

              currentSubround.setCameFromSplit(true);
              newSubround.setCameFromSplit(true);
              currentSubround.setSplitAces(splitResult.didSplitAces());
              newSubround.setSplitAces(splitResult.didSplitAces());

              int nextFreeSlot = nextFreeSlot(subrounds);
              subrounds[nextFreeSlot] = newSubround;
              subroundByState.put(newState, newSubround);

              getHandView(currentHand).cardRemoved(1);
              getHandView(currentHand).cardAdded(
                                                currentHand,
                                                splitResult
                                                    .getCardDealtToCurrentHand()
                                            );
            }
          }
      );
      
      sceneCreator.getPlayerScoreBadge().setShowSoftIndicator(false);
      playDealerHand(subrounds, dealerHand);
    }

    resolveSubrounds(subrounds, dealerHand);

    if (hasMultipleSubrounds(subrounds))
    {
      showAggregateResult(subrounds);
      reviewSplitResults(subrounds);
    }
    else
    {
      RoundResultMessage result = subrounds[0].getResultMessage();

      if (result != null && result.getMessage() != null
          && !result.getMessage().isBlank())
      {
        ChoiceDialog.showGameMessage(
            stage,
            result.getMessage(),
            result.getTone()
        );

        tableAnimator.pause(
            Duration.seconds(2.2)
        );
      }
    }
    
    tableAnimator.clearTable();
    sceneCreator.getSubroundBadge().reset();
  }
    
  private void playDealerHand(Subround[] subrounds, Hand dealerHand)
  {
    if (!dealerNeedsToPlay(subrounds))
      return;

    HandView dealerView = getHandView(dealerHand);
    dealerView.showDealerHoleCard(dealerHand);
    tableAnimator.flipCard(dealerHand.getDealerHoleCard(), dealerView.getDealerHoleCardView());

    dealerPlayEngine.play(dealerHand, shoe,
      card ->
      {
        CardView cardView = getHandView(dealerHand).cardAdded(dealerHand, card);
        tableAnimator.dealCard(card, cardView, false);
      }
    );
    
    outputter.output("Dealer score is: " + dealerHand.getScore());
  }
  
  private boolean dealerNeedsToPlay(Subround[] subrounds)
  {
    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;

      Hand hand = subround.getHand();
      Wager wager = subround.getWager();

      if (!hand.isBusted() && !wager.didSurrender() && !subround.isNaturalBlackjack())
        return true;
    }
    return false;
  }
  
  private int nextFreeSlot(Subround[] subrounds)
  {
    for (int i = 0; i < subrounds.length; i++)
    {
      if (subrounds[i] == null)
        return i;
    }
    throw new IllegalStateException("No free slots available");
  }

  private int getNumSubrounds(Subround[] subrounds)
  {
    int count = 0;
    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;
      count++;
    }
    return count;
  }

  private boolean hasMultipleSubrounds(Subround[] subrounds)
  {
    return subrounds[1] != null;
  }

  
  private void resolveSubrounds(
    Subround[] subrounds,
    Hand dealerHand)
  {
    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;
  
      RoundResultMessage result =
          RoundResultAnalyzer.analyze(
              subround,
              dealerHand,
              outputter,
              settings
          );
  
      subround.setResultMessage(result);
    }
  }

  private void reviewSplitResults(Subround[] subrounds)
  {
    int total = getNumSubrounds(subrounds);

    // The navigator now provides the "1 of 2" context,
    // so don't also show the old badge on the cards.
    sceneCreator.getSubroundBadge().reset();

    ReviewNavigator reviewNavigator =
        new ReviewNavigator(total);

    reviewNavigator.setOnSelectionChanged(index ->
        showReviewSubround(subrounds, index)
    );

    // Always begin review on the first hand.
    showReviewSubround(subrounds, 0);

    ChoiceDialog.show(
        stage,
        "",
        MessageTone.DEFAULT,
        null,
        null,
        reviewNavigator,
        "DONE"
    );
  }

  private void showReviewSubround(
    Subround[] subrounds,
    int index)
  {
    Subround subround = subrounds[index];
    Hand hand = subround.getHand();
    getHandView(hand).showPlayerHand(hand);
  }

  private double getTotalWagered(Subround[] subrounds)
  {
    double total = 0;

    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;

      total += subround.getWager().getBetAmount();
    }

    return total;
  }

  private double getTotalPayout(Subround[] subrounds)
  {
    double total = 0;

    for (Subround subround : subrounds)
    {
      if (subround == null)
        break;

      total += subround.getWager().getPayout();
    }

    return total;
  }

  private void showAggregateResult(Subround[] subrounds)
  {
    double wagered = getTotalWagered(subrounds);
    double payout = getTotalPayout(subrounds);
    double net = payout - wagered;

    String message;
    MessageTone tone;

    if (net > 0)
    {
      message =
          "YOU WON "
          + bankroll.getFormattedCurrency(payout)
          + "! (+"
          + bankroll.getFormattedCurrency(net)
          + ")";

      tone = MessageTone.POSITIVE;
    }
    else if (net == 0)
    {
      message =
          "RECOUPED ALL "
          + bankroll.getFormattedCurrency(wagered);

      tone = MessageTone.NEUTRAL;
    }
    else if (payout > 0)
    {
      message =
          "RECOUPED "
          + bankroll.getFormattedCurrency(payout)
          + " OF "
          + bankroll.getFormattedCurrency(wagered);

      tone = MessageTone.NEGATIVE;
    }
    else
    {
      message =
          "LOST ALL "
          + bankroll.getFormattedCurrency(wagered)
          + "!";

      tone = MessageTone.NEGATIVE;
    }

    ChoiceDialog.showGameMessage(
        stage,
        message,
        tone
    );

    tableAnimator.pause(
        Duration.seconds(2.2)
    );
  }

  private void registerHandView(Hand hand, HandView handView)
  {
    handViews.put(hand, handView);
  }

  private HandView getHandView(Hand hand)
  {
    HandView handView = handViews.get(hand);

  if (handView == null)
  {
    throw new IllegalStateException("No HandView registered for hand");
  }

  return handView;
  }
}
