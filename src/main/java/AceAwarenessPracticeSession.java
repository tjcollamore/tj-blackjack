import java.util.List;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public class AceAwarenessPracticeSession
{
  private static final int DECK_COUNT =
      2;

    private final GameSettings settings;

    private final BasicStrategy strategy =
        new BasicStrategy();

    private final PlayerRoundEngine playerRoundEngine;

    private final DealerPlayEngine dealerPlayEngine;

    private int handsPlayed =
        0;
    
  private Shoe shoe;

  private final AceOnlyCount aceCount =
      new AceOnlyCount();

  private final List<AceAwarenessCheckpoint>
      checkpoints;

    private int questionsAnswered =
        0;

    private int questionsCorrect =
        0;

    private int aceBalanceAnswered =
        0;

    private int aceBalanceCorrect =
        0;

    private int tableSelectionAnswered =
        0;

    private int tableSelectionCorrect =
        0;

    private int super4Answered =
        0;

    private int super4Correct =
        0;

  private int checkpointIndex =
      0;


  public AceAwarenessPracticeSession(
      long seed)
  {
      settings =
          new GameSettings();

      settings.setDecksInShoe(
          DECK_COUNT
      );

      settings.setDealerHitsSoft17(
          true
      );

      settings.setDoubleAfterSplit(
          true
      );

      settings.setAllowSurrender(
          true
      );

      playerRoundEngine =
          new PlayerRoundEngine(
              settings
          );

      dealerPlayEngine =
          new DealerPlayEngine(
              settings
          );
    shoe =
        new Shoe(
            DECK_COUNT,
            seed
        );


          checkpoints =
              new ArrayList<>(
                  List.of(          // Shoe 1
          new AceAwarenessCheckpoint(
              0.35,
              true,
              true
          ),

          new AceAwarenessCheckpoint(
              0.75,
              true,
              true
          ),

          new AceAwarenessCheckpoint(
              1.00,
              true,
              true
          ),

          // Shoe 2
          new AceAwarenessCheckpoint(
              0.35,
              true,
              true
          ),

          new AceAwarenessCheckpoint(
              0.75,
              true,
              true
          ),

          new AceAwarenessCheckpoint(
              1.00,
              true,
              true
          )
      ));
  }

    public void configureSecondShoe(
        boolean aceRich)
    {
      if (aceRich)
      {
        checkpoints.set(
            3,
            new AceAwarenessCheckpoint(
                1.00,
                true,
                true
            )
        );

        checkpoints.set(
            4,
            new AceAwarenessCheckpoint(
                1.25,
                false,
                true
            )
        );

        checkpoints.set(
            5,
            new AceAwarenessCheckpoint(
                1.50,
                false,
                true
            )
        );
      }
      else
      {
        checkpoints.set(
            3,
            new AceAwarenessCheckpoint(
                0.35,
                true,
                true
            )
        );

        checkpoints.set(
            4,
            new AceAwarenessCheckpoint(
                0.75,
                true,
                true
            )
        );

        checkpoints.set(
            5,
            new AceAwarenessCheckpoint(
                1.00,
                true,
                true
            )
        );
      }
    }
    
    public void switchToNewShoe(
        long seed)
    {
      shoe =
          new Shoe(
              DECK_COUNT,
              seed
          );

      aceCount.reset();
    }
    
    public void recordAnswer(
        AceAwarenessQuestionType type,
        boolean correct)
    {
      questionsAnswered++;

      if (correct)
      {
        questionsCorrect++;
      }

      switch (type)
      {
        case ACE_BALANCE:
          aceBalanceAnswered++;

          if (correct)
          {
            aceBalanceCorrect++;
          }

          break;

        case TABLE_SELECTION:
          tableSelectionAnswered++;

          if (correct)
          {
            tableSelectionCorrect++;
          }

          break;

        case SUPER_4:
          super4Answered++;

          if (correct)
          {
            super4Correct++;
          }

          break;
      }
    }
    

    public int getQuestionsAnswered()
    {
      return questionsAnswered;
    }


    public int getQuestionsCorrect()
    {
      return questionsCorrect;
    }
    
  public Shoe getShoe()
  {
    return shoe;
  }


  public AceOnlyCount getAceCount()
  {
    return aceCount;
  }


  public int getDeckCount()
  {
    return DECK_COUNT;
  }


  public AceAwarenessCheckpoint
      getCurrentCheckpoint()
  {
    if (checkpointIndex
        >= checkpoints.size())
    {
      return null;
    }


    return checkpoints.get(
        checkpointIndex
    );
  }


  public void advanceCheckpoint()
  {
    checkpointIndex++;
  }


  public boolean isFinished()
  {
    return checkpointIndex
        >= checkpoints.size();
  }


  public int getCheckpointNumber()
  {
    return checkpointIndex + 1;
  }


  public int getCheckpointCount()
  {
    return checkpoints.size();
  }

    public int getHandsPlayed()
    {
      return handsPlayed;
    }

    public AceAwarenessPracticeHand playNextHand()
    {
      List<Card> visiblePlayerCards =
          new ArrayList<>();

      List<Card> visibleDealerCards =
          new ArrayList<>();

      Map<Card, Boolean> observed =
          new IdentityHashMap<>();


      Hand playerHand =
          new Hand();

      // simplify this test
      playerHand.blockSplitting();

      Hand dealerHand =
          new Hand();


      // Player first card
      Card player1 =
          shoe.drawCard();

      playerHand.addCard(
          player1
      );

      observeVisible(
          player1,
          observed,
          visiblePlayerCards
      );


      // Dealer up card
      Card dealerUp =
          shoe.drawCard();

      dealerHand.addCard(
          dealerUp
      );

      observeVisible(
          dealerUp,
          observed,
          visibleDealerCards
      );


      // Player second card
      Card player2 =
          shoe.drawCard();

      playerHand.addCard(
          player2
      );

      observeVisible(
          player2,
          observed,
          visiblePlayerCards
      );


      // Dealer hole card -- NOT visible yet
      Card dealerHole =
          shoe.drawCard();

      dealerHand.addCard(
          dealerHole
      );


      boolean playerBlackjack =
          playerHand.isBlackjack();

      boolean dealerBlackjack =
          dealerHand.isBlackjack();


      /*
       * Dealer blackjack reveals the hole card.
       */
      if (dealerBlackjack)
      {
        observeVisible(
            dealerHole,
            observed,
            visibleDealerCards
        );

        handsPlayed++;

        return new AceAwarenessPracticeHand(
            visiblePlayerCards,
            visibleDealerCards
        );
      }


      /*
       * Natural player blackjack ends the hand
       * without revealing the dealer hole card.
       */
      if (playerBlackjack)
      {
        handsPlayed++;

        return new AceAwarenessPracticeHand(
            visiblePlayerCards,
            visibleDealerCards
        );
      }


      PlayerHandState initialState =
          new PlayerHandState(
              playerHand
          );


      List<PlayerHandState> states =
          playerRoundEngine.play(
              initialState,
              dealerUp,
              strategy,
              shoe,
              new PlayerRoundListener()
              {
                @Override
                public boolean canDoubleOrSplit(
                    PlayerHandState state)
                {
                  return true;
                }


                @Override
                public double getWagerAmount(
                    PlayerHandState state)
                {
                  return 25.0;
                }


                @Override
                public void handStarted(
                    PlayerHandState state,
                    int index,
                    int handCount)
                {
                }


                @Override
                public void cardDrawn(
                    PlayerHandState state,
                    Card card)
                {
                  observeVisible(
                      card,
                      observed,
                      visiblePlayerCards
                  );
                }


                @Override
                public void handDoubled(
                    PlayerHandState state)
                {
                }


                @Override
                public void handSurrendered(
                    PlayerHandState state)
                {
                }


                  @Override
                  public void handSplit(
                      PlayerHandState originalState,
                      PlayerHandState newState,
                      HandSplitResult splitResult)
                  {
                    throw new IllegalStateException(
                        "Splitting should be disabled "
                        + "during Ace Awareness practice"
                    );
                  }
                  

                @Override
                public void handFinished(
                    PlayerHandState state,
                    int index,
                    int handCount,
                    HandFinishReason reason)
                {
                }
              }
          );


      boolean dealerNeedsToPlay =
          false;


      for (PlayerHandState state :
           states)
      {
        if (!state.getHand().isBusted()
            && !state.didSurrender())
        {
          dealerNeedsToPlay =
              true;

          break;
        }
      }


      if (dealerNeedsToPlay)
      {
        /*
         * Hole card now becomes visible.
         */
        observeVisible(
            dealerHole,
            observed,
            visibleDealerCards
        );


        dealerPlayEngine.play(
            dealerHand,
            shoe,
            card ->
                observeVisible(
                    card,
                    observed,
                    visibleDealerCards
                )
        );
      }


      handsPlayed++;


      return new AceAwarenessPracticeHand(
          visiblePlayerCards,
          visibleDealerCards
      );
    }

    private void observeVisible(
        Card card,
        Map<Card, Boolean> observed,
        List<Card> visibleCards)
    {
      if (observed.containsKey(
          card
      ))
      {
        return;
      }


      observed.put(
          card,
          true
      );

      aceCount.observe(
          card
      );

      visibleCards.add(
          card
      );
    }

    public boolean isCheckpointReached()
    {
      AceAwarenessCheckpoint checkpoint =
          getCurrentCheckpoint();

      if (checkpoint == null)
      {
        return false;
      }

      return getDecksSeen()
          >= checkpoint.getDecksSeenTarget();
    }
    
   
    public int getDiscardedCardCount()
    {
      int startingCardCount =
          DECK_COUNT * 52;

      return startingCardCount
          - shoe.getRemainingCardCount();
    }


    public double getDecksSeen()
    {
      return getDiscardedCardCount()
          / 52.0;
    }

    public int getAceBalanceAnswered()
    {
      return aceBalanceAnswered;
    }


    public int getAceBalanceCorrect()
    {
      return aceBalanceCorrect;
    }


    public int getTableSelectionAnswered()
    {
      return tableSelectionAnswered;
    }


    public int getTableSelectionCorrect()
    {
      return tableSelectionCorrect;
    }


    public int getSuper4Answered()
    {
      return super4Answered;
    }


    public int getSuper4Correct()
    {
      return super4Correct;
    }
}
